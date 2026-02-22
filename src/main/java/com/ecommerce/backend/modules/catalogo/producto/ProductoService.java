package com.ecommerce.backend.modules.catalogo.producto;

import com.ecommerce.backend.modules.catalogo.categoria.Categoria;
import com.ecommerce.backend.modules.catalogo.categoria.CategoriaRepository;
import com.ecommerce.backend.modules.catalogo.producto.dto.ProductoDTO;
import com.ecommerce.backend.modules.catalogo.producto.dto.ProductoRequest;
import com.ecommerce.backend.modules.catalogo.variante.ValorAtributo;
import com.ecommerce.backend.modules.catalogo.variante.ValorAtributoRepository;
import com.ecommerce.backend.modules.catalogo.variante.VarianteProducto;
import com.ecommerce.backend.modules.vendedor.Vendedor;
import com.ecommerce.backend.modules.vendedor.VendedorRepository;
import com.ecommerce.backend.shared.dto.PageResponse;
import com.ecommerce.backend.shared.enums.EstadoVendedor;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final VendedorRepository vendedorRepository;
    private final ValorAtributoRepository valorAtributoRepository;
    private final EntityManager entityManager; // ← para flush explícito

    // ── Listar productos activos paginados ───────────────────
    public PageResponse<ProductoDTO> listarActivos(Pageable pageable) {
        Page<Producto> page = productoRepository.findByEstado("ACTIVO", pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Buscar con filtros ───────────────────────────────────
    public PageResponse<ProductoDTO> buscarConFiltros(
            Integer categoriaId, Integer vendedorId,
            BigDecimal precioMin, BigDecimal precioMax,
            String termino, Pageable pageable) {

        Page<Producto> page = productoRepository.buscarConFiltros(
                categoriaId, vendedorId, precioMin, precioMax, termino, pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Obtener detalle por slug ─────────────────────────────
    public ProductoDTO obtenerPorSlug(String slug) {
        Producto producto = productoRepository.findBySlugConDetalles(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "slug", slug));
        return mapearDTO(producto);
    }

    // ── Obtener por ID ───────────────────────────────────────
    public ProductoDTO obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return mapearDTO(producto);
    }

    // ── Productos destacados ─────────────────────────────────
    public List<ProductoDTO> obtenerDestacados() {
        return productoRepository
                .findTop8ByEstadoOrderByCalificacionPromedioDesc("ACTIVO")
                .stream().map(this::mapearDTO).collect(Collectors.toList());
    }

    // ── Productos del vendedor autenticado (ACTIVOS + INACTIVOS) ─
    public PageResponse<ProductoDTO> listarPorVendedor(Integer usuarioId, Pageable pageable) {
        Vendedor vendedor = obtenerVendedorAprobado(usuarioId);
        Page<Producto> page = productoRepository
                .findByVendedorIdAndEstadoNot(vendedor.getId(), "ELIMINADO", pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Crear producto ───────────────────────────────────────
    @Transactional
    public ProductoDTO crear(ProductoRequest request, Integer usuarioId) {
        Vendedor vendedor = obtenerVendedorAprobado(usuarioId);

        if (productoRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Ya existe un producto con el slug: " + request.getSlug());
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));

        Producto producto = Producto.builder()
                .vendedor(vendedor)
                .categoria(categoria)
                .nombre(request.getNombre())
                .slug(request.getSlug())
                .descripcion(request.getDescripcion())
                .precioBase(request.getPrecioBase())
                .estado("ACTIVO")
                .build();

        agregarVariantes(producto, request.getVariantes());

        return mapearDTO(productoRepository.save(producto));
    }

    // ── Actualizar producto ──────────────────────────────────
    @Transactional
    public ProductoDTO actualizar(Integer id, ProductoRequest request, Integer usuarioId) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        Vendedor vendedor = obtenerVendedorAprobado(usuarioId);

        if (!producto.getVendedor().getId().equals(vendedor.getId())) {
            throw new ForbiddenException("No tienes permiso para editar este producto");
        }

        if (!producto.getSlug().equals(request.getSlug()) &&
                productoRepository.existsBySlug(request.getSlug())) {
            throw new BadRequestException("Ya existe un producto con el slug: " + request.getSlug());
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", "id", request.getCategoriaId()));

        producto.setNombre(request.getNombre());
        producto.setSlug(request.getSlug());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecioBase(request.getPrecioBase());
        producto.setCategoria(categoria);

        // ── Limpiar variantes viejas y forzar DELETE en BD ───
        // antes de insertar las nuevas (evita violación de UNIQUE en SKU)
        producto.getVariantes().clear();
        entityManager.flush(); // ← DELETE ejecutado aquí inmediatamente

        agregarVariantes(producto, request.getVariantes());

        return mapearDTO(productoRepository.save(producto));
    }

    // ── Desactivar producto ──────────────────────────────────
    @Transactional
    public void desactivar(Integer id, Integer usuarioId) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        Vendedor vendedor = obtenerVendedorAprobado(usuarioId);

        if (!producto.getVendedor().getId().equals(vendedor.getId())) {
            throw new ForbiddenException("No tienes permiso para eliminar este producto");
        }

        producto.setEstado("INACTIVO");
        productoRepository.save(producto);
    }

    // ── Helpers privados ─────────────────────────────────────

    private Vendedor obtenerVendedorAprobado(Integer usuarioId) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));

        if (vendedor.getEstado() != EstadoVendedor.APROBADO) {
            throw new ForbiddenException("Tu tienda no está aprobada para publicar productos");
        }
        return vendedor;
    }

    private void agregarVariantes(Producto producto, List<ProductoRequest.VarianteRequest> variantesRequest) {
        for (ProductoRequest.VarianteRequest vr : variantesRequest) {
            VarianteProducto variante = VarianteProducto.builder()
                    .producto(producto)
                    .sku(vr.getSku())
                    .precio(vr.getPrecio())
                    .stock(vr.getStock())
                    .estaActiva(true)
                    .build();

            if (vr.getValoresAtributoIds() != null && !vr.getValoresAtributoIds().isEmpty()) {
                List<ValorAtributo> valores = valorAtributoRepository
                        .findAllById(vr.getValoresAtributoIds());
                variante.setValoresAtributo(valores);
            }

            producto.getVariantes().add(variante);
        }
    }

    // ── Mapear entidad a DTO ─────────────────────────────────
    public ProductoDTO mapearDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setSlug(p.getSlug());
        dto.setDescripcion(p.getDescripcion());
        dto.setPrecioBase(p.getPrecioBase());
        dto.setEstado(p.getEstado());
        dto.setCalificacionPromedio(p.getCalificacionPromedio());
        dto.setTotalResenas(p.getTotalResenas());
        dto.setCreadoEn(p.getCreadoEn());

        if (p.getCategoria() != null) {
            dto.setCategoriaId(p.getCategoria().getId());
            dto.setCategoriaNombre(p.getCategoria().getNombre());
        }

        if (p.getVendedor() != null) {
            dto.setVendedorId(p.getVendedor().getId());
            dto.setNombreTienda(p.getVendedor().getNombreTienda());
            dto.setSlugTienda(p.getVendedor().getSlugTienda());
        }

        if (p.getImagenes() != null) {
            dto.setImagenes(p.getImagenes().stream().map(img -> {
                ProductoDTO.ImagenProductoDTO i = new ProductoDTO.ImagenProductoDTO();
                i.setId(img.getId());
                i.setUrl(img.getUrl());
                i.setEsPrincipal(img.getEsPrincipal());
                i.setOrden(img.getOrden());
                return i;
            }).collect(Collectors.toList()));
        }

        if (p.getVariantes() != null) {
            dto.setVariantes(p.getVariantes().stream().map(v -> {
                ProductoDTO.VarianteProductoDTO vd = new ProductoDTO.VarianteProductoDTO();
                vd.setId(v.getId());
                vd.setSku(v.getSku());
                vd.setPrecio(v.getPrecio());
                vd.setStock(v.getStock());
                vd.setEstaActiva(v.getEstaActiva());
                if (v.getValoresAtributo() != null) {
                    vd.setValoresAtributo(v.getValoresAtributo().stream().map(va -> {
                        ProductoDTO.ValorAtributoDTO vad = new ProductoDTO.ValorAtributoDTO();
                        vad.setId(va.getId());
                        vad.setAtributo(va.getAtributo().getNombre());
                        vad.setValor(va.getValor());
                        return vad;
                    }).collect(Collectors.toList()));
                }
                return vd;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    // ── Cambiar estado (ACTIVO/INACTIVO) ────────────────────────
    @Transactional
    public ProductoDTO cambiarEstado(Integer id, String nuevoEstado, Integer usuarioId) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        Vendedor vendedor = obtenerVendedorAprobado(usuarioId);

        if (!producto.getVendedor().getId().equals(vendedor.getId())) {
            throw new ForbiddenException("No tienes permiso para modificar este producto");
        }

        producto.setEstado(nuevoEstado);
        return mapearDTO(productoRepository.save(producto));
    }
}