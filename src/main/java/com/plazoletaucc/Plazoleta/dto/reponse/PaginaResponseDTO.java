package com.plazoletaucc.Plazoleta.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// HU-09: respuesta paginada reutilizable (tambien la usan HU-10 y HU-12).
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginaResponseDTO<T> {
    private List<T> contenido;
    private Integer pagina;
    private Integer tamano;
    private Long totalElementos;
    private Integer totalPaginas;
}
