package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoEstadoLogResponseDTO;

import java.util.List;

public interface TrazabilidadService {

    // HU-17: el cliente consulta el historial de estados de uno de sus pedidos.
    List<PedidoEstadoLogResponseDTO> consultarTrazabilidad(Integer idPedido, String correoCliente);
}
