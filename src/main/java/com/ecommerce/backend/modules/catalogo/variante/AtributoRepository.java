package com.ecommerce.backend.modules.catalogo.variante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtributoRepository extends JpaRepository<Atributo, Integer> {
    boolean existsByNombre(String nombre);

    List<Atributo> findAllByOrderByNombreAsc();
}