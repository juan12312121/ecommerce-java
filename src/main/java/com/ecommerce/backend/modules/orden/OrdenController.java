package com.ecommerce.backend.modules.orden;

import com.ecommerce.backend.modules.orden.dto.OrdenDTO;
import com.ecommerce.backend.modules.orden.dto.OrdenRequest;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.ApiResponse;
import com.ecommerce.backend.shared.dto.PageResponse;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes y checkout")
public class OrdenController {

        private final OrdenService ordenService;
        private final UsuarioRepository usuarioRepository;

        // ── POST /ordenes/checkout — crear orden ─────────────────
        @PostMapping("/checkout")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Crear orden desde el carrito (checkout)")
        public ResponseEntity<ApiResponse<OrdenDTO>> checkout(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @Valid @RequestBody OrdenRequest request) {

                Integer usuarioId = obtenerUsuarioId(userDetails);
                OrdenDTO dto = ordenService.crearDesdeCarrito(usuarioId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.exito("Orden creada correctamente", dto));
        }

        // ── GET /ordenes/mis-ordenes — mis órdenes (comprador) ───
        @GetMapping("/mis-ordenes")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Ver mis órdenes")
        public ResponseEntity<ApiResponse<PageResponse<OrdenDTO>>> misOrdenes(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "10") int tamanio) {

                Integer usuarioId = obtenerUsuarioId(userDetails);
                Pageable pageable = PageRequest.of(pagina, tamanio);
                return ResponseEntity.ok(ApiResponse.exito(
                                ordenService.misOrdenes(usuarioId, pageable)));
        }

        // ── GET /ordenes/{id} — detalle de orden ─────────────────
        @GetMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Ver detalle de una orden")
        public ResponseEntity<ApiResponse<OrdenDTO>> detalle(
                        @PathVariable Integer id,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = obtenerUsuarioId(userDetails);
                return ResponseEntity.ok(ApiResponse.exito(
                                ordenService.obtenerDetalle(id, usuarioId)));
        }

        // ── PATCH /ordenes/{id}/cancelar — cancelar orden ────────
        @PatchMapping("/{id}/cancelar")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Cancelar una orden pendiente")
        public ResponseEntity<ApiResponse<OrdenDTO>> cancelar(
                        @PathVariable Integer id,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = obtenerUsuarioId(userDetails);
                return ResponseEntity.ok(ApiResponse.exito(
                                "Orden cancelada", ordenService.cancelar(id, usuarioId)));
        }

        // ── GET /ordenes/vendedor/mis-ordenes — órdenes del vendedor
        @GetMapping("/vendedor/mis-ordenes")
        @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
        @Operation(summary = "Ver órdenes de mi tienda (vendedor)")
        public ResponseEntity<ApiResponse<PageResponse<OrdenDTO>>> ordenesVendedor(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "10") int tamanio) {

                Integer usuarioId = obtenerUsuarioId(userDetails);
                Pageable pageable = PageRequest.of(pagina, tamanio);
                return ResponseEntity.ok(ApiResponse.exito(
                                ordenService.ordenesPorVendedor(usuarioId, pageable)));
        }

        // ── GET /ordenes/admin — todas las órdenes (admin) ───────
        @GetMapping("/admin")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Listar todas las órdenes (admin)")
        public ResponseEntity<ApiResponse<PageResponse<OrdenDTO>>> todasLasOrdenes(
                        @RequestParam(required = false) String estado,
                        @RequestParam(defaultValue = "0") int pagina,
                        @RequestParam(defaultValue = "10") int tamanio) {

                Pageable pageable = PageRequest.of(pagina, tamanio);
                return ResponseEntity.ok(ApiResponse.exito(
                                ordenService.todasLasOrdenes(estado, pageable)));
        }

        // ── PATCH /ordenes/{id}/estado — cambiar estado (admin/vendedor) ──
        @PatchMapping("/{id}/estado")
        @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
        @Operation(summary = "Actualizar estado de orden (admin/vendedor)")
        public ResponseEntity<ApiResponse<OrdenDTO>> actualizarEstado(
                        @PathVariable Integer id,
                        @RequestParam String estado) {

                return ResponseEntity.ok(ApiResponse.exito(
                                "Estado actualizado", ordenService.actualizarEstado(id, estado)));
        }

        // ── Helper ────────────────────────────────────────────────
        // ✅ CORRECTO: getUsername() devuelve el correo del JWT → busca en BD
        // ❌ NUNCA: (Usuario) userDetails → ClassCastException con DevTools
        private Integer obtenerUsuarioId(UserDetails userDetails) {
                String correo = userDetails.getUsername();
                return usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", correo))
                                .getId();
        }
}