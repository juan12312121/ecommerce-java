package com.ecommerce.backend.modules.amonestaciones;

import com.ecommerce.backend.modules.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_tienda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportado_por")
    private Usuario reportadoPor;

    @Column(nullable = false, length = 50)
    private String motivo;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String descripcion;

    @Column(length = 20)
    private String estado; // PENDIENTE, REVISADO, DESESTIMADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por")
    private Usuario revisadoPor;

    @Column(name = "revisado_en")
    private LocalDateTime revisadoEn;

    @Column(name = "nota_resolucion", columnDefinition = "NVARCHAR(MAX)")
    private String notaResolucion;

    @CreationTimestamp
    @Column(name = "creado_en")
    private LocalDateTime creadoEn;
}