package com.plazoletaucc.Plazoleta.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlatoRequestDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    // Integer = numero entero (si mandan 25000.5 Spring lo rechaza)
    // @Positive = mayor a 0
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser un numero entero mayor a 0")
    private Integer precio;
    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;
    @NotBlank(message = "La url de la imagen es obligatoria")
    private String urlImagen;
    // Que existan lo revisa el service. No se manda estado ni propietario.
    @NotNull(message = "La categoria es obligatoria")
    private Integer idCategoria;
    @NotNull(message = "El restaurante es obligatorio")
    private Integer idRestaurante;
}
