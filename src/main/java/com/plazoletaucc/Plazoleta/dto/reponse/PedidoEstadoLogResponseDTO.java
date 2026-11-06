package com.plazoletaucc.Plazoleta.dto.reponse;

import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// HU-17: respuesta con un registro de cambio de estado del pedido.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEstadoLogResponseDTO {

    private Integer id;
    private Integer idPedido;
    private EstadoPedido estadoAnterior;
    private EstadoPedido estadoNuevo;
    private Integer idEmpleado;
    private String nombreEmpleado;
    private LocalDateTime fechaCambio;
}
