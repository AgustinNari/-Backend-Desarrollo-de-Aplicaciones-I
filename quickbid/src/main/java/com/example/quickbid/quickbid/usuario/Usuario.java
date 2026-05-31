package com.example.quickbid.quickbid.usuario;

import jakarta.persistence.*;
import lombok.*;


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

    @Column(nullable = false)
    @Builder.Default
    private String categoria = "comun"; // comun | especial | plata | oro | platino

    @Column(nullable = false)
    @Builder.Default
    private String estadoCuenta = "activo"; // activo | bloqueado | activo_sin_medio_pago

    @Column(nullable = false)
    @Builder.Default
    private boolean verificado = false;
}
