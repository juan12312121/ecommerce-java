package com.ecommerce.backend.modules.amonestaciones;

import com.ecommerce.backend.modules.amonestaciones.dto.*;
import com.ecommerce.backend.modules.usuario.Usuario;
import com.ecommerce.backend.modules.usuario.UsuarioRepository;
import com.ecommerce.backend.shared.exception.BadRequestException;
import com.ecommerce.backend.shared.exception.ForbiddenException;
import com.ecommerce.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AmonestacionesService {

    private final ApelacionRepository apelacionRepository;
    private final ReporteTiendaRepository reporteTiendaRepository;
    private final UsuarioRepository usuarioRepository;

    // ══════════════════════════════════════════════════════════
    // VENDEDOR — ver sus reportes y apelar
    // ══════════════════════════════════════════════════════════

    // El vendedor apela un reporte resuelto
    @Transactional
    public ApelacionDTO apelarReporte(Integer reporteId, ApelarReporteRequest request, Integer usuarioId) {

        ReporteTienda reporte = buscarReporte(reporteId);

        // Verificar que el reporte pertenece al vendedor
        if (!reporte.getVendedor().getId().equals(usuarioId)) {
            throw new ForbiddenException("No puedes apelar este reporte");
        }

        // Solo se pueden apelar reportes resueltos
        if (!"RESUELTO".equals(reporte.getEstado()) && !"REVISADO".equals(reporte.getEstado())) {
            throw new BadRequestException("Solo puedes apelar reportes que ya han sido resueltos");
        }

        // Verificar que no haya apelado ya este reporte
        boolean yaApelo = apelacionRepository.findByAdvertenciaId(reporteId)
                .stream()
                .anyMatch(a -> a.getVendedor().getId().equals(usuarioId));
        if (yaApelo) {
            throw new BadRequestException("Ya has apelado este reporte anteriormente");
        }

        Usuario vendedor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", usuarioId));

        Apelacion apelacion = Apelacion.builder()
                .vendedor(vendedor)
                .advertenciaId(reporteId)
                .descripcion(request.getDescripcion())
                .estado("PENDIENTE")
                .build();

        return mapApelacion(apelacionRepository.save(apelacion));
    }

    // ══════════════════════════════════════════════════════════
    // APELACIONES
    // ══════════════════════════════════════════════════════════

    public List<ApelacionDTO> listarApelaciones() {
        return apelacionRepository.findAll()
                .stream().map(this::mapApelacion).collect(Collectors.toList());
    }

    public List<ApelacionDTO> listarApelacionesPorEstado(String estado) {
        return apelacionRepository.findByEstado(estado)
                .stream().map(this::mapApelacion).collect(Collectors.toList());
    }

    public List<ApelacionDTO> listarApelacionesPorVendedor(Integer vendedorId) {
        return apelacionRepository.findByVendedorId(vendedorId)
                .stream().map(this::mapApelacion).collect(Collectors.toList());
    }

    public ApelacionDTO obtenerApelacion(Integer id) {
        return mapApelacion(buscarApelacion(id));
    }

    @Transactional
    public ApelacionDTO resolverApelacion(Integer id, ResolverApelacionRequest request, Integer adminId) {
        Apelacion apelacion = buscarApelacion(id);

        if (!"PENDIENTE".equals(apelacion.getEstado())) {
            throw new BadRequestException("Solo se pueden resolver apelaciones en estado PENDIENTE");
        }

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

        apelacion.setEstado(request.getEstado());
        apelacion.setRespuestaAdmin(request.getRespuestaAdmin());
        apelacion.setRevisadoPor(admin);
        apelacion.setResueltoEn(LocalDateTime.now());

        return mapApelacion(apelacionRepository.save(apelacion));
    }

    // ══════════════════════════════════════════════════════════
    // REPORTES DE TIENDA
    // ══════════════════════════════════════════════════════════

    public List<ReporteTiendaDTO> listarReportes() {
        return reporteTiendaRepository.findAll()
                .stream().map(this::mapReporte).collect(Collectors.toList());
    }

    public List<ReporteTiendaDTO> listarReportesPorEstado(String estado) {
        return reporteTiendaRepository.findByEstado(estado)
                .stream().map(this::mapReporte).collect(Collectors.toList());
    }

    public List<ReporteTiendaDTO> listarReportesPorVendedor(Integer vendedorId) {
        return reporteTiendaRepository.findByVendedorId(vendedorId)
                .stream().map(this::mapReporte).collect(Collectors.toList());
    }

    public ReporteTiendaDTO obtenerReporte(Integer id) {
        return mapReporte(buscarReporte(id));
    }

    @Transactional
    public ReporteTiendaDTO revisarReporte(Integer id, RevisarReporteTiendaRequest request, Integer adminId) {
        ReporteTienda reporte = buscarReporte(id);

        if (!"PENDIENTE".equals(reporte.getEstado())) {
            throw new BadRequestException("Solo se pueden revisar reportes en estado PENDIENTE");
        }

        Usuario admin = usuarioRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", adminId));

        reporte.setEstado(request.getEstado());
        reporte.setNotaResolucion(request.getNotaResolucion());
        reporte.setRevisadoPor(admin);
        reporte.setRevisadoEn(LocalDateTime.now());

        return mapReporte(reporteTiendaRepository.save(reporte));
    }

    // ══════════════════════════════════════════════════════════
    // HELPERS PRIVADOS
    // ══════════════════════════════════════════════════════════

    private Apelacion buscarApelacion(Integer id) {
        return apelacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apelacion", "id", id));
    }

    private ReporteTienda buscarReporte(Integer id) {
        return reporteTiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ReporteTienda", "id", id));
    }

    private ApelacionDTO mapApelacion(Apelacion a) {
        return ApelacionDTO.builder()
                .id(a.getId())
                .vendedorId(a.getVendedor() != null ? a.getVendedor().getId() : null)
                .vendedorNombre(a.getVendedor() != null ? a.getVendedor().getNombreCompleto() : null)
                .advertenciaId(a.getAdvertenciaId())
                .descripcion(a.getDescripcion())
                .estado(a.getEstado())
                .revisadoPorId(a.getRevisadoPor() != null ? a.getRevisadoPor().getId() : null)
                .revisadoPorNombre(a.getRevisadoPor() != null ? a.getRevisadoPor().getNombreCompleto() : null)
                .respuestaAdmin(a.getRespuestaAdmin())
                .creadoEn(a.getCreadoEn())
                .resueltoEn(a.getResueltoEn())
                .build();
    }

    private ReporteTiendaDTO mapReporte(ReporteTienda r) {
        // Buscar si tiene apelación asociada
        List<Apelacion> apelaciones = apelacionRepository.findByAdvertenciaId(r.getId());
        ApelacionDTO apelacionDTO = apelaciones.isEmpty() ? null : mapApelacion(apelaciones.get(0));

        return ReporteTiendaDTO.builder()
                .id(r.getId())
                .vendedorId(r.getVendedor() != null ? r.getVendedor().getId() : null)
                .vendedorNombre(r.getVendedor() != null ? r.getVendedor().getNombreCompleto() : null)
                .reportadoPorId(r.getReportadoPor() != null ? r.getReportadoPor().getId() : null)
                .reportadoPorNombre(r.getReportadoPor() != null ? r.getReportadoPor().getNombreCompleto() : null)
                .motivo(r.getMotivo())
                .descripcion(r.getDescripcion())
                .estado(r.getEstado())
                .revisadoPorId(r.getRevisadoPor() != null ? r.getRevisadoPor().getId() : null)
                .revisadoPorNombre(r.getRevisadoPor() != null ? r.getRevisadoPor().getNombreCompleto() : null)
                .revisadoEn(r.getRevisadoEn())
                .notaResolucion(r.getNotaResolucion())
                .apelacion(apelacionDTO) // ← incluye apelación si existe
                .creadoEn(r.getCreadoEn())
                .build();
    }
}