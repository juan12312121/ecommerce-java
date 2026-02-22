package com.ecommerce.backend.modules.orden;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    // ── Órdenes de un usuario paginadas ──────────────────────
    Page<Orden> findByUsuarioIdOrderByCreadoEnDesc(Integer usuarioId, Pageable pageable);

    // ── Órdenes por estado ───────────────────────────────────
    Page<Orden> findByEstadoOrderByCreadoEnDesc(String estado, Pageable pageable);

    // ── Orden con todos sus detalles ─────────────────────────
    @Query("SELECT o FROM Orden o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.variante v " +
            "LEFT JOIN FETCH v.producto " +
            "LEFT JOIN FETCH o.ordenesVendedor " +
            "WHERE o.id = :id")
    Optional<Orden> findByIdConDetalles(@Param("id") Integer id);

    // ── Órdenes de un vendedor específico ────────────────────
    @Query("SELECT o FROM Orden o " +
            "INNER JOIN o.items i " +
            "WHERE i.vendedor.id = :vendedorId " +
            "ORDER BY o.creadoEn DESC")
    Page<Orden> findByVendedorId(@Param("vendedorId") Integer vendedorId, Pageable pageable);
}