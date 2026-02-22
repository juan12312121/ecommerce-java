package com.ecommerce.backend.modules.resena;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    // ── Reseñas de un producto ───────────────────────────────
    Page<Resena> findByProductoIdOrderByCreadoEnDesc(Integer productoId, Pageable pageable);

    // ── Reseñas de un usuario ────────────────────────────────
    Page<Resena> findByUsuarioIdOrderByCreadoEnDesc(Integer usuarioId, Pageable pageable);

    // ── Verificar si ya reseñó ese item ─────────────────────
    boolean existsByUsuarioIdAndItemOrdenId(Integer usuarioId, Integer itemOrdenId);

    // ── Reseña con detalles ──────────────────────────────────
    @Query("SELECT r FROM Resena r " +
            "LEFT JOIN FETCH r.imagenes " +
            "LEFT JOIN FETCH r.usuario " +
            "WHERE r.producto.id = :productoId")
    Page<Resena> findByProductoIdConDetalles(
            @Param("productoId") Integer productoId, Pageable pageable);
}