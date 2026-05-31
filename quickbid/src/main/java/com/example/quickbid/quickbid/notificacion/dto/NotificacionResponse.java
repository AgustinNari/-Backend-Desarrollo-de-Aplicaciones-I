package com.example.quickbid.quickbid.notificacion.dto;

import com.example.quickbid.quickbid.notificacion.Notificacion;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificacionResponse {

    private final Long id;
    private final String tipo;
    private final String categoria;
    private final String mensaje;
    private final boolean leida;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaLectura;
    private final Long referenciaId;

    public NotificacionResponse(Notificacion n) {
        this.id = n.getId();
        this.tipo = n.getTipo();
        this.categoria = n.getCategoria();
        this.mensaje = n.getMensaje();
        this.leida = n.isLeida();
        this.fechaCreacion = n.getFechaCreacion();
        this.fechaLectura = n.getFechaLectura();
        this.referenciaId = n.getReferenciaId();
    }
}
