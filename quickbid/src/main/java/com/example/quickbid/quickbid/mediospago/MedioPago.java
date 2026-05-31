package com.example.quickbid.quickbid.mediospago;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabla nueva: medios_de_pago
 * Almacena tarjetas, cuentas bancarias y cheques certificados del usuario.
 */
@Entity
@Table(name = "medios_de_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedioPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long identificador;

    @Column(nullable = false)
    private Long usuarioId; // FK -> usuarios.id (futuro: clientes.identificador)

    @Column(nullable = false, length = 25)
    private String tipo; // tarjeta_credito | tarjeta_debito | cuenta_bancaria | cheque

    @Column(nullable = false, length = 5)
    private String moneda; // ARS | USD

    @Column(length = 100)
    private String datosEnmascarados; // **** **** **** 1234 o cuenta parcial

    @Column(length = 200)
    private String titular;

    private BigDecimal limiteOperativo; // limite declarado (tarjeta) o monto (cheque)

    @Column(length = 30)
    private String marca; // Visa | Mastercard (solo tarjetas)

    @Column(length = 5)
    private String vencimientoTarjeta; // MM/AA (solo tarjetas)

    @Column(length = 50)
    private String numeroCuenta; // solo cuenta_bancaria

    @Column(length = 50)
    private String aliasCbu; // solo cuenta_bancaria

    @Column(length = 150)
    private String nombreBanco; // cuenta_bancaria y cheque

    @Column(nullable = false)
    @Builder.Default
    private boolean nacional = true;

    @Column(length = 30)
    private String numeroCheque; // solo cheque

    private LocalDate fechaVencimientoCheque; // solo cheque

    @Lob
    private byte[] fotoAnversoCheque; // solo cheque

    @Lob
    private byte[] fotoReversoCheque; // solo cheque

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "pendiente_verificacion";
    // pendiente_verificacion | verificado | expirado | rechazado | eliminado

    @Column(nullable = false)
    @Builder.Default
    private boolean esPrincipal = false;

    private LocalDateTime fechaVerificacion;
    private LocalDateTime vigenciaHasta; // 3 dias habiles desde verificacion

    @Column(nullable = false)
    @Builder.Default
    private boolean eliminado = false;
}
