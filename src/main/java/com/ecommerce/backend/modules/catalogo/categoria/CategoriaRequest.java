package com.ecommerce.backend.modules.catalogo.categoria;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El slug es obligatorio")
    private String slug;

    private String imagenUrl;

    private Integer categoriaPadreId;
}