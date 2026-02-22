package com.ecommerce.backend.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private Integer id;
    private String correo;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String telefono;
    private String avatarUrl;
    private Boolean estaActivo;
    private Set<String> roles; // ["ADMIN", "COMPRADOR"]
    private LocalDateTime creadoEn;
}