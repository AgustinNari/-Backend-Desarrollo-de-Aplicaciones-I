package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CambiarClaveRequest {

    @NotBlank
    private String token; // token recibido por email para recuperar clave

    @NotBlank
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    private String nuevaClave;
}
