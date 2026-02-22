package com.ecommerce.backend.modules.cupon;

import com.ecommerce.backend.modules.vendedor.Vendedor;
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
@Table(name = "cupones")
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // NULL = cupón global de plataforma
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Vendedor vendedor;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo; // PORCENTAJE, MONTO_FIJO

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "monto_minimo_orden", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoMinimoOrden = BigDecimal.ZERO;

    @Column(name = "maximo_usos")
    private Integer maximoUsos; // NULL = ilimitado

    @Column(name = "usos_actuales")
    @Builder.Default
    private Integer usosActuales = 0;

    @Column(name = "inicia_en")
    private LocalDateTime iniciaEn;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(name = "esta_activo")
    @Builder.Default
    private Boolean estaActivo = true;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}