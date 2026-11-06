package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PedidoRequestDTO {

    @NotNull(message = "El restaurante es obligatorio")
    private Integer idRestaurante;

    @NotEmpty(message = "El pedido debe tener al menos un plato")
    @Valid
    private List<@NotNull(message = "El plato es obligatorio") DetallePedidoRequestDTO> platos;
}
