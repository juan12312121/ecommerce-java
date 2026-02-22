package com.ecommerce.backend.modules.amonestaciones;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReporteTiendaRepository extends JpaRepository<ReporteTienda, Integer> {

    List<ReporteTienda> findByVendedorId(Integer vendedorId);

    List<ReporteTienda> findByEstado(String estado);

    List<ReporteTienda> findByReportadoPorId(Integer reportadoPorId);
}