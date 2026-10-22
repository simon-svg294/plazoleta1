package com.plazoletaucc.Plazoleta.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String nit;

    @Column(nullable = false)
    private String direccion;

    private String telefono;

    private String urlLogo;

    // Propietario a cargo del restaurante (HU-02: debe tener rol PROPIETARIO)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idPropietario", nullable = false)
    private Usuario propietario;
}
