package com.ecommerce.backend.modules.orden;

import com.ecommerce.backend.modules.vendedor.Vendedor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordenes_vendedor")
public class OrdenVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Vendedor vendedor;

    @Column(name = "estado", length = 30)
    @Builder.Default
    private String estado = "PENDIENTE";

    @Column(name = "numero_seguimiento", length = 100)
    private String numeroSeguimiento;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}