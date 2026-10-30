package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.RestauranteRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.NitDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.PropietarioNoValidoException;
import com.plazoletaucc.Plazoleta.exception.ReglaNegocioException;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestauranteServiceImpl implements RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO dto) {
        // Nombre: puede tener numeros, pero no solo numeros
        if (dto.getNombre() != null && dto.getNombre().matches("^[0-9]+$")) {
            throw new IllegalArgumentException("El nombre del restaurante no puede contener solo numeros");
        }

        // NIT duplicado
        if (restauranteRepository.existsByNit(dto.getNit())) {
            throw new NitDuplicadoException("El NIT ya esta registrado");
        }

        // El id debe corresponder a un usuario con rol PROPIETARIO
        Usuario propietario = usuarioRepository.findById(dto.getIdPropietario())
                .orElseThrow(() -> new PropietarioNoValidoException("El propietario no existe"));

        if (propietario.getRol() != Rol.PROPIETARIO) {
            throw new PropietarioNoValidoException("El id no corresponde a un usuario con rol propietario");
        }

        Restaurante guardado = restauranteRepository.save(Restaurante.builder()
                .nombre(dto.getNombre())
                .nit(dto.getNit())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .urlLogo(dto.getUrlLogo())
                .propietario(propietario)
                .build());

        return RestauranteResponseDTO.builder()
                .id(guardado.getId())
                .nombre(guardado.getNombre())
                .nit(guardado.getNit())
                .direccion(guardado.getDireccion())
                .telefono(guardado.getTelefono())
                .urlLogo(guardado.getUrlLogo())
                .idPropietario(propietario.getId())
                .build();
    }

    // HU-09: todos los restaurantes, en orden alfabetico y paginados.
    @Override
    public PaginaResponseDTO<RestauranteListadoResponseDTO> listarRestaurantes(int pagina, int tamano) {
        // 1. La pagina y el tamano deben tener sentido
        if (pagina < 0) {
            throw new ReglaNegocioException("La pagina debe ser 0 o mayor");
        }
        if (tamano < 1) {
            throw new ReglaNegocioException("El tamano de pagina debe ser 1 o mayor");
        }

        // 2. La base de datos ordena y trae solo la pagina pedida
        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("nombre").ascending());
        Page<Restaurante> resultado = restauranteRepository.findAll(pageable);

        // 3. Al cliente solo se le devuelven nombre y urlLogo
        List<RestauranteListadoResponseDTO> contenido = resultado.getContent().stream()
                .map(restaurante -> RestauranteListadoResponseDTO.builder()
                        .nombre(restaurante.getNombre())
                        .urlLogo(restaurante.getUrlLogo())
                        .build())
                .toList();

        return PaginaResponseDTO.<RestauranteListadoResponseDTO>builder()
                .contenido(contenido)
                .pagina(resultado.getNumber())
                .tamano(resultado.getSize())
                .totalElementos(resultado.getTotalElements())
                .totalPaginas(resultado.getTotalPages())
                .build();
    }
}
