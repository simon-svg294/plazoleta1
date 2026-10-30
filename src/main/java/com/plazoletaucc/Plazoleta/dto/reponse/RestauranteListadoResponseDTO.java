package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// HU-09: al cliente solo se le muestran el nombre y el logo del restaurante.
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestauranteListadoResponseDTO {
    private String nombre;
    private String urlLogo;
}
