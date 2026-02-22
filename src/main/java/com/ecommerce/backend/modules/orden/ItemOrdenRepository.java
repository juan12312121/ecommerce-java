package com.ecommerce.backend.modules.orden;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemOrdenRepository extends JpaRepository<ItemOrden, Integer> {

    // ── Items de una orden ───────────────────────────────────
    List<ItemOrden> findByOrdenId(Integer ordenId);

    // ── Items de un vendedor ─────────────────────────────────
    List<ItemOrden> findByVendedorId(Integer vendedorId);
}