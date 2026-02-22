package com.ecommerce.backend.modules.orden;

import com.ecommerce.backend.modules.carrito.Carrito;
import com.ecommerce.backend.modules.carrito.CarritoRepository;
import com.ecommerce.backend.modules.carrito.ItemCarrito;
import com.ecommerce.backend.modules.cupon.Cupon;
import com.ecommerce.backend.modules.cupon.CuponService;
import com.ecommerce.backend.modules.orden.dto.OrdenDTO;
import com.ecommerce.backend.modules.orden.dto.OrdenRequest;
import com.ecommerce.backend.modules.usuario.Direccion;
import com.ecommerce.backend.modules.usuario.DireccionRepository;
import com.ecommerce.backend.modules.vendedor.Vendedor;
import com.ecommerce.backend.modules.vendedor.VendedorRepository;
import com.ecommerce.backend.shared.dto.PageResponse;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final CarritoRepository carritoRepository;
    private final DireccionRepository direccionRepository;
    private final CuponService cuponService;
    private final VendedorRepository vendedorRepository; // ← para buscar vendedor por usuarioId

    // ── Crear orden desde el carrito (checkout) ──────────────
    @Transactional
    public OrdenDTO crearDesdeCarrito(Integer usuarioId, OrdenRequest request) {

        Carrito carrito = carritoRepository.findByUsuarioIdConItems(usuarioId)
                .orElseThrow(() -> new BadRequestException("Tu carrito está vacío"));

        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("Tu carrito está vacío");
        }

        Direccion direccion = direccionRepository.findById(request.getDireccionEnvioId())
                .orElseThrow(() -> new ResourceNotFoundException("Dirección", "id", request.getDireccionEnvioId()));

        if (!direccion.getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("La dirección no pertenece a tu cuenta");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemCarrito item : carrito.getItems()) {
            if (item.getVariante().getStock() < item.getCantidad()) {
                throw new BadRequestException("Stock insuficiente para: " +
                        item.getVariante().getProducto().getNombre());
            }
            subtotal = subtotal.add(
                    item.getVariante().getPrecio()
                            .multiply(BigDecimal.valueOf(item.getCantidad())));
        }

        Cupon cupon = null;
        BigDecimal descuento = BigDecimal.ZERO;
        if (request.getCodigoCupon() != null && !request.getCodigoCupon().isBlank()) {
            cupon = cuponService.validarCupon(request.getCodigoCupon(), subtotal);
            descuento = cuponService.calcularDescuento(cupon, subtotal);
        }

        BigDecimal costoEnvio = BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuento).add(costoEnvio);

        Orden orden = Orden.builder()
                .usuario(carrito.getUsuario())
                .cupon(cupon)
                .direccionEnvio(direccion)
                .subtotal(subtotal)
                .montoDescuento(descuento)
                .costoEnvio(costoEnvio)
                .total(total)
                .notas(request.getNotas())
                .estado("PENDIENTE")
                .build();

        Map<Integer, List<ItemOrden>> itemsPorVendedor = new java.util.HashMap<>();

        for (ItemCarrito itemCarrito : carrito.getItems()) {
            ItemOrden itemOrden = ItemOrden.builder()
                    .orden(orden)
                    .variante(itemCarrito.getVariante())
                    .vendedor(itemCarrito.getVariante().getProducto().getVendedor())
                    .cantidad(itemCarrito.getCantidad())
                    .precioUnitario(itemCarrito.getVariante().getPrecio())
                    .subtotal(itemCarrito.getVariante().getPrecio()
                            .multiply(BigDecimal.valueOf(itemCarrito.getCantidad())))
                    .build();

            orden.getItems().add(itemOrden);

            Integer vendedorId = itemCarrito.getVariante().getProducto().getVendedor().getId();
            itemsPorVendedor.computeIfAbsent(vendedorId, k -> new ArrayList<>()).add(itemOrden);
        }

        for (Map.Entry<Integer, List<ItemOrden>> entry : itemsPorVendedor.entrySet()) {
            BigDecimal totalVendedor = entry.getValue().stream()
                    .map(ItemOrden::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            OrdenVendedor ordenVendedor = OrdenVendedor.builder()
                    .orden(orden)
                    .vendedor(entry.getValue().get(0).getVendedor())
                    .total(totalVendedor)
                    .estado("PENDIENTE")
                    .build();

            orden.getOrdenesVendedor().add(ordenVendedor);
        }

        Orden ordenGuardada = ordenRepository.save(orden);

        carrito.getItems().clear();
        carritoRepository.save(carrito);

        return mapearDTO(ordenGuardada);
    }

    // ── Mis órdenes (comprador) ──────────────────────────────
    public PageResponse<OrdenDTO> misOrdenes(Integer usuarioId, Pageable pageable) {
        Page<Orden> page = ordenRepository
                .findByUsuarioIdOrderByCreadoEnDesc(usuarioId, pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Ver detalle de una orden ─────────────────────────────
    public OrdenDTO obtenerDetalle(Integer ordenId, Integer usuarioId) {
        Orden orden = ordenRepository.findByIdConDetalles(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", ordenId));

        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No tienes acceso a esta orden");
        }

        return mapearDTO(orden);
    }

    // ── Órdenes del vendedor autenticado ─────────────────────
    // Recibe usuarioId (del JWT), lo convierte a vendedorId
    public PageResponse<OrdenDTO> ordenesPorVendedor(Integer usuarioId, Pageable pageable) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));

        Page<Orden> page = ordenRepository.findByVendedorId(vendedor.getId(), pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Todas las órdenes (admin) ────────────────────────────
    public PageResponse<OrdenDTO> todasLasOrdenes(String estado, Pageable pageable) {
        Page<Orden> page = estado != null
                ? ordenRepository.findByEstadoOrderByCreadoEnDesc(estado, pageable)
                : ordenRepository.findAll(pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Cancelar orden ───────────────────────────────────────
    @Transactional
    public OrdenDTO cancelar(Integer ordenId, Integer usuarioId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", ordenId));

        if (!orden.getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No tienes acceso a esta orden");
        }

        if (!orden.getEstado().equals("PENDIENTE")) {
            throw new BadRequestException("Solo se pueden cancelar órdenes en estado PENDIENTE");
        }

        orden.setEstado("CANCELADO");
        return mapearDTO(ordenRepository.save(orden));
    }

    // ── Actualizar estado (admin/vendedor) ───────────────────
    @Transactional
    public OrdenDTO actualizarEstado(Integer ordenId, String nuevoEstado) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", ordenId));

        orden.setEstado(nuevoEstado);
        return mapearDTO(ordenRepository.save(orden));
    }

    // ── Mapear entidad a DTO ─────────────────────────────────
    public OrdenDTO mapearDTO(Orden o) {
        OrdenDTO dto = new OrdenDTO();
        dto.setId(o.getId());
        dto.setEstado(o.getEstado());
        dto.setSubtotal(o.getSubtotal());
        dto.setMontoDescuento(o.getMontoDescuento());
        dto.setCostoEnvio(o.getCostoEnvio());
        dto.setTotal(o.getTotal());
        dto.setNotas(o.getNotas());
        dto.setCreadoEn(o.getCreadoEn());

        if (o.getCupon() != null) {
            dto.setCodigoCupon(o.getCupon().getCodigo());
            dto.setTipoCupon(o.getCupon().getTipo());
        }

        if (o.getDireccionEnvio() != null) {
            Direccion d = o.getDireccionEnvio();
            dto.setDireccionEnvio(d.getCalle() + ", " + d.getCiudad() +
                    ", " + d.getEstado() + ", " + d.getPais());
        }

        if (o.getItems() != null) {
            dto.setItems(o.getItems().stream().map(i -> {
                OrdenDTO.ItemOrdenDTO item = new OrdenDTO.ItemOrdenDTO();
                item.setId(i.getId());
                item.setCantidad(i.getCantidad());
                item.setPrecioUnitario(i.getPrecioUnitario());
                item.setSubtotal(i.getSubtotal());
                if (i.getVariante() != null && i.getVariante().getProducto() != null) {
                    item.setProductoNombre(i.getVariante().getProducto().getNombre());
                    item.setProductoSlug(i.getVariante().getProducto().getSlug());
                    item.setSku(i.getVariante().getSku());
                }
                if (i.getVendedor() != null) {
                    item.setNombreTienda(i.getVendedor().getNombreTienda());
                    item.setVendedorId(i.getVendedor().getId());
                }
                return item;
            }).collect(Collectors.toList()));
        }

        if (o.getOrdenesVendedor() != null) {
            dto.setOrdenesVendedor(o.getOrdenesVendedor().stream().map(ov -> {
                OrdenDTO.OrdenVendedorDTO ovDto = new OrdenDTO.OrdenVendedorDTO();
                ovDto.setId(ov.getId());
                ovDto.setEstado(ov.getEstado());
                ovDto.setNumeroSeguimiento(ov.getNumeroSeguimiento());
                ovDto.setTotal(ov.getTotal());
                if (ov.getVendedor() != null) {
                    ovDto.setNombreTienda(ov.getVendedor().getNombreTienda());
                }
                return ovDto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}