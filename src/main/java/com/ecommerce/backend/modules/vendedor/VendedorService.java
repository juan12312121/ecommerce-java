package com.ecommerce.backend.modules.vendedor;

import com.ecommerce.backend.modules.usuario.Rol;
import com.ecommerce.backend.modules.usuario.RolRepository;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.enums.EstadoVendedor;
import com.ecommerce.backend.shared.enums.RolNombre;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendedorService {

    private final VendedorRepository vendedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    // ── Obtener tienda por slug (público) ────────────────────
    public Vendedor obtenerPorSlug(String slug) {
        return vendedorRepository.findBySlugConUsuario(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda", "slug", slug));
    }

    // ── Obtener tienda por ID ────────────────────────────────
    public Vendedor obtenerPorId(Integer id) {
        return vendedorRepository.findByIdConUsuario(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda", "id", id));
    }

    // ── Obtener mi tienda (vendedor autenticado) ─────────────
    public Vendedor obtenerMiTienda(Integer usuarioId) {
        return vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));
    }

    // ── Listar tiendas aprobadas (público) ───────────────────
    public List<Vendedor> listarAprobados() {
        return vendedorRepository.findByEstadoOrderByNombreTiendaAsc(EstadoVendedor.APROBADO);
    }

    // ── Listar por estado (admin) ────────────────────────────
    public List<Vendedor> listarPorEstado(EstadoVendedor estado) {
        return vendedorRepository.findByEstado(estado);
    }

    // ── Registrar tienda ─────────────────────────────────────
    @Transactional
    public Vendedor registrar(VendedorRequest request, Integer usuarioId) {
        if (vendedorRepository.existsByUsuarioId(usuarioId)) {
            throw new BadRequestException("Ya tienes una tienda registrada");
        }

        if (vendedorRepository.existsBySlugTienda(request.getSlugTienda())) {
            throw new BadRequestException("El nombre de tienda ya está en uso");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        // Asignar rol VENDEDOR al usuario
        Rol rolVendedor = rolRepository.findByNombre(RolNombre.VENDEDOR)
                .orElseThrow(() -> new ResourceNotFoundException("Rol VENDEDOR no encontrado"));
        usuario.agregarRol(rolVendedor);
        usuarioRepository.save(usuario);

        Vendedor vendedor = Vendedor.builder()
                .usuario(usuario)
                .nombreTienda(request.getNombreTienda())
                .slugTienda(request.getSlugTienda())
                .descripcion(request.getDescripcion())
                .estado(EstadoVendedor.PENDIENTE)
                .build();

        return vendedorRepository.save(vendedor);
    }

    // ── Actualizar mi tienda ─────────────────────────────────
    @Transactional
    public Vendedor actualizar(VendedorRequest request, Integer usuarioId) {
        Vendedor vendedor = obtenerMiTienda(usuarioId);

        if (!vendedor.getSlugTienda().equals(request.getSlugTienda()) &&
                vendedorRepository.existsBySlugTienda(request.getSlugTienda())) {
            throw new BadRequestException("El nombre de tienda ya está en uso");
        }

        vendedor.setNombreTienda(request.getNombreTienda());
        vendedor.setSlugTienda(request.getSlugTienda());
        vendedor.setDescripcion(request.getDescripcion());

        if (request.getLogoUrl() != null)
            vendedor.setLogoUrl(request.getLogoUrl());
        if (request.getBannerUrl() != null)
            vendedor.setBannerUrl(request.getBannerUrl());

        return vendedorRepository.save(vendedor);
    }

    // ── Aprobar tienda (admin) ───────────────────────────────
    @Transactional
    public Vendedor aprobar(Integer id) {
        Vendedor vendedor = obtenerPorId(id);

        if (vendedor.getEstado() == EstadoVendedor.APROBADO) {
            throw new BadRequestException("La tienda ya está aprobada");
        }

        vendedor.setEstado(EstadoVendedor.APROBADO);
        return vendedorRepository.save(vendedor);
    }

    // ── Suspender tienda (admin) ─────────────────────────────
    @Transactional
    public Vendedor suspender(Integer id, String razon) {
        Vendedor vendedor = obtenerPorId(id);

        if (vendedor.getEstado() == EstadoVendedor.SUSPENDIDO_PERM) {
            throw new BadRequestException("La tienda ya está suspendida permanentemente");
        }

        vendedor.setEstado(EstadoVendedor.SUSPENDIDO_TEMP);
        vendedor.setRazonSuspension(razon);
        return vendedorRepository.save(vendedor);
    }

    // ── Reactivar tienda (admin) ─────────────────────────────
    @Transactional
    public Vendedor reactivar(Integer id) {
        Vendedor vendedor = obtenerPorId(id);

        if (vendedor.getEstado() == EstadoVendedor.SUSPENDIDO_PERM) {
            throw new ForbiddenException("No se puede reactivar una tienda suspendida permanentemente");
        }

        vendedor.setEstado(EstadoVendedor.APROBADO);
        vendedor.setRazonSuspension(null);
        vendedor.setSuspendidoHasta(null);
        return vendedorRepository.save(vendedor);
    }
}