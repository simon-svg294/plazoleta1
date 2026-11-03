package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

// HU-15: PIN que el cliente recibio por SMS y que el empleado digita para entregar el pedido.
@Getter
@Setter
public class EntregarPedidoRequestDTO {

    @NotBlank(message = "El PIN es obligatorio")
    @Pattern(regexp = "^\\d{4}$", message = "El PIN debe tener 4 digitos")
    private String pin;
}
