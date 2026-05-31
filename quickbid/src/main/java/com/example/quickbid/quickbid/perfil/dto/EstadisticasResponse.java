package com.example.quickbid.quickbid.perfil.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EstadisticasResponse {

    private String periodo;
    private Long totalPujado;
    private Integer porcentajeExito;
    private Long totalPagado;
    private List<PuntoHistorico> serieHistorica;

    @Getter
    @Builder
    public static class PuntoHistorico {
        private String etiqueta;
        private Long valor;
    }
}
