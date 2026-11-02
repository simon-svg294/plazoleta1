package com.plazoletaucc.Plazoleta.dto.reponse;

import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// HU-14: respuesta al empleado cuando notifica que el pedido esta listo.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoListoResponseDTO {

    private Integer id;
    private EstadoPedido estado;
    private String pin;
    private String mensaje;
}
