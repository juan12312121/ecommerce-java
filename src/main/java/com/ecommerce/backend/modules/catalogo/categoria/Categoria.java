package com.ecommerce.backend.modules.catalogo.categoria;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "esta_activa")
    @Builder.Default
    private Boolean estaActiva = true;

    // ── Relación con categoría padre ─────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_padre_id")
    private Categoria categoriaPadre;

    // ── Subcategorías hijas ──────────────────────────────────
    @OneToMany(mappedBy = "categoriaPadre", cascade = CascadeType.ALL)
    @JsonIgnore
    @Builder.Default
    private List<Categoria> subcategorias = new ArrayList<>();
}