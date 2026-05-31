package com.example.quickbid.quickbid.usuario;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String clave; // bcrypt hash

    private String telefono;

    private LocalDate fechaNacimiento;

    private String domicilio;

    private String fotoDni; // path o URL de la foto del DNI

    @Column(nullable = false)
    @Builder.Default
    private boolean verificado = false;
}
