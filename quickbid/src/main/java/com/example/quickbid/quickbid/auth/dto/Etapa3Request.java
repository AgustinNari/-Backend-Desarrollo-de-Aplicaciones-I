package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class Etapa3Request {

    @NotBlank
    private String setupToken;

    @NotBlank
    @Size(min = 8, message = "La clave debe tener al menos 8 caracteres")
    private String clave;

    @NotBlank
    private String claveConfirmacion;
}
