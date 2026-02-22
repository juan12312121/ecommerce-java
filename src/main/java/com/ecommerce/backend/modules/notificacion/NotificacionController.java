package com.ecommerce.backend.modules.notificacion;

import com.ecommerce.backend.shared.dto.ApiResponse;
import com.ecommerce.backend.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Notificaciones", description = "Gestión de notificaciones del usuario")
public class NotificacionController {

    private final NotificacionService notificacionService;

    // ── GET /notificaciones — todas paginadas ─────────────────
    @GetMapping
    @Operation(summary = "Listar todas mis notificaciones")
    public ResponseEntity<ApiResponse<PageResponse<Notificacion>>> listarTodas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "15") int tamanio) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        Pageable pageable = PageRequest.of(pagina, tamanio);
        return ResponseEntity.ok(ApiResponse.exito(
                notificacionService.listarTodas(usuarioId, pageable)));
    }

    // ── GET /notificaciones/no-leidas ─────────────────────────
    @GetMapping("/no-leidas")
    @Operation(summary = "Listar notificaciones no leídas")
    public ResponseEntity<ApiResponse<PageResponse<Notificacion>>> noLeidas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "15") int tamanio) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        Pageable pageable = PageRequest.of(pagina, tamanio);
        return ResponseEntity.ok(ApiResponse.exito(
                notificacionService.listarNoLeidas(usuarioId, pageable)));
    }

    // ── GET /notificaciones/contador ──────────────────────────
    @GetMapping("/contador")
    @Operation(summary = "Contar notificaciones no leídas")
    public ResponseEntity<ApiResponse<Long>> contador(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(
                notificacionService.contarNoLeidas(usuarioId)));
    }

    // ── PATCH /notificaciones/{id}/leer ──────────────────────
    @PatchMapping("/{id}/leer")
    @Operation(summary = "Marcar notificación como leída")
    public ResponseEntity<ApiResponse<Void>> marcarComoLeida(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        notificacionService.marcarComoLeida(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Notificación marcada como leída"));
    }

    // ── PATCH /notificaciones/leer-todas ─────────────────────
    @PatchMapping("/leer-todas")
    @Operation(summary = "Marcar todas como leídas")
    public ResponseEntity<ApiResponse<Void>> marcarTodasComoLeidas(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Todas las notificaciones marcadas como leídas"));
    }

    // ── DELETE /notificaciones/{id} ───────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar notificación")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        notificacionService.eliminar(id, usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Notificación eliminada"));
    }

    // ── Helper ────────────────────────────────────────────────
    private Integer obtenerUsuarioId(UserDetails userDetails) {
        return ((com.ecommerce.backend.modules.usuario.Usuario) userDetails).getId();
    }
}