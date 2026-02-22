package com.ecommerce.backend.modules.catalogo.producto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

        // ── Buscar por slug ──────────────────────────────────────
        Optional<Producto> findBySlug(String slug);

        // ── Verificar slug único ─────────────────────────────────
        boolean existsBySlug(String slug);

        // ── Productos activos paginados ──────────────────────────
        Page<Producto> findByEstado(String estado, Pageable pageable);

        // ── Productos por categoría ──────────────────────────────
        Page<Producto> findByCategoriaIdAndEstado(Integer categoriaId, String estado, Pageable pageable);

        // ── Productos por vendedor (solo ACTIVOS) ────────────────
        Page<Producto> findByVendedorIdAndEstado(Integer vendedorId, String estado, Pageable pageable);

        // ── Productos por vendedor (ACTIVOS + INACTIVOS, excluye ELIMINADO) ──
        Page<Producto> findByVendedorIdAndEstadoNot(Integer vendedorId, String estado, Pageable pageable);

        // ── Buscar por nombre (like) ─────────────────────────────
        @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO' AND " +
                        "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
        Page<Producto> buscarPorNombre(@Param("termino") String termino, Pageable pageable);

        // ── Búsqueda con filtros combinados ──────────────────────
        @Query("SELECT p FROM Producto p WHERE p.estado = 'ACTIVO' " +
                        "AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId) " +
                        "AND (:vendedorId IS NULL OR p.vendedor.id = :vendedorId) " +
                        "AND (:precioMin IS NULL OR p.precioBase >= :precioMin) " +
                        "AND (:precioMax IS NULL OR p.precioBase <= :precioMax) " +
                        "AND (:termino IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :termino, '%')))")
        Page<Producto> buscarConFiltros(
                        @Param("categoriaId") Integer categoriaId,
                        @Param("vendedorId") Integer vendedorId,
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax,
                        @Param("termino") String termino,
                        Pageable pageable);

        // ── Producto con imágenes y variantes ────────────────────
        @Query("SELECT p FROM Producto p " +
                        "LEFT JOIN FETCH p.imagenes " +
                        "LEFT JOIN FETCH p.variantes " +
                        "WHERE p.slug = :slug AND p.estado = 'ACTIVO'")
        Optional<Producto> findBySlugConDetalles(@Param("slug") String slug);

        // ── Productos destacados (mejor calificación) ────────────
        List<Producto> findTop8ByEstadoOrderByCalificacionPromedioDesc(String estado);

        // ── Contar productos de un vendedor ──────────────────────
        long countByVendedorIdAndEstado(Integer vendedorId, String estado);
}