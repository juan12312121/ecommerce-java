package com.ecommerce.backend.modules.catalogo.producto.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductoDTO {

    private Integer id;
    private String nombre;
    private String slug;
    private String descripcion;
    private BigDecimal precioBase;
    private String estado;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;
    private LocalDateTime creadoEn;

    // ── Datos de la categoría ────────────────────────────────
    private Integer categoriaId;
    private String categoriaNombre;

    // ── Datos del vendedor ───────────────────────────────────
    private Integer vendedorId;
    private String nombreTienda;
    private String slugTienda;

    // ── Imágenes ─────────────────────────────────────────────
    private List<ImagenProductoDTO> imagenes;

    // ── Variantes ────────────────────────────────────────────
    private List<VarianteProductoDTO> variantes;

    // ── DTO interno para imágenes ────────────────────────────
    @Data
    public static class ImagenProductoDTO {
        private Integer id;
        private String url;
        private Boolean esPrincipal;
        private Integer orden;
    }

    // ── DTO interno para variantes ───────────────────────────
    @Data
    public static class VarianteProductoDTO {
        private Integer id;
        private String sku;
        private BigDecimal precio;
        private Integer stock;
        private Boolean estaActiva;
        private List<ValorAtributoDTO> valoresAtributo;
    }

    // ── DTO interno para valores de atributo ─────────────────
    @Data
    public static class ValorAtributoDTO {
        private Integer id;
        private String atributo;
        private String valor;
    }
}