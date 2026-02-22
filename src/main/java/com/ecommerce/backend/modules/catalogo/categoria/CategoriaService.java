package com.ecommerce.backend.modules.catalogo.categoria;

import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // ── Listar todas las categorías activas ──────────────────
    public List<Categoria> listarTodas() {
        return categoriaRepository.findByEstaActivaTrue();
    }

    // ── Listar árbol: categorías padre con subcategorías ─────
    public List<Categoria> listarArbol() {
        return categoriaRepository.findAllConSubcategorias();
    }

    // ── Listar solo categorías padre activas ─────────────────
    public List<Categoria> listarPadres() {
        return categoriaRepository.findByCategoriaPadreIsNullAndEstaActivaTrue();
    }

    // ── Listar subcategorías de una categoría ────────────────
    public List<Categoria> listarSubcategorias(Integer padreId) {
        return categoriaRepository.findByCategoriaPadreIdAndEstaActivaTrue(padreId);
    }

    // ── Obtener por ID ───────────────────────────────────────
    public Categoria obtenerPorId(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", id));
    }

    // ── Obtener por slug ─────────────────────────────────────
    public Categoria obtenerPorSlug(String slug) {
        return categoriaRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "slug", slug));
    }

    // ── Crear categoría (admin) ──────────────────────────────
    @Transactional
    public Categoria crear(CategoriaRequest request) {
        if (categoriaRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Ya existe una categoría con el slug: " + request.getSlug());
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .slug(request.getSlug())
                .imagenUrl(request.getImagenUrl())
                .estaActiva(true)
                .build();

        if (request.getCategoriaPadreId() != null) {
            Categoria padre = obtenerPorId(request.getCategoriaPadreId());
            categoria.setCategoriaPadre(padre);
        }

        return categoriaRepository.save(categoria);
    }

    // ── Actualizar categoría (admin) ─────────────────────────
    @Transactional
    public Categoria actualizar(Integer id, CategoriaRequest request) {
        Categoria categoria = obtenerPorId(id);

        if (!categoria.getSlug().equals(request.getSlug()) &&
                categoriaRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Ya existe una categoría con el slug: " + request.getSlug());
        }

        categoria.setNombre(request.getNombre());
        categoria.setSlug(request.getSlug());
        if (request.getImagenUrl() != null)
            categoria.setImagenUrl(request.getImagenUrl());

        if (request.getCategoriaPadreId() != null) {
            Categoria padre = obtenerPorId(request.getCategoriaPadreId());
            categoria.setCategoriaPadre(padre);
        }

        return categoriaRepository.save(categoria);
    }

    // ── Desactivar categoría (admin) ─────────────────────────
    @Transactional
    public void desactivar(Integer id) {
        Categoria categoria = obtenerPorId(id);
        categoria.setEstaActiva(false);
        categoriaRepository.save(categoria);
    }
}