package com.ecommerce.backend.modules.catalogo.variante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValorAtributoRepository extends JpaRepository<ValorAtributo, Integer> {
    java.util.List<ValorAtributo> findByAtributoId(Integer atributoId);
}