package com.example.quickbid.quickbid.mediospago.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CrearMedioPagoRequest {

    @NotBlank
    @Pattern(regexp = "tarjeta_credito|tarjeta_debito|cuenta_bancaria|cheque",
             message = "tipo debe ser: tarjeta_credito, tarjeta_debito, cuenta_bancaria o cheque")
    private String tipo;

    @NotBlank
    @Pattern(regexp = "ARS|USD", message = "moneda debe ser ARS o USD")
    private String moneda;

    // ── Tarjeta ──────────────────────────────────────────────────────────────
    private String nombreTitular;
    private String numeroTarjeta;  // 16 digitos — solo se guardan los ultimos 4
    private String vencimiento;    // MM/AA
    private String cvv;            // NO se persiste
    private Boolean nacional;

    // ── Cuenta bancaria ───────────────────────────────────────────────────────
    private String numeroCuenta;
    private String nombreBanco;
    private String alias;

    // ── Cheque ────────────────────────────────────────────────────────────────
    // Las fotos llegan como @RequestParam MultipartFile, no en este JSON.
    // Este DTO se usa para los campos de texto del cheque.
    private BigDecimal monto;
    private LocalDate fechaVencimiento;
    private String numeroCheque;
}
