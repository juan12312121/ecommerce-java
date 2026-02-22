package com.ecommerce.backend.modules.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    // ── Todas las direcciones de un usuario ──────────────────
    List<Direccion> findByUsuarioId(Integer usuarioId);

    // ── Dirección predeterminada ─────────────────────────────
    List<Direccion> findByUsuarioIdAndEsPredeterminadaTrue(Integer usuarioId);
}