package com.example.quickbid.quickbid.perfil.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PerfilResponse {

    private String email;
    private String nombre;       // nombre completo: "Nicolas Lazzaro"
    private String iniciales;    // "NL"
    private String categoria;    // comun | especial | plata | oro | platino
    private Double reputacionPostor;
    private Integer puntajeAcumulado;
}
