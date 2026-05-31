package com.example.quickbid.quickbid.perfil.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HistorialResponse {

    private int total;
    private int page;
    private int limit;
    private List<Object> items; // Vacio hasta conectar tablas del profesor (pujos, registroDeSubasta, productos)
}
