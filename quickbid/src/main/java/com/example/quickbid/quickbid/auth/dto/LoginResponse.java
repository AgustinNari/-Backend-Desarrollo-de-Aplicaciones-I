package com.example.quickbid.quickbid.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String nombre;
    private String categoria;
    private String estadoCuenta;
    private boolean requiereMedioPago;
    private boolean tieneMultasActivas;
}
