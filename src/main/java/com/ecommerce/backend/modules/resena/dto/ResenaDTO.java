package com.ecommerce.backend.modules.resena.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResenaDTO {

    private Integer id;
    private Integer calificacion;
    private String titulo;
    private String comentario;
    private Boolean esVerificada;
    private LocalDateTime creadoEn;

    // ── Datos del usuario ────────────────────────────────────
    private Integer usuarioId;
    private String nombreUsuario;
    private String avatarUrl;

    // ── Datos del producto ───────────────────────────────────
    private Integer productoId;
    private String productoNombre;

    // ── Imágenes ─────────────────────────────────────────────
    private List<String> imagenesUrl;
}