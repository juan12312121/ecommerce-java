package com.ecommerce.backend.modules.catalogo.categoria;

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

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías del catálogo")
public class CategoriaController {

    private final CategoriaService categoriaService;

    // ── GET /categorias — listar todas (público) ─────────────
    @GetMapping
    @Operation(summary = "Listar todas las categorías activas")
    public ResponseEntity<ApiResponse<List<Categoria>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.exito(categoriaService.listarTodas()));
    }

    // ── GET /categorias/arbol — árbol con subcategorías ──────
    @GetMapping("/arbol")
    @Operation(summary = "Listar categorías padre con sus subcategorías")
    public ResponseEntity<ApiResponse<List<Categoria>>> listarArbol() {
        return ResponseEntity.ok(ApiResponse.exito(categoriaService.listarArbol()));
    }

    // ── GET /categorias/{id} — ver por ID ────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<ApiResponse<Categoria>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(categoriaService.obtenerPorId(id)));
    }

    // ── GET /categorias/slug/{slug} — ver por slug ───────────
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtener categoría por slug")
    public ResponseEntity<ApiResponse<Categoria>> obtenerPorSlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.exito(categoriaService.obtenerPorSlug(slug)));
    }

    // ── GET /categorias/{id}/subcategorias ───────────────────
    @GetMapping("/{id}/subcategorias")
    @Operation(summary = "Listar subcategorías de una categoría")
    public ResponseEntity<ApiResponse<List<Categoria>>> listarSubcategorias(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(categoriaService.listarSubcategorias(id)));
    }

    // ── POST /categorias — crear (VENDEDOR) ──────────────────
    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Crear nueva categoría (vendedor)")
    public ResponseEntity<ApiResponse<Categoria>> crear(
            @Valid @RequestBody CategoriaRequest request) {
        Categoria nueva = categoriaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Categoría creada correctamente", nueva));
    }

    // ── PUT /categorias/{id} — actualizar (VENDEDOR) ─────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Actualizar categoría (vendedor)")
    public ResponseEntity<ApiResponse<Categoria>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequest request) {
        Categoria actualizada = categoriaService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.exito("Categoría actualizada correctamente", actualizada));
    }

    // ── DELETE /categorias/{id} — desactivar (VENDEDOR) ──────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Desactivar categoría (vendedor)")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Integer id) {
        categoriaService.desactivar(id);
        return ResponseEntity.ok(ApiResponse.exito("Categoría desactivada correctamente"));
    }
}