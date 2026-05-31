package com.example.quickbid.quickbid.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class VerificarTokenRequest {

    @NotBlank
    private String token;
}
