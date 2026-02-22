package com.ecommerce.backend.modules.vendedor;

import com.ecommerce.backend.shared.enums.EstadoVendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Integer> {

    // ── Buscar por usuario ───────────────────────────────────
    Optional<Vendedor> findByUsuarioId(Integer usuarioId);

    // ── Buscar por slug de tienda ────────────────────────────
    Optional<Vendedor> findBySlugTienda(String slugTienda);

    // ── Verificar si existe slug ─────────────────────────────
    boolean existsBySlugTienda(String slugTienda);

    // ── Verificar si un usuario ya tiene tienda ──────────────
    boolean existsByUsuarioId(Integer usuarioId);

    // ── Listar por estado ────────────────────────────────────
    List<Vendedor> findByEstado(EstadoVendedor estado);

    // ── Vendedores aprobados ─────────────────────────────────
    List<Vendedor> findByEstadoOrderByNombreTiendaAsc(EstadoVendedor estado);

    // ── Vendedor con su usuario ──────────────────────────────
    @Query("SELECT v FROM Vendedor v JOIN FETCH v.usuario WHERE v.id = :id")
    Optional<Vendedor> findByIdConUsuario(@Param("id") Integer id);

    // ── Vendedor por slug con usuario ────────────────────────
    @Query("SELECT v FROM Vendedor v JOIN FETCH v.usuario WHERE v.slugTienda = :slug")
    Optional<Vendedor> findBySlugConUsuario(@Param("slug") String slug);
}