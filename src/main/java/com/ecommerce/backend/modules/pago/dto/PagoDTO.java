package com.ecommerce.backend.modules.pago.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PagoDTO {

    private Integer id;
    private Integer ordenId;
    private String proveedor;
    private String idPagoProveedor;
    private String estado;
    private BigDecimal monto;
    private String moneda;
    private String metodoPago;
    private LocalDateTime creadoEn;

    // ── URL de pago para redirigir al usuario ────────────────
    private String urlPago; // solo al crear el pago
}