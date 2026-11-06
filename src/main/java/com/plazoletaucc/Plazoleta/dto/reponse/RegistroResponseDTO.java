package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.*;

@Getter
@Setter
@Builder
public class RegistroResponseDTO {
    private Integer id;
    private String nombre;
    private String apellido;
    private String correo;
    private String documentoDeIdentidad;
    private String celular;
}
