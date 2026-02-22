package com.ecommerce.backend.modules.amonestaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevisarReporteTiendaRequest {

    @NotBlank
    @Pattern(regexp = "REVISADO|DESESTIMADO", message = "Estado debe ser REVISADO o DESESTIMADO")
    private String estado;

    private String notaResolucion;
}