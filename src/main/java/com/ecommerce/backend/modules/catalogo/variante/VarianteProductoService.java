package com.ecommerce.backend.modules.catalogo.variante;

import com.ecommerce.backend.modules.catalogo.producto.Producto;
import com.ecommerce.backend.modules.catalogo.producto.ProductoRepository;
import com.ecommerce.backend.modules.catalogo.variante.dto.VarianteProductoRequest;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VarianteProductoService {

    private final VarianteProductoRepository varianteRepository;
    private final ValorAtributoRepository valorAtributoRepository;
    private final ProductoRepository productoRepository;

    // ── Listar variantes activas de un producto ──────────────
    public List<VarianteProducto> listarPorProducto(Integer productoId) {
        return varianteRepository.findByProductoIdAndEstaActivaTrue(productoId);
    }

    // ── Obtener variante por ID ──────────────────────────────
    public VarianteProducto obtenerPorId(Integer id) {
        return varianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarianteProducto", "id", id));
    }

    // ── Crear variante ───────────────────────────────────────
    @Transactional
    public VarianteProducto crear(Integer productoId, VarianteProductoRequest request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));

        if (varianteRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Ya existe una variante con el SKU: " + request.getSku());
        }

        List<ValorAtributo> valores = valorAtributoRepository.findAllById(request.getValoresAtributoIds());

        VarianteProducto variante = VarianteProducto.builder()
                .producto(producto)
                .sku(request.getSku())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .estaActiva(request.getEstaActiva())
                .valoresAtributo(valores)
                .build();

        return varianteRepository.save(variante);
    }

    // ── Actualizar variante ──────────────────────────────────
    @Transactional
    public VarianteProducto actualizar(Integer id, VarianteProductoRequest request) {
        VarianteProducto variante = obtenerPorId(id);

        if (!variante.getSku().equals(request.getSku()) &&
                varianteRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Ya existe una variante con el SKU: " + request.getSku());
        }

        List<ValorAtributo> valores = valorAtributoRepository.findAllById(request.getValoresAtributoIds());

        variante.setSku(request.getSku());
        variante.setPrecio(request.getPrecio());
        variante.setStock(request.getStock());
        variante.setEstaActiva(request.getEstaActiva());
        variante.setValoresAtributo(valores);

        return varianteRepository.save(variante);
    }

    // ── Desactivar variante ──────────────────────────────────
    @Transactional
    public void desactivar(Integer id) {
        VarianteProducto variante = obtenerPorId(id);
        variante.setEstaActiva(false);
        varianteRepository.save(variante);
    }

    // ── Actualizar stock ─────────────────────────────────────
    @Transactional
    public VarianteProducto actualizarStock(Integer id, Integer nuevoStock) {
        if (nuevoStock < 0)
            throw new BadRequestException("El stock no puede ser negativo");
        VarianteProducto variante = obtenerPorId(id);
        variante.setStock(nuevoStock);
        return varianteRepository.save(variante);
    }
}