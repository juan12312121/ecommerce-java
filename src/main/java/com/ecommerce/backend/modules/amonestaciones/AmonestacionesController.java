package com.ecommerce.backend.modules.amonestaciones;

import com.ecommerce.backend.modules.amonestaciones.dto.*;
import com.ecommerce.backend.modules.usuario.UsuarioService;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.ApiResponse;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
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
@RequestMapping("/amonestaciones")
@RequiredArgsConstructor
@Tag(name = "Amonestaciones", description = "Gestión de apelaciones y reportes de tienda")
public class AmonestacionesController {

    private final AmonestacionesService amonestacionesService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    // ══════════════════════════════════════════════════════════
    // VENDEDOR — ver sus propios reportes y apelar
    // ══════════════════════════════════════════════════════════

    // GET /amonestaciones/mis-reportes
    @GetMapping("/mis-reportes")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Ver mis reportes como vendedor")
    public ResponseEntity<ApiResponse<List<ReporteTiendaDTO>>> misReportes(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito("Tus reportes",
                amonestacionesService.listarReportesPorVendedor(usuarioId)));
    }

    // GET /amonestaciones/mis-apelaciones
    @GetMapping("/mis-apelaciones")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Ver mis apelaciones como vendedor")
    public ResponseEntity<ApiResponse<List<ApelacionDTO>>> misApelaciones(
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito("Tus apelaciones",
                amonestacionesService.listarApelacionesPorVendedor(usuarioId)));
    }

    // POST /amonestaciones/reportes/{id}/apelar
    @PostMapping("/reportes/{id}/apelar")
    @PreAuthorize("hasRole('VENDEDOR')")
    @Operation(summary = "Apelar un reporte resuelto")
    public ResponseEntity<ApiResponse<ApelacionDTO>> apelarReporte(
            @PathVariable Integer id,
            @Valid @RequestBody ApelarReporteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        ApelacionDTO dto = amonestacionesService.apelarReporte(id, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Apelación enviada correctamente", dto));
    }

    // ══════════════════════════════════════════════════════════
    // ADMIN — apelaciones
    // ══════════════════════════════════════════════════════════

    @GetMapping("/apelaciones")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las apelaciones (admin)")
    public ResponseEntity<ApiResponse<List<ApelacionDTO>>> listarApelaciones(
            @RequestParam(required = false) String estado) {

        List<ApelacionDTO> lista = estado != null
                ? amonestacionesService.listarApelacionesPorEstado(estado)
                : amonestacionesService.listarApelaciones();
        return ResponseEntity.ok(ApiResponse.exito(lista));
    }

    @GetMapping("/apelaciones/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver apelación por ID (admin)")
    public ResponseEntity<ApiResponse<ApelacionDTO>> obtenerApelacion(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(amonestacionesService.obtenerApelacion(id)));
    }

    @GetMapping("/apelaciones/vendedor/{vendedorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar apelaciones de un vendedor (admin)")
    public ResponseEntity<ApiResponse<List<ApelacionDTO>>> apelacionesPorVendedor(
            @PathVariable Integer vendedorId) {
        return ResponseEntity.ok(ApiResponse.exito(
                amonestacionesService.listarApelacionesPorVendedor(vendedorId)));
    }

    @PatchMapping("/apelaciones/{id}/resolver")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolver una apelación (admin)")
    public ResponseEntity<ApiResponse<ApelacionDTO>> resolverApelacion(
            @PathVariable Integer id,
            @Valid @RequestBody ResolverApelacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer adminId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito("Apelación resuelta correctamente",
                amonestacionesService.resolverApelacion(id, request, adminId)));
    }

    // ══════════════════════════════════════════════════════════
    // ADMIN — reportes
    // ══════════════════════════════════════════════════════════

    @GetMapping("/reportes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los reportes de tienda (admin)")
    public ResponseEntity<ApiResponse<List<ReporteTiendaDTO>>> listarReportes(
            @RequestParam(required = false) String estado) {

        List<ReporteTiendaDTO> lista = estado != null
                ? amonestacionesService.listarReportesPorEstado(estado)
                : amonestacionesService.listarReportes();
        return ResponseEntity.ok(ApiResponse.exito(lista));
    }

    @GetMapping("/reportes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ver reporte por ID (admin)")
    public ResponseEntity<ApiResponse<ReporteTiendaDTO>> obtenerReporte(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.exito(amonestacionesService.obtenerReporte(id)));
    }

    @GetMapping("/reportes/vendedor/{vendedorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar reportes de un vendedor (admin)")
    public ResponseEntity<ApiResponse<List<ReporteTiendaDTO>>> reportesPorVendedor(
            @PathVariable Integer vendedorId) {
        return ResponseEntity.ok(ApiResponse.exito(
                amonestacionesService.listarReportesPorVendedor(vendedorId)));
    }

    @PatchMapping("/reportes/{id}/revisar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revisar un reporte de tienda (admin)")
    public ResponseEntity<ApiResponse<ReporteTiendaDTO>> revisarReporte(
            @PathVariable Integer id,
            @Valid @RequestBody RevisarReporteTiendaRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Integer adminId = obtenerUsuarioId(userDetails);
        return ResponseEntity.ok(ApiResponse.exito("Reporte revisado correctamente",
                amonestacionesService.revisarReporte(id, request, adminId)));
    }

    // ── Helper ────────────────────────────────────────────────
    private Integer obtenerUsuarioId(UserDetails userDetails) {
        String correo = userDetails.getUsername();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "correo", correo))
                .getId();
    }
}