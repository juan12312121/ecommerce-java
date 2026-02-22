package com.ecommerce.backend.modules.resena.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// ── Request para crear reseña ────────────────────────────────
@Data
public class ResenaRequest {

    @NotNull(message = "El item de orden es obligatorio")
    private Integer itemOrdenId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer calificacion;

    @Size(max = 200, message = "El título no puede superar 200 caracteres")
    private String titulo;

    private String comentario;

    private List<String> imagenesUrl;
}