package com.ecommerce.backend.modules.catalogo.producto.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar 200 caracteres")
    private String nombre;

    @NotBlank(message = "El slug es obligatorio")
    @Size(max = 200, message = "El slug no puede superar 200 caracteres")
    private String slug;

    private String descripcion;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private BigDecimal precioBase;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    // ── Variantes del producto ───────────────────────────────
    @NotEmpty(message = "Debe agregar al menos una variante")
    @Valid
    private List<VarianteRequest> variantes;

    // ── DTO interno para variantes ───────────────────────────
    @Data
    public static class VarianteRequest {

        @NotBlank(message = "El SKU es obligatorio")
        private String sku;

        @NotNull(message = "El precio de la variante es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        private BigDecimal precio;

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        private Integer stock;

        // IDs de los valores de atributo (ej: id de "XL", id de "Rojo")
        private List<Integer> valoresAtributoIds;
    }
}