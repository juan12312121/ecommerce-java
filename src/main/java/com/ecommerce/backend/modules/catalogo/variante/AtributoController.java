package com.ecommerce.backend.modules.catalogo.variante;

import com.ecommerce.backend.modules.catalogo.variante.dto.*;
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
@RequestMapping("/atributos")
@RequiredArgsConstructor
@Tag(name = "Atributos", description = "Gestión de atributos y sus valores")
public class AtributoController {

    private final AtributoService atributoService;

    // ── GET /atributos — listar todos (autenticado) ──────────
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todos los atributos")
    public ResponseEntity<ApiResponse<List<Atributo>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.exito(atributoService.listarTodos()));
    }

    // ── GET /atributos/{id} ──────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener atributo por ID")
    public ResponseEntity<ApiResponse<Atributo>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(atributoService.obtenerPorId(id)));
    }

    // ── POST /atributos — crear (VENDEDOR) ───────────────────
    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Crear atributo (vendedor)")
    public ResponseEntity<ApiResponse<Atributo>> crear(
            @Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Atributo creado correctamente", atributoService.crear(request)));
    }

    // ── PUT /atributos/{id} — actualizar (VENDEDOR) ──────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Actualizar atributo (vendedor)")
    public ResponseEntity<ApiResponse<Atributo>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody AtributoRequest request) {
        return ResponseEntity.ok(ApiResponse.exito("Atributo actualizado", atributoService.actualizar(id, request)));
    }

    // ── DELETE /atributos/{id} — eliminar (VENDEDOR) ─────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Eliminar atributo (vendedor)")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        atributoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.exito("Atributo eliminado correctamente"));
    }

    // ── GET /atributos/{id}/valores ──────────────────────────
    @GetMapping("/{id}/valores")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar valores de un atributo")
    public ResponseEntity<ApiResponse<List<ValorAtributo>>> listarValores(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(atributoService.listarValores(id)));
    }

    // ── POST /atributos/{id}/valores — agregar valor ─────────
    @PostMapping("/{id}/valores")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Agregar valor a un atributo (vendedor)")
    public ResponseEntity<ApiResponse<ValorAtributo>> agregarValor(
            @PathVariable Integer id,
            @Valid @RequestBody ValorAtributoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Valor agregado correctamente", atributoService.agregarValor(id, request)));
    }

    // ── DELETE /atributos/valores/{valorId} ──────────────────
    @DeleteMapping("/valores/{valorId}")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Eliminar valor de atributo (vendedor)")
    public ResponseEntity<ApiResponse<Void>> eliminarValor(@PathVariable Integer valorId) {
        atributoService.eliminarValor(valorId);
        return ResponseEntity.ok(ApiResponse.exito("Valor eliminado correctamente"));
    }
}