package com.plazoletaucc.Plazoleta.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="usuario")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable=false)
    private String nombre;
    private String apellido;
    @Column(unique = true)
    private String documentoDeIdentidad;
    private String celular;
    private LocalDate fechaDeNacimiento;
    @Column(nullable=false, unique=true)
    private String correo;
    private String clave;
    @Enumerated(EnumType.STRING)
    private Rol rol;
}
