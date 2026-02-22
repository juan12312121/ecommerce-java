package com.ecommerce.backend.modules.resena;

import com.ecommerce.backend.modules.catalogo.producto.Producto;
import com.ecommerce.backend.modules.catalogo.producto.ProductoRepository;
import com.ecommerce.backend.modules.orden.ItemOrden;
import com.ecommerce.backend.modules.orden.ItemOrdenRepository;
import com.ecommerce.backend.modules.resena.dto.ResenaDTO;
import com.ecommerce.backend.modules.resena.dto.ResenaRequest;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.dto.PageResponse;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemOrdenRepository itemOrdenRepository;

    // ── Listar reseñas de un producto ────────────────────────
    public PageResponse<ResenaDTO> listarPorProducto(Integer productoId, Pageable pageable) {
        Page<Resena> page = resenaRepository
                .findByProductoIdConDetalles(productoId, pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Mis reseñas ──────────────────────────────────────────
    public PageResponse<ResenaDTO> misResenas(Integer usuarioId, Pageable pageable) {
        Page<Resena> page = resenaRepository
                .findByUsuarioIdOrderByCreadoEnDesc(usuarioId, pageable);
        return PageResponse.desde(page.map(this::mapearDTO));
    }

    // ── Crear reseña ─────────────────────────────────────────
    @Transactional
    public ResenaDTO crear(ResenaRequest request, Integer usuarioId) {

        // Verificar que el item de orden existe y pertenece al usuario
        ItemOrden itemOrden = itemOrdenRepository.findById(request.getItemOrdenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de orden", "id", request.getItemOrdenId()));

        if (!itemOrden.getOrden().getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No puedes reseñar este producto");
        }

        // Verificar que la orden fue entregada
        if (!itemOrden.getOrden().getEstado().equals("ENTREGADO")) {
            throw new BadRequestException(
                    "Solo puedes reseñar productos de órdenes entregadas");
        }

        // Verificar que no haya reseñado antes
        if (resenaRepository.existsByUsuarioIdAndItemOrdenId(
                usuarioId, request.getItemOrdenId())) {
            throw new BadRequestException("Ya has reseñado este producto");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Producto producto = itemOrden.getVariante().getProducto();

        Resena resena = Resena.builder()
                .producto(producto)
                .usuario(usuario)
                .itemOrden(itemOrden)
                .calificacion(request.getCalificacion())
                .titulo(request.getTitulo())
                .comentario(request.getComentario())
                .esVerificada(true)
                .build();

        // Agregar imágenes si se proporcionaron
        if (request.getImagenesUrl() != null && !request.getImagenesUrl().isEmpty()) {
            for (String url : request.getImagenesUrl()) {
                ImagenResena imagen = ImagenResena.builder()
                        .resena(resena)
                        .url(url)
                        .build();
                resena.getImagenes().add(imagen);
            }
        }

        return mapearDTO(resenaRepository.save(resena));
    }

    // ── Eliminar reseña (admin o dueño) ─────────────────────
    @Transactional
    public void eliminar(Integer id, Integer usuarioId, boolean esAdmin) {
        Resena resena = resenaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña", "id", id));

        if (!esAdmin && !resena.getUsuario().getId().equals(usuarioId)) {
            throw new ForbiddenException("No puedes eliminar esta reseña");
        }

        resenaRepository.delete(resena);
    }

    // ── Mapear entidad a DTO ─────────────────────────────────
    private ResenaDTO mapearDTO(Resena r) {
        ResenaDTO dto = new ResenaDTO();
        dto.setId(r.getId());
        dto.setCalificacion(r.getCalificacion());
        dto.setTitulo(r.getTitulo());
        dto.setComentario(r.getComentario());
        dto.setEsVerificada(r.getEsVerificada());
        dto.setCreadoEn(r.getCreadoEn());

        if (r.getUsuario() != null) {
            dto.setUsuarioId(r.getUsuario().getId());
            dto.setNombreUsuario(r.getUsuario().getNombre() + " " + r.getUsuario().getApellido());
            dto.setAvatarUrl(r.getUsuario().getAvatarUrl());
        }

        if (r.getProducto() != null) {
            dto.setProductoId(r.getProducto().getId());
            dto.setProductoNombre(r.getProducto().getNombre());
        }

        if (r.getImagenes() != null) {
            dto.setImagenesUrl(r.getImagenes().stream()
                    .map(ImagenResena::getUrl)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}