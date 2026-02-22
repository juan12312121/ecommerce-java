package com.ecommerce.backend.modules.cupon;

import com.ecommerce.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cupones")
@RequiredArgsConstructor
@Tag(name = "Cupones", description = "Gestión de cupones y descuentos")
public class CuponController {

    private final CuponService cuponService;

    // ── GET /cupones/validar?codigo=XXX — validar cupón ──────
    @GetMapping("/validar")
    @Operation(summary = "Validar cupón por código")
    public ResponseEntity<ApiResponse<Cupon>> validar(
            @RequestParam String codigo,
            @RequestParam java.math.BigDecimal monto) {
        return ResponseEntity.ok(ApiResponse.exito(
                cuponService.validarCupon(codigo, monto)));
    }

    // ── GET /cupones/mis-cupones — cupones del vendedor ───────
    @GetMapping("/mis-cupones")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    @Operation(summary = "Listar mis cupones (vendedor)")
    public ResponseEntity<ApiResponse<List<Cupon>>> misCupones(
            @AuthenticationPrincipal UserDetails userDetails) {
        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito(cuponService.listarMisCupones(usuarioId)));
    }

    // ── GET /cupones/globales — cupones globales (ADMIN) ──────
    @GetMapping("/globales")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar cupones globales (admin)")
    public ResponseEntity<ApiResponse<List<Cupon>>> globales() {
        return ResponseEntity.ok(ApiResponse.exito(cuponService.listarGlobales()));
    }

    // ── POST /cupones — crear cupón de vendedor ───────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    @Operation(summary = "Crear cupón de tienda")
    public ResponseEntity<ApiResponse<Cupon>> crear(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CuponRequest request) {
        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Cupón creado correctamente",
                        cuponService.crear(request, usuarioId)));
    }

    // ── POST /cupones/global — crear cupón global (ADMIN) ─────
    @PostMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cupón global (admin)")
    public ResponseEntity<ApiResponse<Cupon>> crearGlobal(
            @Valid @RequestBody CuponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Cupón global creado",
                        cuponService.crearGlobal(request)));
    }

    // ── DELETE /cupones/{id} — desactivar cupón ───────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMIN')")
    @Operation(summary = "Desactivar cupón")
    public ResponseEntity<ApiResponse<Void>> desactivar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        boolean esAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        cuponService.desactivar(id, usuarioId, esAdmin);
        return ResponseEntity.ok(ApiResponse.exito("Cupón desactivado correctamente"));
    }

    // ── Helper ────────────────────────────────────────────────
    private Integer obtenerUsuarioId(UserDetails userDetails) {
        return ((com.ecommerce.backend.modules.usuario.Usuario) userDetails).getId();
    }
}