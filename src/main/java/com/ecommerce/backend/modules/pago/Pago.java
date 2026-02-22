package com.ecommerce.backend.modules.pago;

import com.ecommerce.backend.modules.orden.Orden;
import com.ecommerce.backend.shared.enums.EstadoPago;
import com.ecommerce.backend.shared.enums.ProveedorPago;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Enumerated(EnumType.STRING)
    @Column(name = "proveedor", nullable = false, length = 20)
    private ProveedorPago proveedor; // STRIPE, MERCADOPAGO

    @Column(name = "id_pago_proveedor", length = 200)
    private String idPagoProveedor; // ID externo del proveedor

    @Column(name = "id_orden_proveedor", length = 200)
    private String idOrdenProveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPago estado;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", length = 10)
    @Builder.Default
    private String moneda = "MXN";

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // tarjeta, oxxo, transferencia, etc.

    @Column(name = "metadata", columnDefinition = "NVARCHAR(MAX)")
    private String metadata; // JSON con respuesta del proveedor

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}