package com.ecommerce.backend.modules.pago;

import com.ecommerce.backend.modules.pago.dto.PagoDTO;
import com.ecommerce.backend.modules.pago.dto.PagoRequest;
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

import java.util.Map;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Gestión de pagos con Stripe y MercadoPago")
public class PagoController {

    private final PagoService pagoService;

    // ── POST /pagos/iniciar — iniciar pago ───────────────────
    @PostMapping("/iniciar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Iniciar proceso de pago")
    public ResponseEntity<ApiResponse<PagoDTO>> iniciar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PagoRequest request) {

        Integer usuarioId = obtenerUsuarioId(userDetails);
        PagoDTO dto = pagoService.iniciarPago(request, usuarioId);
        return ResponseEntity.ok(ApiResponse.exito(
                "Redirige al usuario a urlPago para completar el pago", dto));
    }

    // ── GET /pagos/orden/{ordenId} — ver pago de una orden ───
    @GetMapping("/orden/{ordenId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver pago de una orden")
    public ResponseEntity<ApiResponse<PagoDTO>> obtenerPorOrden(
            @PathVariable Integer ordenId) {
        return ResponseEntity.ok(ApiResponse.exito(pagoService.obtenerPorOrden(ordenId)));
    }

    // ── POST /pagos/webhook/stripe — webhook de Stripe ───────
    @PostMapping("/webhook/stripe")
    @Operation(summary = "Webhook de Stripe (uso interno)")
    public ResponseEntity<Void> webhookStripe(
            @RequestBody Map<String, Object> payload) {

        // Stripe envía el session_id en el objeto data.object.id
        try {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            Map<String, Object> object = (Map<String, Object>) data.get("object");
            String sessionId = (String) object.get("id");
            String eventType = (String) payload.get("type");

            if ("checkout.session.completed".equals(eventType)) {
                pagoService.procesarWebhookStripe(sessionId);
            }
        } catch (Exception e) {
            // Loguear pero siempre responder 200 a Stripe
            System.err.println("Error procesando webhook Stripe: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // ── POST /pagos/webhook/mercadopago — webhook de MP ──────
    @PostMapping("/webhook/mercadopago")
    @Operation(summary = "Webhook de MercadoPago (uso interno)")
    public ResponseEntity<Void> webhookMercadoPago(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> payload) {

        try {
            if ("payment".equals(topic) && id != null) {
                // En producción aquí consultarías la API de MP para obtener
                // el estado real del pago usando el id
                String externalReference = "";
                String estado = "";

                if (payload != null && payload.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) payload.get("data");
                    externalReference = String.valueOf(data.get("external_reference"));
                    estado = String.valueOf(data.get("status"));
                }

                if (!externalReference.isEmpty()) {
                    pagoService.procesarWebhookMercadoPago(externalReference, estado);
                }
            }
        } catch (Exception e) {
            System.err.println("Error procesando webhook MercadoPago: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // ── Helper ────────────────────────────────────────────────
    private Integer obtenerUsuarioId(UserDetails userDetails) {
        return ((com.ecommerce.backend.modules.usuario.Usuario) userDetails).getId();
    }
}