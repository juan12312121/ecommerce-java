package com.ecommerce.backend.modules.carrito;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    // ── Buscar carrito por usuario ───────────────────────────
    Optional<Carrito> findByUsuarioId(Integer usuarioId);

    // ── Carrito con todos sus items y variantes ───────────────
    @Query("SELECT c FROM Carrito c " +
            "LEFT JOIN FETCH c.items i " +
            "LEFT JOIN FETCH i.variante v " +
            "LEFT JOIN FETCH v.producto " +
            "WHERE c.usuario.id = :usuarioId")
    Optional<Carrito> findByUsuarioIdConItems(@Param("usuarioId") Integer usuarioId);
}