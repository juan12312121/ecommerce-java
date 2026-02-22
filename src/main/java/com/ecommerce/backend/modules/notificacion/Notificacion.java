package com.ecommerce.backend.modules.notificacion;

import com.ecommerce.backend.modules.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo", length = 50)
    private String tipo;
    // ORDEN_ACTUALIZADA, NUEVO_REPORTE, ADVERTENCIA_EMITIDA, PAGO_RECIBIDO

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "mensaje", columnDefinition = "NVARCHAR(MAX)")
    private String mensaje;

    @Column(name = "leida")
    @Builder.Default
    private Boolean leida = false;

    @Column(name = "url_redireccion", length = 500)
    private String urlRedireccion;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}