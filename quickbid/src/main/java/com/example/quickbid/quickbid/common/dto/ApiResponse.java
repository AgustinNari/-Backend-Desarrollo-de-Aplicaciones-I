package com.example.quickbid.quickbid.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

/**
 * Formato estándar de respuesta para todos los endpoints.
 * { data, message, errors? }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final T data;
    private final String message;
    private final List<String> errors;

    private ApiResponse(T data, String message, List<String> errors) {
        this.data = data;
        this.message = message;
        this.errors = errors;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(data, message, null);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(null, message, null);
    }

    public static <T> ApiResponse<T> error(String message, List<String> errors) {
        return new ApiResponse<>(null, message, errors);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(null, message, null);
    }
}
