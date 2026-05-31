package com.example.quickbid.quickbid.mediospago.dto;

import com.example.quickbid.quickbid.mediospago.MedioPago;
import lombok.Getter;

@Getter
public class MedioPagoResponse {

    private final Long id;
    private final String tipo;
    private final String marca;
    private final String datosEnmascarados;
    private final String titular;
    private final String moneda;
    private final String estado;
    private final boolean esPrincipal;

    public MedioPagoResponse(MedioPago m) {
        this.id = m.getIdentificador();
        this.tipo = m.getTipo();
        this.marca = m.getMarca();
        this.datosEnmascarados = m.getDatosEnmascarados();
        this.titular = m.getTitular();
        this.moneda = m.getMoneda();
        this.estado = m.getEstado();
        this.esPrincipal = m.isEsPrincipal();
    }
}
