package com.ecommerce.backend.modules.catalogo.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // ── Buscar por slug ──────────────────────────────────────
    Optional<Categoria> findBySlug(String slug);

    // ── Verificar si existe un slug ──────────────────────────
    boolean existsBySlug(String slug);

    // ── Solo categorías activas ──────────────────────────────
    List<Categoria> findByEstaActivaTrue();

    // ── Solo categorías padre (sin padre) activas ────────────
    List<Categoria> findByCategoriaPadreIsNullAndEstaActivaTrue();

    // ── Subcategorías de una categoría padre ─────────────────
    List<Categoria> findByCategoriaPadreIdAndEstaActivaTrue(Integer padreId);

    // ── Todas las categorías con sus subcategorías ───────────
    @Query("SELECT c FROM Categoria c LEFT JOIN FETCH c.subcategorias WHERE c.categoriaPadre IS NULL AND c.estaActiva = true")
    List<Categoria> findAllConSubcategorias();
}