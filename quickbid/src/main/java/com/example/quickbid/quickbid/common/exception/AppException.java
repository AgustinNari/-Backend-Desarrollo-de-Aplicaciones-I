package com.example.quickbid.quickbid.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción genérica para errores de negocio.
 * Uso: throw new AppException("El email ya está registrado", HttpStatus.CONFLICT);
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
