package com.ecommerce.backend.modules.usuario;

import com.ecommerce.backend.modules.usuario.dto.UsuarioDTO;
import com.ecommerce.backend.modules.usuario.dto.UsuarioUpdateRequest;
import com.ecommerce.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ── GET /usuarios/perfil — ver mi perfil ─────────────────
    @GetMapping("/perfil")
    @Operation(summary = "Ver mi perfil")
    public ResponseEntity<ApiResponse<UsuarioDTO>> verMiPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {

        UsuarioDTO dto = usuarioService.obtenerPorCorreo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.exito(dto));
    }

    // ── PUT /usuarios/perfil — actualizar mi perfil ──────────
    @PutMapping("/perfil")
    @Operation(summary = "Actualizar mi perfil")
    public ResponseEntity<ApiResponse<UsuarioDTO>> actualizarMiPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UsuarioUpdateRequest request) {

        UsuarioDTO usuarioActual = usuarioService.obtenerPorCorreo(userDetails.getUsername());
        UsuarioDTO dto = usuarioService.actualizarPerfil(usuarioActual.getId(), request);
        return ResponseEntity.ok(ApiResponse.exito("Perfil actualizado correctamente", dto));
    }

    // ── GET /usuarios — listar todos (solo ADMIN) ────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios (admin)")
    public ResponseEntity<ApiResponse<List<UsuarioDTO>>> listarTodos() {
        List<UsuarioDTO> usuarios = usuarioService.listarTodos();
        return ResponseEntity.ok(ApiResponse.exito(usuarios));
    }

    // ── GET /usuarios/{id} — ver usuario por ID (solo ADMIN) ─
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver usuario por ID (admin)")
    public ResponseEntity<ApiResponse<UsuarioDTO>> verPorId(@PathVariable Integer id) {
        UsuarioDTO dto = usuarioService.obtenerPorId(id);
        return ResponseEntity.ok(ApiResponse.exito(dto));
    }

    // ── PATCH /usuarios/{id}/desactivar (solo ADMIN) ─────────
    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar usuario (admin)")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Integer id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(ApiResponse.exito("Usuario desactivado correctamente"));
    }

    // ── PATCH /usuarios/{id}/activar (solo ADMIN) ────────────
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar usuario (admin)")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable Integer id) {
        usuarioService.activarUsuario(id);
        return ResponseEntity.ok(ApiResponse.exito("Usuario activado correctamente"));
    }
}