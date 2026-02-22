package com.ecommerce.backend.modules.notificacion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // ── Notificaciones de un usuario paginadas ───────────────
    Page<Notificacion> findByUsuarioIdOrderByCreadoEnDesc(Integer usuarioId, Pageable pageable);

    // ── Solo no leídas ───────────────────────────────────────
    Page<Notificacion> findByUsuarioIdAndLeidaFalseOrderByCreadoEnDesc(
            Integer usuarioId, Pageable pageable);

    // ── Contar no leídas ─────────────────────────────────────
    long countByUsuarioIdAndLeidaFalse(Integer usuarioId);

    // ── Marcar todas como leídas ─────────────────────────────
    @Modifying
    @Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.id = :usuarioId")
    void marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId);
}