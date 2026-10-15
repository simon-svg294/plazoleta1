package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.PlatoRequestDTO;

public interface PlatoService {
    PlatoResponseDTO registrarPlato(PlatoRequestDTO platoRequestDTO, String correoUsuario);
}
