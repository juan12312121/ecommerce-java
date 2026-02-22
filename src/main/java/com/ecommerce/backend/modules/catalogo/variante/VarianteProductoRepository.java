package com.ecommerce.backend.modules.catalogo.variante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Integer> {

    // ── Variantes activas de un producto ─────────────────────
    List<VarianteProducto> findByProductoIdAndEstaActivaTrue(Integer productoId);

    // ── Verificar si existe un SKU ────────────────────────────
    boolean existsBySku(String sku);
}