package com.ecommerce.backend.modules.vendedor;

import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.shared.enums.EstadoVendedor;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "vendedores")
public class Vendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @JsonIgnore // ← evita loop circular con Usuario
    private Usuario usuario;

    @Column(name = "nombre_tienda", nullable = false, length = 150)
    private String nombreTienda;

    @Column(name = "slug_tienda", nullable = false, unique = true, length = 150)
    private String slugTienda;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 20)
    @Builder.Default
    private EstadoVendedor estado = EstadoVendedor.APROBADO;

    @Column(name = "tasa_comision", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tasaComision = new BigDecimal("10.00");

    // ── Moderación ───────────────────────────────────────────
    @Column(name = "cantidad_advertencias", nullable = false)
    @Builder.Default
    private Integer cantidadAdvertencias = 0;

    @Column(name = "suspendido_hasta")
    private LocalDateTime suspendidoHasta;

    @Column(name = "razon_suspension", columnDefinition = "TEXT")
    private String razonSuspension;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}