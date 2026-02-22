package com.ecommerce.backend.modules.auth;

import com.ecommerce.backend.modules.auth.dto.AuthResponse;
import com.ecommerce.backend.modules.auth.dto.LoginRequest;
import com.ecommerce.backend.modules.auth.dto.RegisterRequest;
import com.ecommerce.backend.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro y refresh token")
public class AuthController {

    private final AuthService authService;

    // ── POST /auth/registro ──────────────────────────────────
    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario comprador")
    public ResponseEntity<ApiResponse<AuthResponse>> registrar(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.registrar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Usuario registrado correctamente", response));
    }

    // ── POST /auth/registro-vendedor ─────────────────────────
    @PostMapping("/registro-vendedor")
    @Operation(summary = "Registrar nuevo vendedor (pendiente de aprobación)")
    public ResponseEntity<ApiResponse<Void>> registrarVendedor(
            @Valid @RequestBody RegisterRequest request) {

        authService.registrarVendedor(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Solicitud enviada. Tu cuenta será revisada por un administrador.", null));
    }

    // ── POST /auth/login ─────────────────────────────────────
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.exito("Login exitoso", response));
    }

    // ── POST /auth/refresh ───────────────────────────────────
    @PostMapping("/refresh")
    @Operation(summary = "Renovar access token con refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {

        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.exito("Token renovado correctamente", response));
    }

    // ── POST /auth/crear-admin ───────────────────────────────
    @PostMapping("/crear-admin")
    @Operation(summary = "Crear usuario administrador")
    public ResponseEntity<ApiResponse<AuthResponse>> crearAdmin(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.crearAdmin(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.exito("Admin creado correctamente", response));
    }
}