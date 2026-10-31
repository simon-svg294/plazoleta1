package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoResponseDTO {

    private Integer idPlato;
    private String nombre;
    private Integer cantidad;
    private Integer precioUnitario;
    private Integer subtotal;
}
