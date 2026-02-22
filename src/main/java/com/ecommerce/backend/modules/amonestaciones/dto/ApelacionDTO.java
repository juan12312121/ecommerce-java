package com.ecommerce.backend.modules.amonestaciones.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApelacionDTO {

    private Integer id;
    private Integer vendedorId;
    private String vendedorNombre;
    private Integer advertenciaId;
    private String descripcion;
    private String estado;
    private Integer revisadoPorId;
    private String revisadoPorNombre;
    private String respuestaAdmin;
    private LocalDateTime creadoEn;
    private LocalDateTime resueltoEn;
}