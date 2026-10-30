package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.RestauranteRequestDTO;

public interface RestauranteService {
    RestauranteResponseDTO crearRestaurante(RestauranteRequestDTO dto);

    // HU-09: listado paginado y alfabetico para el cliente
    PaginaResponseDTO<RestauranteListadoResponseDTO> listarRestaurantes(int pagina, int tamano);
}
