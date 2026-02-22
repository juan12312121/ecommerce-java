package com.ecommerce.backend.modules.pago.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ── Request para iniciar pago ────────────────────────────────
@Data
public class PagoRequest {

    @NotNull(message = "La orden es obligatoria")
    private Integer ordenId;

    @NotNull(message = "El proveedor es obligatorio")
    private String proveedor; // STRIPE, MERCADOPAGO

    private String moneda; // MXN por defecto
}