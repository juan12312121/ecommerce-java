package com.ecommerce.backend.modules.vendedor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VendedorRequest {

    @NotBlank(message = "El nombre de la tienda es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombreTienda;

    @NotBlank(message = "El slug de la tienda es obligatorio")
    @Size(max = 150, message = "El slug no puede superar 150 caracteres")
    private String slugTienda;

    private String descripcion;
    private String logoUrl;
    private String bannerUrl;
}