package com.ecommerce.backend.modules.carrito;

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

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Carrito", description = "Gestión del carrito de compras")
public class CarritoController {

    private final CarritoService carritoService;

    // ── GET /carrito — ver mi carrito ────────────────────────
    @GetMapping
    @Operation(summary = "Ver mi carrito")
    public ResponseEntity<ApiResponse<Carrito>> verCarrito(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(carritoService.obtenerCarrito(usuarioId)));
    }

    // ── POST /carrito/items — agregar item ───────────────────
    @PostMapping("/items")
    @Operation(summary = "Agregar producto al carrito")
    public ResponseEntity<ApiResponse<Carrito>> agregar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ItemCarritoRequest request) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(
                "Producto agregado al carrito",
                carritoService.agregar(usuarioId, request)));
    }

    // ── PUT /carrito/items/{itemId} — actualizar cantidad ────
    @PutMapping("/items/{itemId}")
    @Operation(summary = "Actualizar cantidad de un item")
    public ResponseEntity<ApiResponse<Carrito>> actualizarCantidad(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer itemId,
            @RequestParam Integer cantidad) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(
                "Cantidad actualizada",
                carritoService.actualizarCantidad(usuarioId, itemId, cantidad)));
    }

    // ── DELETE /carrito/items/{itemId} — eliminar item ───────
    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Eliminar item del carrito")
    public ResponseEntity<ApiResponse<Carrito>> eliminarItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer itemId) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(
                "Producto eliminado del carrito",
                carritoService.eliminarItem(usuarioId, itemId)));
    }

    // ── DELETE /carrito — vaciar carrito ─────────────────────
    @DeleteMapping
    @Operation(summary = "Vaciar carrito completo")
    public ResponseEntity<ApiResponse<Void>> vaciar(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        carritoService.vaciar(usuarioId);
        return ResponseEntity.ok(ApiResponse.exito("Carrito vaciado correctamente"));
    }

    // ── Helper ────────────────────────────────────────────────
    private Integer obtenerUsuarioId(UserDetails userDetails) {
        return ((com.ecommerce.backend.modules.usuario.Usuario) userDetails).getId();
    }
}