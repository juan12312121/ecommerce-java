package com.ecommerce.backend.modules.carrito;

import com.ecommerce.backend.modules.catalogo.variante.VarianteProducto;
import com.ecommerce.backend.modules.catalogo.variante.VarianteProductoRepository;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final VarianteProductoRepository varianteProductoRepository;
    private final UsuarioRepository usuarioRepository;

    // ── Obtener carrito del usuario ──────────────────────────
    public Carrito obtenerCarrito(Integer usuarioId) {
        return carritoRepository.findByUsuarioIdConItems(usuarioId)
                .orElseGet(() -> crearCarrito(usuarioId));
    }

    // ── Agregar item al carrito ──────────────────────────────
    @Transactional
    public Carrito agregar(Integer usuarioId, ItemCarritoRequest request) {
        Carrito carrito = obtenerCarrito(usuarioId);

        VarianteProducto variante = varianteProductoRepository.findById(request.getVarianteId())
                .orElseThrow(() -> new ResourceNotFoundException("Variante", "id", request.getVarianteId()));

        // Validar stock disponible
        if (variante.getStock() < request.getCantidad()) {
            throw new BadRequestException("Stock insuficiente. Disponible: " + variante.getStock());
        }

        // Si ya existe el item, actualizar cantidad
        carrito.getItems().stream()
                .filter(i -> i.getVariante().getId().equals(request.getVarianteId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int nuevaCantidad = item.getCantidad() + request.getCantidad();
                            if (nuevaCantidad > variante.getStock()) {
                                throw new BadRequestException("Stock insuficiente. Disponible: " + variante.getStock());
                            }
                            item.setCantidad(nuevaCantidad);
                        },
                        () -> {
                            ItemCarrito nuevoItem = ItemCarrito.builder()
                                    .carrito(carrito)
                                    .variante(variante)
                                    .cantidad(request.getCantidad())
                                    .build();
                            carrito.getItems().add(nuevoItem);
                        });

        return carritoRepository.save(carrito);
    }

    // ── Actualizar cantidad de un item ───────────────────────
    @Transactional
    public Carrito actualizarCantidad(Integer usuarioId, Integer itemId, Integer cantidad) {
        Carrito carrito = obtenerCarrito(usuarioId);

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        if (cantidad <= 0) {
            carrito.getItems().remove(item);
        } else {
            if (item.getVariante().getStock() < cantidad) {
                throw new BadRequestException("Stock insuficiente. Disponible: " + item.getVariante().getStock());
            }
            item.setCantidad(cantidad);
        }

        return carritoRepository.save(carrito);
    }

    // ── Eliminar item del carrito ────────────────────────────
    @Transactional
    public Carrito eliminarItem(Integer usuarioId, Integer itemId) {
        Carrito carrito = obtenerCarrito(usuarioId);

        boolean eliminado = carrito.getItems()
                .removeIf(i -> i.getId().equals(itemId));

        if (!eliminado) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        return carritoRepository.save(carrito);
    }

    // ── Vaciar carrito ───────────────────────────────────────
    @Transactional
    public void vaciar(Integer usuarioId) {
        Carrito carrito = obtenerCarrito(usuarioId);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

    // ── Crear carrito nuevo ──────────────────────────────────
    private Carrito crearCarrito(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Carrito carrito = Carrito.builder()
                .usuario(usuario)
                .build();

        return carritoRepository.save(carrito);
    }
}