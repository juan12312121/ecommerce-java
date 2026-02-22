package com.ecommerce.backend.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> contenido;
    private int paginaActual;
    private int totalPaginas;
    private long totalElementos;
    private int tamanioPagina;
    private boolean esUltima;
    private boolean esPrimera;

    // ── Construir desde un Page de Spring ────────────────────
    public static <T> PageResponse<T> desde(Page<T> page) {
        return PageResponse.<T>builder()
                .contenido(page.getContent())
                .paginaActual(page.getNumber())
                .totalPaginas(page.getTotalPages())
                .totalElementos(page.getTotalElements())
                .tamanioPagina(page.getSize())
                .esUltima(page.isLast())
                .esPrimera(page.isFirst())
                .build();
    }
}