package com.ecommerce.backend.modules.notificacion;

import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.PageResponse;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Listar todas las notificaciones ─────────────────────
    public PageResponse<Notificacion> listarTodas(Integer usuarioId, Pageable pageable) {
        Page<Notificacion> page = notificacionRepository
                .findByUsuarioIdOrderByCreadoEnDesc(usuarioId, pageable);
        return PageResponse.desde(page);
    }

    // ── Listar solo no leídas ────────────────────────────────
    public PageResponse<Notificacion> listarNoLeidas(Integer usuarioId, Pageable pageable) {
        Page<Notificacion> page = notificacionRepository
                .findByUsuarioIdAndLeidaFalseOrderByCreadoEnDesc(usuarioId, pageable);
        return PageResponse.desde(page);
    }

    // ── Contar no leídas ─────────────────────────────────────
    public long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    // ── Marcar una como leída ────────────────────────────────
    @Transactional
    public void marcarComoLeida(Integer id, Integer usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));

        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Notificación", "id", id);
        }

        notificacion.setLeida(true);
        notificacionRepository.save(notificacion);
    }

    // ── Marcar todas como leídas ─────────────────────────────
    @Transactional
    public void marcarTodasComoLeidas(Integer usuarioId) {
        notificacionRepository.marcarTodasComoLeidas(usuarioId);
    }

    // ── Crear notificación (uso interno desde otros servicios)
    @Transactional
    public void crear(Integer usuarioId, String tipo, String titulo, String mensaje, String url) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .tipo(tipo)
                .titulo(titulo)
                .mensaje(mensaje)
                .urlRedireccion(url)
                .leida(false)
                .build();

        notificacionRepository.save(notificacion);
    }

    // ── Eliminar notificación ────────────────────────────────
    @Transactional
    public void eliminar(Integer id, Integer usuarioId) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación", "id", id));

        if (!notificacion.getUsuario().getId().equals(usuarioId)) {
            throw new ResourceNotFoundException("Notificación", "id", id);
        }

        notificacionRepository.delete(notificacion);
    }
}