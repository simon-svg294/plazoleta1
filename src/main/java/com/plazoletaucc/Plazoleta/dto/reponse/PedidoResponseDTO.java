package com.plazoletaucc.Plazoleta.dto.reponse;

import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Integer id;
    private Integer idRestaurante;
    private String nombreRestaurante;
    private EstadoPedido estado;
    private LocalDateTime fechaCreacion;
    private Integer total;
    private List<DetallePedidoResponseDTO> platos;
}
