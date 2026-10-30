package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.HabilitarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.ModificarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PlatoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Categoria;
import com.plazoletaucc.Plazoleta.entity.Plato;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.exception.AccesoDenegadoException;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.CategoriaRepository;
import com.plazoletaucc.Plazoleta.repository.PlatoRespository;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.service.PlatoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {
    private final PlatoRespository platoRespository;
    private final CategoriaRepository categoriaRepository;
    private final RestauranteRepository restauranteRepository;

    @Override
    public PlatoResponseDTO registrarPlato(PlatoRequestDTO platoRequestDTO, String correoUsuario) {
        // 1. El restaurante y la categoria deben existir
        Restaurante restaurante = restauranteRepository.findById(platoRequestDTO.getIdRestaurante())
                .orElseThrow(() -> new ReglaNegocioException("El restaurante no existe"));

        Categoria categoria = categoriaRepository.findById(platoRequestDTO.getIdCategoria())
                .orElseThrow(() -> new ReglaNegocioException("La categoria no existe"));

        // 2. Solo el propietario del restaurante puede crear platos
        validarDueno(restaurante, correoUsuario);

        // 3. Todo plato recien creado nace activo (estado = true)
        Plato plato = Plato.builder()
                .nombre(platoRequestDTO.getNombre())
                .precio(platoRequestDTO.getPrecio())
                .descripcion(platoRequestDTO.getDescripcion())
                .urlImagen(platoRequestDTO.getUrlImagen())
                .estado(true)
                .categoria(categoria)
                .restaurante(restaurante)
                .build();

        return convertir(platoRespository.save(plato));
    }

    // HU-04: modificar plato. Solo cambia precio y descripcion; lo demas no se toca.
    @Override
    public PlatoResponseDTO modificarPlato(Integer idPlato, ModificarPlatoRequestDTO dto, String correoUsuario) {
        // 1. El plato debe existir
        Plato plato = platoRespository.findById(idPlato)
                .orElseThrow(() -> new ReglaNegocioException("El plato no existe"));

        // 2. No se permiten modificar platos de otros restaurantes diferentes al propio
        validarDueno(plato.getRestaurante(), correoUsuario);

        // 3. Se cambian SOLO los dos campos permitidos
        plato.setPrecio(dto.getPrecio());
        plato.setDescripcion(dto.getDescripcion());

        // 4. save() sobre un plato que ya tiene id hace UPDATE
        return convertir(platoRespository.save(plato));
    }

    // HU-07: habilitar/deshabilitar plato. Solo cambia el estado; lo demas queda igual.
    @Override
    public PlatoResponseDTO cambiarEstadoPlato(Integer idPlato, HabilitarPlatoRequestDTO dto, String correoUsuario) {
        // 1. El plato debe existir (404 para no confundirlo con los 400 de regla de negocio)
        Plato plato = platoRespository.findById(idPlato)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El plato no existe"));

        // 2. Solo el dueño del restaurante del plato puede cambiar su estado
        validarDueno(plato.getRestaurante(), correoUsuario);

        // 3. Se cambia SOLO el estado: activo = true (habilitado) / false (deshabilitado)
        plato.setEstado(dto.getActivo());

        return convertir(platoRespository.save(plato));
    }

    // HU-10: el cliente ve el menu de un restaurante, paginado y filtrable por categoria.
    @Override
    @Transactional(readOnly = true)
    public PaginaResponseDTO<PlatoResponseDTO> listarPlatos(Integer idRestaurante, Integer idCategoria,
                                                            int pagina, int tamano) {
        // 1. La pagina y el tamano deben tener sentido
        if (pagina < 0) {
            throw new ReglaNegocioException("La pagina debe ser 0 o mayor");
        }
        if (tamano < 1) {
            throw new ReglaNegocioException("El tamano de pagina debe ser 1 o mayor");
        }

        // 2. El restaurante del que se quiere ver el menu debe existir
        if (!restauranteRepository.existsById(idRestaurante)) {
            throw new ReglaNegocioException("El restaurante no existe");
        }

        // 3. Spring trae solo la pagina pedida y ya filtra los platos activos
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("nombre").ascending());
        Page<Plato> resultado = (idCategoria == null)
                ? platoRespository.findByRestaurante_IdAndEstadoTrue(idRestaurante, pageable)
                : platoRespository.findByRestaurante_IdAndCategoria_IdAndEstadoTrue(idRestaurante, idCategoria, pageable);

        List<PlatoResponseDTO> contenido = resultado.getContent().stream()
                .map(this::convertir)
                .toList();

        return PaginaResponseDTO.<PlatoResponseDTO>builder()
                .contenido(contenido)
                .pagina(resultado.getNumber())
                .tamano(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .build();
    }

    // El correo viene del token (authentication.getName()). Si no es el dueño -> 403
    private void validarDueno(Restaurante restaurante, String correoUsuario) {
        if (restaurante.getPropietario() == null
                || !restaurante.getPropietario().getCorreo().equals(correoUsuario)) {
            throw new AccesoDenegadoException("Solo el propietario del restaurante puede gestionar sus platos");
        }
    }

    private PlatoResponseDTO convertir(Plato plato) {
        return PlatoResponseDTO.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .precio(plato.getPrecio())
                .descripcion(plato.getDescripcion())
                .urlImagen(plato.getUrlImagen())
                .estado(plato.getEstado())
                .categoria(plato.getCategoria().getNombre())
                .idRestaurante(plato.getRestaurante().getId())
                .build();
    }
}
