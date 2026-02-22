package com.ecommerce.backend.modules.orden.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ── Request para crear orden (checkout) ─────────────────────
@Data
public class OrdenRequest {

    @NotNull(message = "La dirección de envío es obligatoria")
    private Integer direccionEnvioId;

    private String codigoCupon; // opcional

    private String notas; // opcional
}