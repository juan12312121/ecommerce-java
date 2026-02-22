package com.ecommerce.backend.modules.cupon;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CuponRequest {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 50, message = "El código no puede superar 50 caracteres")
    private String codigo;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "PORCENTAJE|MONTO_FIJO", message = "El tipo debe ser PORCENTAJE o MONTO_FIJO")
    private String tipo;

    @NotNull(message = "El valor es obligatorio")
    @DecimalMin(value = "0.01", message = "El valor debe ser mayor a 0")
    private BigDecimal valor;

    private BigDecimal montoMinimoOrden;

    private Integer maximoUsos;

    private LocalDateTime iniciaEn;

    private LocalDateTime expiraEn;
}