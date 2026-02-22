package com.ecommerce.backend.modules.catalogo.variante.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class VarianteProductoRequest {

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 100, message = "El SKU no puede exceder 100 caracteres")
    private String sku;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock = 0;

    private Boolean estaActiva = true;

    @NotEmpty(message = "Debe incluir al menos un valor de atributo")
    private List<Integer> valoresAtributoIds; // IDs de ValorAtributo
}