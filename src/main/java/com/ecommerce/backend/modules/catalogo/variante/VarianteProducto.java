package com.ecommerce.backend.modules.catalogo.variante;

import com.ecommerce.backend.modules.catalogo.producto.Producto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "variantes_producto")
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "sku", nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock")
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "esta_activa")
    @Builder.Default
    private Boolean estaActiva = true;

    // ── Relación con valores de atributos (talla, color...) ──
    @ManyToMany
    @JoinTable(name = "variante_valores_atributo", joinColumns = @JoinColumn(name = "variante_id"), inverseJoinColumns = @JoinColumn(name = "valor_atributo_id"))
    @Builder.Default
    private List<ValorAtributo> valoresAtributo = new ArrayList<>();
}