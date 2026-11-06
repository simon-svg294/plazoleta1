package com.plazoletaucc.Plazoleta.dto.reponse;

import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// HU-12: pedido completo para el listado que consulta el empleado.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoListadoResponseDTO {

    private Integer id;
    private Integer idCliente;
    private String nombreCliente;
    private Integer idRestaurante;
    private String nombreRestaurante;
    private EstadoPedido estado;
    private Integer idEmpleadoAsignado;
    private LocalDateTime fechaCreacion;
    private Integer total;
    private List<DetallePedidoResponseDTO> platos;
}
