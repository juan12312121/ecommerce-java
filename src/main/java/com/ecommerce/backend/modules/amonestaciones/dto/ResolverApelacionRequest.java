package com.ecommerce.backend.modules.amonestaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResolverApelacionRequest {

    @NotBlank
    @Pattern(regexp = "APROBADA|RECHAZADA", message = "Estado debe ser APROBADA o RECHAZADA")
    private String estado;

    @NotBlank(message = "La respuesta del admin es obligatoria")
    private String respuestaAdmin;
}