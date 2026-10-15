package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PlatoResponseDTO {
    private Integer id;
    private String nombre;
    private Integer precio;
    private String descripcion;
    private String urlImagen;
    private Boolean estado;
    private String categoria;
    private Integer idRestaurante;
}
