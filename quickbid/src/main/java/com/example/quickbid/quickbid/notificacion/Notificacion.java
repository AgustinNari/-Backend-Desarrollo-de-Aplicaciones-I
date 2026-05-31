package com.example.quickbid.quickbid.notificacion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long usuarioId;

    /**
     * Tipos: puja_superada | puja_ganada | subasta_por_comenzar | catalogo_nuevo |
     *        consignacion_aprobada | consignacion_rechazada | documentacion_solicitada |
     *        acuerdo_pendiente | medio_pago_verificado | multa_asignada
     */
    @Column(nullable = false)
    private String tipo;

    /**
     * Categorias: subastas | transacciones
     */
    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    @Builder.Default
    private boolean leida = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column
    private LocalDateTime fechaLectura;

    // ID de referencia opcional (id de subasta, consignacion, etc.)
    @Column
    private Long referenciaId;
}
