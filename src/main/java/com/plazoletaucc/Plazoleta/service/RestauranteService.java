package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.RestauranteRequestDTO;

public interface RestauranteService {
    RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO dto);
}
