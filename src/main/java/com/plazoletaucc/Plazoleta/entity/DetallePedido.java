package com.plazoletaucc.Plazoleta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_pedido",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_detalle_pedido_plato",
                columnNames = {"id_pedido", "id_plato"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plato", nullable = false)
    private Plato plato;

    @Column(nullable = false)
    private Integer cantidad;

    // HU-11: se guarda el precio al momento de crear el pedido.
    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;
}
