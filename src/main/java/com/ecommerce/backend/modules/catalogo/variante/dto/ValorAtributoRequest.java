package com.ecommerce.backend.modules.catalogo.variante.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ValorAtributoRequest {

    @NotBlank(message = "El valor es obligatorio")
    @Size(max = 100, message = "El valor no puede exceder 100 caracteres")
    private String valor;
}