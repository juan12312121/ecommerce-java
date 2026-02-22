package com.ecommerce.backend.modules.amonestaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTiendaDTO {

    private Integer id;
    private Integer vendedorId;
    private String vendedorNombre;
    private Integer reportadoPorId;
    private String reportadoPorNombre;
    private String motivo;
    private String descripcion;
    private String estado;
    private Integer revisadoPorId;
    private String revisadoPorNombre;
    private LocalDateTime revisadoEn;
    private String notaResolucion;
    private ApelacionDTO apelacion; // ← campo nuevo
    private LocalDateTime creadoEn;
}