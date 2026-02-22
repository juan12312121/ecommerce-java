package com.ecommerce.backend.modules.resena;

import com.ecommerce.backend.modules.resena.dto.ResenaDTO;
import com.ecommerce.backend.modules.resena.dto.ResenaRequest;
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
@RequestMapping("/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Gestión de reseñas y calificaciones")
public class ResenaController {

    private final ResenaService resenaService;
    private final UsuarioRepository usuarioRepository; // ← agregado

    // ── GET /resenas/producto/{id} — reseñas del producto ────
    @GetMapping("/producto/{productoId}")
    @Operation(summary = "Listar reseñas de un producto")
    public ResponseEntity<ApiResponse<PageResponse<ResenaDTO>>> listarPorProducto(
            @PathVariable Integer productoId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {

        Pageable pageable = PageRequest.of(pagina, tamanio);
        return ResponseEntity.ok(ApiResponse.exito(
                resenaService.listarPorProducto(productoId, pageable)));
    }

    // ── GET /resenas/mis-resenas — mis reseñas ────────────────
    @GetMapping("/mis-resenas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver mis reseñas")
    public ResponseEntity<ApiResponse<PageResponse<ResenaDTO>>> misResenas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamanio) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        Pageable pageable = PageRequest.of(pagina, tamanio);
        return ResponseEntity.ok(ApiResponse.exito(
                resenaService.misResenas(usuarioId, pageable)));
    }

    // ── POST /resenas — crear reseña ──────────────────────────
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Crear reseña de producto comprado")
    public ResponseEntity<ApiResponse<ResenaDTO>> crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ResenaRequest request) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        ResenaDTO dto = resenaService.crear(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Reseña creada correctamente", dto));
    }

    // ── DELETE /resenas/{id} — eliminar reseña ────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Eliminar reseña")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        resenaService.eliminar(id, usuarioId, esAdmin);
        return ResponseEntity.ok(ApiResponse.exito("Reseña eliminada correctamente"));
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