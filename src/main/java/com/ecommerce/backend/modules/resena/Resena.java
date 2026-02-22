package com.ecommerce.backend.modules.resena;

import com.ecommerce.backend.modules.catalogo.producto.Producto;
import com.ecommerce.backend.modules.orden.ItemOrden;
import com.ecommerce.backend.modules.usuario.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Solo compradores verificados pueden reseñar
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_orden_id", nullable = false)
    private ItemOrden itemOrden;

    @Column(name = "calificacion", nullable = false)
    private Integer calificacion; // 1 a 5

    @Column(name = "titulo", length = 200)
    private String titulo;

    @Column(name = "comentario", columnDefinition = "NVARCHAR(MAX)")
    private String comentario;

    @Column(name = "es_verificada")
    @Builder.Default
    private Boolean esVerificada = true;

    // ── Imágenes de la reseña ────────────────────────────────
    @OneToMany(mappedBy = "resena", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ImagenResena> imagenes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}