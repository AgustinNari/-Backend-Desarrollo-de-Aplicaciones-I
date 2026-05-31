package com.example.quickbid.quickbid.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Persiste las fotos del DNI del usuario (frente + dorso).
 * Se crea en etapa3 al finalizar el registro.
 * Tabla nueva segun spec: documentos_identidad
 */
@Entity
@Table(name = "documentos_identidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoIdentidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identificador;

    // FK -> usuarios.id (en el modelo definitivo sera FK -> personas.identificador)
    @Column(nullable = false)
    private Long usuarioId;

    @Lob
    @Column(nullable = false)
    private byte[] fotoFrente;

    @Lob
    @Column(nullable = false)
    private byte[] fotoDorso;

    @Column(nullable = false)
    private LocalDateTime fechaCarga;
}
