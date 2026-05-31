package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class Etapa3Request {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
    private String clave;
}
