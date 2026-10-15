package com.plazoletaucc.Plazoleta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="plato")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Plato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable=false)
    private String nombre;
    @Column(nullable=false)
    private Integer precio;
    private String descripcion;
    private String urlImagen;
    // true = activo. Todo plato nuevo nace en true (HU-03)
    private Boolean estado;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="idCategoria", nullable=false)
    private Categoria categoria;

    // Todo plato debe estar asociado a un restaurante (HU-03)
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="idRestaurante", nullable=false)
    private Restaurante restaurante;
}
