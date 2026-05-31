package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Etapa2Request {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String fotoDni; // base64 o URL de la imagen
}
