package com.ecommerce.backend.modules.pago;

import com.ecommerce.backend.shared.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    // ── Pago de una orden ────────────────────────────────────
    Optional<Pago> findByOrdenId(Integer ordenId);

    // ── Buscar por ID externo del proveedor ──────────────────
    Optional<Pago> findByIdPagoProveedor(String idPagoProveedor);

    // ── Pagos por estado ─────────────────────────────────────
    List<Pago> findByEstado(EstadoPago estado);
}