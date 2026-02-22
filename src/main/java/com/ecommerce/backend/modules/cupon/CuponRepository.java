package com.ecommerce.backend.modules.cupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Integer> {

    // ── Buscar por código ────────────────────────────────────
    Optional<Cupon> findByCodigo(String codigo);

    // ── Verificar si existe un código ───────────────────────
    boolean existsByCodigo(String codigo);

    // ── Cupones activos de un vendedor ───────────────────────
    List<Cupon> findByVendedorIdAndEstaActivoTrue(Integer vendedorId);

    // ── Cupones globales activos (sin vendedor) ──────────────
    List<Cupon> findByVendedorIsNullAndEstaActivoTrue();

    // ── Validar cupón disponible para usar ───────────────────
    @Query("SELECT c FROM Cupon c WHERE c.codigo = :codigo " +
            "AND c.estaActivo = true " +
            "AND (c.iniciaEn IS NULL OR c.iniciaEn <= CURRENT_TIMESTAMP) " +
            "AND (c.expiraEn IS NULL OR c.expiraEn >= CURRENT_TIMESTAMP) " +
            "AND (c.maximoUsos IS NULL OR c.usosActuales < c.maximoUsos)")
    Optional<Cupon> findCuponValido(@Param("codigo") String codigo);
}