package com.ecommerce.backend.modules.cupon;

import com.ecommerce.backend.modules.vendedor.Vendedor;
import com.ecommerce.backend.modules.vendedor.VendedorRepository;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuponService {

    private final CuponRepository cuponRepository;
    private final VendedorRepository vendedorRepository;

    // ── Validar y obtener cupón por código ───────────────────
    public Cupon validarCupon(String codigo, BigDecimal montoOrden) {
        Cupon cupon = cuponRepository.findCuponValido(codigo)
                .orElseThrow(() -> new BadRequestException(
                        "El cupón '" + codigo + "' no es válido, está vencido o agotado"));

        if (montoOrden.compareTo(cupon.getMontoMinimoOrden()) < 0) {
            throw new BadRequestException(
                    "El monto mínimo para usar este cupón es $" + cupon.getMontoMinimoOrden());
        }

        return cupon;
    }

    // ── Calcular descuento ───────────────────────────────────
    public BigDecimal calcularDescuento(Cupon cupon, BigDecimal subtotal) {
        if (cupon.getTipo().equals("PORCENTAJE")) {
            return subtotal.multiply(cupon.getValor()).divide(BigDecimal.valueOf(100));
        } else {
            // MONTO_FIJO — no puede ser mayor al subtotal
            return cupon.getValor().min(subtotal);
        }
    }

    // ── Obtener cupón por ID ─────────────────────────────────
    public Cupon obtenerPorId(Integer id) {
        return cuponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cupón", "id", id));
    }

    // ── Listar cupones del vendedor autenticado ──────────────
    public List<Cupon> listarMisCupones(Integer usuarioId) {
        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));
        return cuponRepository.findByVendedorIdAndEstaActivoTrue(vendedor.getId());
    }

    // ── Listar cupones globales (admin) ──────────────────────
    public List<Cupon> listarGlobales() {
        return cuponRepository.findByVendedorIsNullAndEstaActivoTrue();
    }

    // ── Crear cupón de vendedor ──────────────────────────────
    @Transactional
    public Cupon crear(CuponRequest request, Integer usuarioId) {
        if (cuponRepository.existsByCodigo(request.getCodigo())) {
            throw new BadRequestException("El código '" + request.getCodigo() + "' ya está en uso");
        }

        Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));

        Cupon cupon = Cupon.builder()
                .vendedor(vendedor)
                .codigo(request.getCodigo().toUpperCase())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .montoMinimoOrden(request.getMontoMinimoOrden() != null
                        ? request.getMontoMinimoOrden()
                        : BigDecimal.ZERO)
                .maximoUsos(request.getMaximoUsos())
                .iniciaEn(request.getIniciaEn())
                .expiraEn(request.getExpiraEn())
                .estaActivo(true)
                .build();

        return cuponRepository.save(cupon);
    }

    // ── Crear cupón global (solo ADMIN) ─────────────────────
    @Transactional
    public Cupon crearGlobal(CuponRequest request) {
        if (cuponRepository.existsByCodigo(request.getCodigo())) {
            throw new BadRequestException("El código '" + request.getCodigo() + "' ya está en uso");
        }

        Cupon cupon = Cupon.builder()
                .vendedor(null)
                .codigo(request.getCodigo().toUpperCase())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .montoMinimoOrden(request.getMontoMinimoOrden() != null
                        ? request.getMontoMinimoOrden()
                        : BigDecimal.ZERO)
                .maximoUsos(request.getMaximoUsos())
                .iniciaEn(request.getIniciaEn())
                .expiraEn(request.getExpiraEn())
                .estaActivo(true)
                .build();

        return cuponRepository.save(cupon);
    }

    // ── Desactivar cupón ─────────────────────────────────────
    @Transactional
    public void desactivar(Integer id, Integer usuarioId, boolean esAdmin) {
        Cupon cupon = obtenerPorId(id);

        if (!esAdmin) {
            Vendedor vendedor = vendedorRepository.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> new BadRequestException("No tienes una tienda registrada"));

            if (cupon.getVendedor() == null ||
                    !cupon.getVendedor().getId().equals(vendedor.getId())) {
                throw new ForbiddenException("No tienes permiso para desactivar este cupón");
            }
        }

        cupon.setEstaActivo(false);
        cuponRepository.save(cupon);
    }
}