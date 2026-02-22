package com.ecommerce.backend.modules.catalogo.variante;

import com.ecommerce.backend.modules.catalogo.variante.dto.VarianteProductoRequest;
import com.ecommerce.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos/{productoId}/variantes")
@RequiredArgsConstructor
@Tag(name = "Variantes", description = "Gestión de variantes de productos")
public class VarianteProductoController {

    private final VarianteProductoService varianteService;

    // ── GET /productos/{productoId}/variantes ────────────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar variantes activas de un producto")
    public ResponseEntity<ApiResponse<List<VarianteProducto>>> listar(
            @PathVariable Integer productoId) {
        return ResponseEntity.ok(ApiResponse.exito(varianteService.listarPorProducto(productoId)));
    }

    // ── GET /productos/{productoId}/variantes/{id} ───────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener variante por ID")
    public ResponseEntity<ApiResponse<VarianteProducto>> obtener(
            @PathVariable Integer productoId,
            @PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(varianteService.obtenerPorId(id)));
    }

    // ── POST /productos/{productoId}/variantes ───────────────
    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Crear variante (vendedor)")
    public ResponseEntity<ApiResponse<VarianteProducto>> crear(
            @PathVariable Integer productoId,
            @Valid @RequestBody VarianteProductoRequest request) {
        VarianteProducto nueva = varianteService.crear(productoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Variante creada correctamente", nueva));
    }

    // ── PUT /productos/{productoId}/variantes/{id} ───────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Actualizar variante (vendedor)")
    public ResponseEntity<ApiResponse<VarianteProducto>> actualizar(
            @PathVariable Integer productoId,
            @PathVariable Integer id,
            @Valid @RequestBody VarianteProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.exito("Variante actualizada", varianteService.actualizar(id, request)));
    }

    // ── PATCH /productos/{productoId}/variantes/{id}/stock ───
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Actualizar stock de variante (vendedor)")
    public ResponseEntity<ApiResponse<VarianteProducto>> actualizarStock(
            @PathVariable Integer productoId,
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        Integer nuevoStock = body.get("stock");
        return ResponseEntity
                .ok(ApiResponse.exito("Stock actualizado", varianteService.actualizarStock(id, nuevoStock)));
    }

    // ── DELETE /productos/{productoId}/variantes/{id} ────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Desactivar variante (vendedor)")
    public ResponseEntity<ApiResponse<Void>> desactivar(
            @PathVariable Integer productoId,
            @PathVariable Integer id) {
        varianteService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.exito("Variante desactivada correctamente"));
    }
}