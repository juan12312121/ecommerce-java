package com.ecommerce.backend.modules.catalogo.variante;

import com.ecommerce.backend.modules.catalogo.variante.dto.*;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AtributoService {

    private final AtributoRepository atributoRepository;
    private final ValorAtributoRepository valorAtributoRepository;

    // ── Listar todos los atributos ───────────────────────────
    public List<Atributo> listarTodos() {
        return atributoRepository.findAllByOrderByNombreAsc();
    }

    // ── Obtener atributo por ID ──────────────────────────────
    public Atributo obtenerPorId(Integer id) {
        return atributoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atributo", "id", id));
    }

    // ── Crear atributo ───────────────────────────────────────
    @Transactional
    public Atributo crear(AtributoRequest request) {
        if (atributoRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe un atributo con el nombre: " + request.getNombre());
        }
        return atributoRepository.save(
                Atributo.builder().nombre(request.getNombre()).build());
    }

    // ── Actualizar atributo ──────────────────────────────────
    @Transactional
    public Atributo actualizar(Integer id, AtributoRequest request) {
        Atributo atributo = obtenerPorId(id);
        if (!atributo.getNombre().equals(request.getNombre()) &&
                atributoRepository.existsByNombre(request.getNombre())) {
            throw new BadRequestException("Ya existe un atributo con el nombre: " + request.getNombre());
        }
        atributo.setNombre(request.getNombre());
        return atributoRepository.save(atributo);
    }

    // ── Eliminar atributo ────────────────────────────────────
    @Transactional
    public void eliminar(Integer id) {
        Atributo atributo = obtenerPorId(id);
        atributoRepository.delete(atributo);
    }

    // ── Listar valores de un atributo ────────────────────────
    public List<ValorAtributo> listarValores(Integer atributoId) {
        obtenerPorId(atributoId); // valida que existe
        return valorAtributoRepository.findByAtributoId(atributoId);
    }

    // ── Agregar valor a un atributo ──────────────────────────
    @Transactional
    public ValorAtributo agregarValor(Integer atributoId, ValorAtributoRequest request) {
        Atributo atributo = obtenerPorId(atributoId);
        ValorAtributo valor = ValorAtributo.builder()
                .atributo(atributo)
                .valor(request.getValor())
                .build();
        return valorAtributoRepository.save(valor);
    }

    // ── Eliminar valor de un atributo ────────────────────────
    @Transactional
    public void eliminarValor(Integer valorId) {
        ValorAtributo valor = valorAtributoRepository.findById(valorId)
                .orElseThrow(() -> new ResourceNotFoundException("ValorAtributo", "id", valorId));
        valorAtributoRepository.delete(valor);
    }
}