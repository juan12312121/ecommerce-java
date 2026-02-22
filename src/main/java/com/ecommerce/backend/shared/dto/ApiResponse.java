package com.ecommerce.backend.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean exitoso;
    private String mensaje;
    private T datos;
    private String error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ── Respuesta exitosa solo con datos ─────────────────────
    public static <T> ApiResponse<T> exito(T datos) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .datos(datos)
                .build();
    }

    // ── Respuesta exitosa con mensaje y datos ────────────────
    public static <T> ApiResponse<T> exito(String mensaje, T datos) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .datos(datos)
                .build();
    }

    // ── Respuesta exitosa solo con mensaje ───────────────────
    public static <T> ApiResponse<T> exito(String mensaje) {
        return ApiResponse.<T>builder()
                .exitoso(true)
                .mensaje(mensaje)
                .build();
    }

    // ── Respuesta de error ───────────────────────────────────
    public static <T> ApiResponse<T> error(String error) {
        return ApiResponse.<T>builder()
                .exitoso(false)
                .error(error)
                .build();
    }
}