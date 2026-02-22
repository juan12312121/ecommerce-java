package com.ecommerce.backend.modules.pago;

import com.ecommerce.backend.modules.orden.Orden;
import com.ecommerce.backend.modules.orden.OrdenRepository;
import com.ecommerce.backend.modules.pago.dto.PagoDTO;
import com.ecommerce.backend.modules.pago.dto.PagoRequest;
import com.ecommerce.backend.shared.enums.EstadoPago;
import com.ecommerce.backend.shared.enums.ProveedorPago;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final OrdenRepository ordenRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${mercadopago.access-token}")
    private String mercadoPagoAccessToken;

    @Value("${app.url}")
    private String appUrl;

    // ── Iniciar pago ─────────────────────────────────────────
    @Transactional
    public PagoDTO iniciarPago(PagoRequest request, Integer usuarioId) {
        Orden orden = ordenRepository.findById(request.getOrdenId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", request.getOrdenId()));

        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw new BadRequestException("La orden no pertenece a tu cuenta");
        }

        if (!orden.getEstado().equals("PENDIENTE")) {
            throw new BadRequestException("Esta orden ya fue procesada");
        }

        ProveedorPago proveedor = ProveedorPago.valueOf(request.getProveedor());

        return switch (proveedor) {
            case STRIPE -> iniciarPagoStripe(orden);
            case MERCADOPAGO -> iniciarPagoMercadoPago(orden);
        };
    }

    // ── Stripe ───────────────────────────────────────────────
    private PagoDTO iniciarPagoStripe(Orden orden) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // Construir items para Stripe
            List<SessionCreateParams.LineItem> lineItems = orden.getItems().stream()
                    .map(item -> SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getCantidad())
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("mxn")
                                    .setUnitAmount(item.getPrecioUnitario()
                                            .multiply(BigDecimal.valueOf(100)).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(item.getVariante().getProducto().getNombre())
                                            .build())
                                    .build())
                            .build())
                    .toList();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(appUrl + "/pago/exitoso?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(appUrl + "/pago/cancelado")
                    .addAllLineItem(lineItems)
                    .putMetadata("orden_id", String.valueOf(orden.getId()))
                    .build();

            Session session = Session.create(params);

            // Guardar pago pendiente
            Pago pago = Pago.builder()
                    .orden(orden)
                    .proveedor(ProveedorPago.STRIPE)
                    .idPagoProveedor(session.getId())
                    .estado(EstadoPago.PENDIENTE)
                    .monto(orden.getTotal())
                    .moneda("MXN")
                    .build();

            pagoRepository.save(pago);

            PagoDTO dto = mapearDTO(pago);
            dto.setUrlPago(session.getUrl());
            return dto;

        } catch (Exception e) {
            throw new BadRequestException("Error al crear sesión de pago Stripe: " + e.getMessage());
        }
    }

    // ── MercadoPago ──────────────────────────────────────────
    private PagoDTO iniciarPagoMercadoPago(Orden orden) {
        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            List<PreferenceItemRequest> items = orden.getItems().stream()
                    .map(item -> PreferenceItemRequest.builder()
                            .title(item.getVariante().getProducto().getNombre())
                            .quantity(item.getCantidad())
                            .unitPrice(item.getPrecioUnitario())
                            .currencyId("MXN")
                            .build())
                    .toList();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(PreferenceBackUrlsRequest.builder()
                            .success(appUrl + "/pago/exitoso")
                            .failure(appUrl + "/pago/fallido")
                            .pending(appUrl + "/pago/pendiente")
                            .build())
                    .autoReturn("approved")
                    .externalReference(String.valueOf(orden.getId()))
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Guardar pago pendiente
            Pago pago = Pago.builder()
                    .orden(orden)
                    .proveedor(ProveedorPago.MERCADOPAGO)
                    .idPagoProveedor(preference.getId())
                    .estado(EstadoPago.PENDIENTE)
                    .monto(orden.getTotal())
                    .moneda("MXN")
                    .build();

            pagoRepository.save(pago);

            PagoDTO dto = mapearDTO(pago);
            dto.setUrlPago(preference.getInitPoint());
            return dto;

        } catch (Exception e) {
            throw new BadRequestException("Error al crear preferencia MercadoPago: " + e.getMessage());
        }
    }

    // ── Webhook Stripe ───────────────────────────────────────
    @Transactional
    public void procesarWebhookStripe(String sessionId) {
        Pago pago = pagoRepository.findByIdPagoProveedor(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "sessionId", sessionId));

        pago.setEstado(EstadoPago.COMPLETADO);
        pagoRepository.save(pago);

        // Actualizar estado de la orden
        Orden orden = pago.getOrden();
        orden.setEstado("PAGADO");
        ordenRepository.save(orden);
    }

    // ── Webhook MercadoPago ──────────────────────────────────
    @Transactional
    public void procesarWebhookMercadoPago(String externalReference, String estado) {
        Orden orden = ordenRepository.findById(Integer.parseInt(externalReference))
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", externalReference));

        Pago pago = pagoRepository.findByOrdenId(orden.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "ordenId", orden.getId()));

        if (estado.equals("approved")) {
            pago.setEstado(EstadoPago.COMPLETADO);
            orden.setEstado("PAGADO");
        } else if (estado.equals("rejected")) {
            pago.setEstado(EstadoPago.FALLIDO);
        }

        pagoRepository.save(pago);
        ordenRepository.save(orden);
    }

    // ── Obtener pago por orden ───────────────────────────────
    public PagoDTO obtenerPorOrden(Integer ordenId) {
        Pago pago = pagoRepository.findByOrdenId(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "ordenId", ordenId));
        return mapearDTO(pago);
    }

    // ── Mapear entidad a DTO ─────────────────────────────────
    private PagoDTO mapearDTO(Pago p) {
        PagoDTO dto = new PagoDTO();
        dto.setId(p.getId());
        dto.setOrdenId(p.getOrden().getId());
        dto.setProveedor(p.getProveedor().name());
        dto.setIdPagoProveedor(p.getIdPagoProveedor());
        dto.setEstado(p.getEstado().name());
        dto.setMonto(p.getMonto());
        dto.setMoneda(p.getMoneda());
        dto.setMetodoPago(p.getMetodoPago());
        dto.setCreadoEn(p.getCreadoEn());
        return dto;
    }
}