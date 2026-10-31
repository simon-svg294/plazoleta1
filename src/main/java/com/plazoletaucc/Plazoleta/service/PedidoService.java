package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;

public interface PedidoService {

    // HU-11: el cliente crea un pedido con platos de un mismo restaurante.
    PedidoResponseDTO crearPedido(PedidoRequestDTO dto, String correoCliente);
}
