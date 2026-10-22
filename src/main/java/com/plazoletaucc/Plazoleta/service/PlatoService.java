package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.HabilitarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.ModificarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PlatoRequestDTO;

public interface PlatoService {
    PlatoResponseDTO registrarPlato(PlatoRequestDTO platoRequestDTO, String correoUsuario);

    // HU-04: modificar precio y descripcion
    PlatoResponseDTO modificarPlato(Integer idPlato, ModificarPlatoRequestDTO dto, String correoUsuario);

    // HU-07: habilitar/deshabilitar plato
    PlatoResponseDTO cambiarEstadoPlato(Integer idPlato, HabilitarPlatoRequestDTO dto, String correoUsuario);
}
