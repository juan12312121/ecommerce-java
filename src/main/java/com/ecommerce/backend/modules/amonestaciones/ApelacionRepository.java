package com.ecommerce.backend.modules.amonestaciones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApelacionRepository extends JpaRepository<Apelacion, Integer> {

    List<Apelacion> findByVendedorId(Integer vendedorId);

    List<Apelacion> findByEstado(String estado);

    List<Apelacion> findByAdvertenciaId(Integer advertenciaId);
}