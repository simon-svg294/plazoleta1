package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;

public interface PedidoService {

    // HU-11: el cliente crea un pedido con platos de un mismo restaurante.
    PedidoResponseDTO crearPedido(PedidoRequestDTO dto, String correoCliente);

    // HU-12: el empleado lista, paginado, los pedidos de su restaurante filtrando por estado.
    PaginaResponseDTO<PedidoListadoResponseDTO> listarPedidosPorEstado(EstadoPedido estado, int pagina, int tamano,
                                                                      String correoEmpleado);
}
