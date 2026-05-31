package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class Etapa1Request {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String domicilioLegal;

    @NotNull
    private Integer idPaisOrigen;
}
