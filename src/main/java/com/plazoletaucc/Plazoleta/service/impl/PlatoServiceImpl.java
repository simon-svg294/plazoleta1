package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
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
import org.springframework.stereotype.Service;

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
