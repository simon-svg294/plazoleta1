package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

// HU-04: solo se puede modificar precio y descripcion.
// Por eso el DTO solo tiene esos dos campos: lo demas se ignora.
@Getter
@Setter
public class ModificarPlatoRequestDTO {
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un numero entero mayor a 0")
    private Integer precio;

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;
}
