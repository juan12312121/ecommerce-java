package com.ecommerce.backend.modules.catalogo.producto;

import com.ecommerce.backend.modules.catalogo.producto.dto.ProductoDTO;
import com.ecommerce.backend.modules.catalogo.producto.dto.ProductoRequest;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.ApiResponse;
import com.ecommerce.backend.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Productos", description = "Gestión del catálogo de productos")
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

        private final ProductoService productoService;
        private final UsuarioRepository usuarioRepository;

        // ── GET /api/productos ───────────────────────────────────
        @Operation(summary = "Listar productos activos")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<ProductoDTO>>> listar(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size,
                        @RequestParam(defaultValue = "creadoEn") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);
                return ResponseEntity.ok(ApiResponse.exito("Productos obtenidos correctamente",
                                productoService.listarActivos(pageable)));
        }

        // ── GET /api/productos/buscar ────────────────────────────
        @Operation(summary = "Buscar productos con filtros")
        @GetMapping("/buscar")
        public ResponseEntity<ApiResponse<PageResponse<ProductoDTO>>> buscar(
                        @RequestParam(required = false) Integer categoriaId,
                        @RequestParam(required = false) Integer vendedorId,
                        @RequestParam(required = false) BigDecimal precioMin,
                        @RequestParam(required = false) BigDecimal precioMax,
                        @RequestParam(required = false) String termino,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());
                return ResponseEntity.ok(ApiResponse.exito("Búsqueda completada",
                                productoService.buscarConFiltros(categoriaId, vendedorId, precioMin, precioMax, termino,
                                                pageable)));
        }

        // ── GET /api/productos/destacados ────────────────────────
        @Operation(summary = "Obtener productos destacados")
        @GetMapping("/destacados")
        public ResponseEntity<ApiResponse<List<ProductoDTO>>> destacados() {
                return ResponseEntity.ok(
                                ApiResponse.exito("Productos destacados", productoService.obtenerDestacados()));
        }

        // ── GET /api/productos/slug/{slug} ───────────────────────
        @Operation(summary = "Obtener producto por slug")
        @GetMapping("/slug/{slug}")
        public ResponseEntity<ApiResponse<ProductoDTO>> porSlug(@PathVariable String slug) {
                return ResponseEntity.ok(
                                ApiResponse.exito("Producto encontrado", productoService.obtenerPorSlug(slug)));
        }

        // ── GET /api/productos/{id} ──────────────────────────────
        @Operation(summary = "Obtener producto por ID")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<ProductoDTO>> porId(@PathVariable Integer id) {
                return ResponseEntity.ok(
                                ApiResponse.exito("Producto encontrado", productoService.obtenerPorId(id)));
        }

        // ── GET /api/productos/mis-productos ─────────────────────
        @Operation(summary = "Listar mis productos")
        @GetMapping("/mis-productos")
        @PreAuthorize("hasRole('VENDEDOR')")
        public ResponseEntity<ApiResponse<PageResponse<ProductoDTO>>> misProductos(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {

                Integer usuarioId = extraerUsuarioId(userDetails);
                Pageable pageable = PageRequest.of(page, size, Sort.by("creadoEn").descending());
                return ResponseEntity.ok(ApiResponse.exito("Tus productos",
                                productoService.listarPorVendedor(usuarioId, pageable)));
        }

        // ── POST /api/productos ──────────────────────────────────
        @Operation(summary = "Crear producto")
        @PostMapping
        @PreAuthorize("hasRole('VENDEDOR')")
        public ResponseEntity<ApiResponse<ProductoDTO>> crear(
                        @Valid @RequestBody ProductoRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = extraerUsuarioId(userDetails);
                ProductoDTO creado = productoService.crear(request, usuarioId);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.exito("Producto creado correctamente", creado));
        }

        // ── PUT /api/productos/{id} ──────────────────────────────
        @Operation(summary = "Actualizar producto")
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('VENDEDOR')")
        public ResponseEntity<ApiResponse<ProductoDTO>> actualizar(
                        @PathVariable Integer id,
                        @Valid @RequestBody ProductoRequest request,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = extraerUsuarioId(userDetails);
                return ResponseEntity.ok(ApiResponse.exito("Producto actualizado correctamente",
                                productoService.actualizar(id, request, usuarioId)));
        }

        // ── DELETE /api/productos/{id} ───────────────────────────
        @Operation(summary = "Desactivar producto")
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('VENDEDOR')")
        public ResponseEntity<ApiResponse<Void>> desactivar(
                        @PathVariable Integer id,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = extraerUsuarioId(userDetails);
                productoService.desactivar(id, usuarioId);
                return ResponseEntity.ok(ApiResponse.<Void>exito("Producto desactivado correctamente"));
        }

        // ── Helper: correo → usuarioId ───────────────────────────
        // Tu UserDetails usa correo como username (no el ID)
        private Integer extraerUsuarioId(UserDetails userDetails) {
                String correo = userDetails.getUsername();
                Usuario usuario = usuarioRepository.findByCorreo(correo)
                                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));
                return usuario.getId();
        }

        // ── PATCH /api/productos/{id}/estado ────────────────────────
        @Operation(summary = "Cambiar estado del producto")
        @PatchMapping("/{id}/estado")
        @PreAuthorize("hasRole('VENDEDOR')")
        public ResponseEntity<ApiResponse<ProductoDTO>> cambiarEstado(
                        @PathVariable Integer id,
                        @RequestParam String estado,
                        @AuthenticationPrincipal UserDetails userDetails) {

                Integer usuarioId = extraerUsuarioId(userDetails);
                return ResponseEntity.ok(ApiResponse.exito("Estado actualizado",
                                productoService.cambiarEstado(id, estado, usuarioId)));
        }
}