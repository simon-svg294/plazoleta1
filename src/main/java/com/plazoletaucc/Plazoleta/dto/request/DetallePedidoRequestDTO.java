package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetallePedidoRequestDTO {

    @NotNull(message = "El id del plato es obligatorio")
    private Integer idPlato;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}
