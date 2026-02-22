package com.ecommerce.backend.modules.usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // ── Buscar por correo ────────────────────────────────────
    Optional<Usuario> findByCorreo(String correo);

    // ── Verificar si existe un correo ────────────────────────
    boolean existsByCorreo(String correo);

    // ── Buscar usuario activo por correo ─────────────────────
    Optional<Usuario> findByCorreoAndEstaActivoTrue(String correo);

    // ── Buscar usuario con sus roles cargados ────────────────
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.roles WHERE u.correo = :correo")
    Optional<Usuario> findByCorreoConRoles(@Param("correo") String correo);

    // ── Buscar usuario con sus direcciones ───────────────────
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.direcciones WHERE u.id = :id")
    Optional<Usuario> findByIdConDirecciones(@Param("id") Integer id);
}