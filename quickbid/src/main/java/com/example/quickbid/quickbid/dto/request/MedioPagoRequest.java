package com.example.quickbid.quickbid.dto.request;
import jakarta.validation.constraints.*;
public record MedioPagoRequest(@NotBlank String tipo,@NotBlank String moneda,@NotNull Boolean nacional,@NotBlank String titular,String numeroTarjeta,String cvv,Integer vencimientoMes,Integer vencimientoAnio,String marca,String numeroCuenta,String cbuCvu,String nombreBanco,String alias){}
