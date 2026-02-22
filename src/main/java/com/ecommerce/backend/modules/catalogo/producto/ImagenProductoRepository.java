package com.ecommerce.backend.modules.catalogo.producto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Integer> {

    // ── Imágenes de un producto ordenadas ────────────────────
    List<ImagenProducto> findByProductoIdOrderByOrdenAsc(Integer productoId);

    // ── Imagen principal de un producto ─────────────────────
    Optional<ImagenProducto> findByProductoIdAndEsPrincipalTrue(Integer productoId);

    // ── Contar imágenes de un producto ───────────────────────
    long countByProductoId(Integer productoId);

    // ── Eliminar todas las imágenes de un producto ───────────
    void deleteByProductoId(Integer productoId);
}