package com.example.quickbid.quickbid.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Almacena el progreso del registro de 3 etapas antes de que el usuario complete el proceso.
 * Se elimina una vez que el usuario se registra correctamente.
 */
@Entity
@Table(name = "registros_temporales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroTemporal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String nombre;
    private String apellido;
    private String domicilioLegal;
    private Integer idPaisOrigen;

    @Lob
    private byte[] fotoFrenteDni;

    @Lob
    private byte[] fotoDorsoDni;

    // Token del link enviado por email para verificar la cuenta
    private String tokenVerificacion;
    private LocalDateTime tokenExpiracion;

    // Token temporal generado tras verificar el link; se usa en etapa3 para crear la clave
    private String setupToken;
    private LocalDateTime setupTokenExpiracion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EtapaRegistro etapa = EtapaRegistro.ETAPA_1;

    public enum EtapaRegistro {
        ETAPA_1, ETAPA_2, VERIFICADO
    }
}
