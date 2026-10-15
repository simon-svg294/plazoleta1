package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.RestauranteRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Restaurante;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.NitDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.PropietarioNoValidoException;
import com.plazoletaucc.Plazoleta.repository.RestauranteRepository;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.RestauranteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
