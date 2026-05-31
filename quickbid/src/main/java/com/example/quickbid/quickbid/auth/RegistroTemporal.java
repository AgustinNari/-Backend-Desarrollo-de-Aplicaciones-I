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
    private String telefono;
    private String domicilio;
    private String fotoDni;

    // Token de verificación enviado por email
    private String tokenVerificacion;

    private LocalDateTime tokenExpiracion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EtapaRegistro etapa = EtapaRegistro.ETAPA_1;

    public enum EtapaRegistro {
        ETAPA_1, ETAPA_2, VERIFICADO
    }
}
