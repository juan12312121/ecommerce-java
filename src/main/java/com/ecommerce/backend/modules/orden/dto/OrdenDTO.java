package com.ecommerce.backend.modules.orden.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdenDTO {

    private Integer id;
    private String estado;
    private BigDecimal subtotal;
    private BigDecimal montoDescuento;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private String notas;
    private LocalDateTime creadoEn;

    // ── Datos del cupón ──────────────────────────────────────
    private String codigoCupon;
    private String tipoCupon;

    // ── Dirección de envío ───────────────────────────────────
    private String direccionEnvio;

    // ── Items ────────────────────────────────────────────────
    private List<ItemOrdenDTO> items;

    // ── Sub-órdenes por vendedor ─────────────────────────────
    private List<OrdenVendedorDTO> ordenesVendedor;

    // ── DTO interno para items ───────────────────────────────
    @Data
    public static class ItemOrdenDTO {
        private Integer id;
        private String productoNombre;
        private String productoSlug;
        private String sku;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
        private String nombreTienda;
        private Integer vendedorId;
    }

    // ── DTO interno para sub-orden vendedor ──────────────────
    @Data
    public static class OrdenVendedorDTO {
        private Integer id;
        private String nombreTienda;
        private String estado;
        private String numeroSeguimiento;
        private BigDecimal total;
    }
}