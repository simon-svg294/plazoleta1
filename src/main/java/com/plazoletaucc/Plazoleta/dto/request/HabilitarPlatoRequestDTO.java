package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// HU-07: habilitar/deshabilitar un plato.
// El DTO solo tiene el campo activo: los demas campos del plato no se pueden cambiar.
@Getter
@Setter
public class HabilitarPlatoRequestDTO {
    @NotNull(message = "El campo activo es obligatorio")
    private Boolean activo;
}
