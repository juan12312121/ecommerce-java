package com.ecommerce.backend.modules.vendedor;

import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.ApiResponse;
import com.ecommerce.backend.shared.enums.EstadoVendedor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tiendas")
@RequiredArgsConstructor
@Tag(name = "Vendedores", description = "Gestión de tiendas y vendedores")
public class VendedorController {

    private final VendedorService vendedorService;
    private final UsuarioRepository usuarioRepository;

    // ── GET /tiendas — listar tiendas aprobadas (público) ────
    @GetMapping
    @Operation(summary = "Listar todas las tiendas aprobadas")
    public ResponseEntity<ApiResponse<List<Vendedor>>> listarAprobadas() {
        return ResponseEntity.ok(ApiResponse.exito(vendedorService.listarAprobados()));
    }

    // ── GET /tiendas/{slug} — ver tienda (público) ────────────
    @GetMapping("/{slug}")
    @Operation(summary = "Ver detalle de tienda por slug")
    public ResponseEntity<ApiResponse<Vendedor>> obtenerPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.exito(vendedorService.obtenerPorSlug(slug)));
    }

    // ── GET /tiendas/mi-tienda — ver mi tienda (VENDEDOR) ────
    @GetMapping("/mi-tienda")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    @Operation(summary = "Ver mi tienda")
    public ResponseEntity<ApiResponse<Vendedor>> miTienda(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer usuarioId = extraerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(vendedorService.obtenerMiTienda(usuarioId)));
    }

    // ── POST /tiendas/registrar — registrar tienda ────────────
    @PostMapping("/registrar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Registrar nueva tienda")
    public ResponseEntity<ApiResponse<Vendedor>> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VendedorRequest request) {
        Integer usuarioId = extraerUsuarioId(userDetails);
        Vendedor vendedor = vendedorService.registrar(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Tienda registrada, pendiente de aprobación", vendedor));
    }

    // ── PUT /tiendas/mi-tienda — actualizar mi tienda ─────────
    @PutMapping("/mi-tienda")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    @Operation(summary = "Actualizar mi tienda")
    public ResponseEntity<ApiResponse<Vendedor>> actualizar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VendedorRequest request) {
        Integer usuarioId = extraerUsuarioId(userDetails);
        Vendedor vendedor = vendedorService.actualizar(request, usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Tienda actualizada correctamente", vendedor));
    }

    // ── PATCH /tiendas/{id}/aprobar — aprobar tienda (ADMIN) ─
    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar tienda (admin)")
    public ResponseEntity<ApiResponse<Vendedor>> aprobar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito("Tienda aprobada", vendedorService.aprobar(id)));
    }

    // ── PATCH /tiendas/{id}/suspender — suspender (ADMIN) ────
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspender tienda (admin)")
    public ResponseEntity<ApiResponse<Vendedor>> suspender(
            @PathVariable Integer id,
            @RequestParam String razon) {
        return ResponseEntity.ok(ApiResponse.exito("Tienda suspendida", vendedorService.suspender(id, razon)));
    }

    // ── PATCH /tiendas/{id}/reactivar — reactivar (ADMIN) ────
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivar tienda (admin)")
    public ResponseEntity<ApiResponse<Vendedor>> reactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito("Tienda reactivada", vendedorService.reactivar(id)));
    }

    // ── GET /tiendas/admin/estado — listar por estado (ADMIN) ─
    @GetMapping("/admin/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar tiendas por estado (admin)")
    public ResponseEntity<ApiResponse<List<Vendedor>>> listarPorEstado(
            @RequestParam EstadoVendedor estado) {
        return ResponseEntity.ok(ApiResponse.exito(vendedorService.listarPorEstado(estado)));
    }

    // ── Helper: correo → usuarioId (igual que ProductoController) ──
    private Integer extraerUsuarioId(UserDetails userDetails) {
        String correo = userDetails.getUsername();
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
        return usuario.getId();
    }
}