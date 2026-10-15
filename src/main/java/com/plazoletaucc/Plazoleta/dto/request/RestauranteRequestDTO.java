package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestauranteRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El NIT es obligatorio")
    @Pattern(regexp = "^[0-9]+$", message = "El NIT debe ser unicamente numerico")
    private String nit;

    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;

    @NotBlank(message = "El telefono es obligatorio")
    @Size(max = 13, message = "El telefono debe contener un maximo de 13 caracteres")
    @Pattern(regexp = "^\\+?[0-9]{1,13}$", message = "El telefono debe contener un maximo de 13 caracteres y puede contener el simbolo +. Ejemplo: +573005698325")
    private String telefono;

    @NotBlank(message = "La url del logo es obligatoria")
    private String urlLogo;

    @NotNull(message = "El id del propietario es obligatorio")
    private Integer idPropietario;
}
