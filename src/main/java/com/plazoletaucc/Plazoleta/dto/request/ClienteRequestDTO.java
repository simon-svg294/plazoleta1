package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "El documento de identidad es obligatorio")
    @Pattern(regexp = "^[0-9]+$", message = "El documento de identidad debe ser unicamente numerico")
    private String documentoDeIdentidad;
    @NotBlank(message = "El celular es obligatorio")
    @Size(max = 13, message = "El celular debe contener un maximo de 13 caracteres")
    @Pattern(regexp = "^\\+?[0-9]{1,13}$", message = "El celular debe contener un maximo de 13 caracteres y puede contener el simbolo +. Ejemplo: +573005698325")
    private String celular;
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "La estructura del email no es valida")
    private String correo;
    @NotBlank(message = "La clave es obligatoria")
    private String clave;
}
