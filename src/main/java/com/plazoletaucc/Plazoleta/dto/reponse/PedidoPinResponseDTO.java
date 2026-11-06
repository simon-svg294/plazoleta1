package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-14/HU-15: PIN que el cliente puede consultar en su propia cuenta para
 * reclamar el pedido. Nunca se devuelve al empleado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoPinResponseDTO {

    private Integer id;
    private String pin;
}
