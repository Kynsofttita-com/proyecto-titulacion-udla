package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.ProgresoAcademicoResponse;
import com.escuela.estudiantes.dto.UpdateProgresoAcademicoRequest;
import com.escuela.estudiantes.entity.Estudiante;
import com.escuela.estudiantes.entity.ProgresoAcademico;
import com.escuela.estudiantes.exception.EstudianteNotFoundException;
import com.escuela.estudiantes.feign.TipoCursoClient;
import com.escuela.estudiantes.repository.EstudianteRepository;
import com.escuela.estudiantes.repository.ProgresoAcademicoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@Transactional
public class ProgresoAcademicoService {

    private static final Logger log = LoggerFactory.getLogger(ProgresoAcademicoService.class);

    private final ProgresoAcademicoRepository progresoRepository;
    private final EstudianteRepository estudianteRepository;
    private final ObjectProvider<TipoCursoClient> tipoCursoClientProvider;

    public ProgresoAcademicoService(ProgresoAcademicoRepository progresoRepository,
                                    EstudianteRepository estudianteRepository,
                                    ObjectProvider<TipoCursoClient> tipoCursoClientProvider) {
        this.progresoRepository = progresoRepository;
        this.estudianteRepository = estudianteRepository;
        this.tipoCursoClientProvider = tipoCursoClientProvider;
    }

    // =========================================================================
    //  Lectura: combina datos almacenados + derivados
    // =========================================================================

    @Transactional(readOnly = true)
    public ProgresoAcademicoResponse obtener(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));
        ProgresoAcademico p = progresoRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> crearProgresoVacio(estudiante));
        return toResponse(p, estudiante);
    }

    // =========================================================================
    //  Mantenimiento via listener de eventos asignacion.creada/completada/cancelada
    // =========================================================================

    /** Incrementa el contador clasesPlaneadas. Llamado al recibir asignacion.creada. */
    public void incrementarPlaneadas(Long estudianteId) {
        ProgresoAcademico p = upsertVacio(estudianteId);
        short actual = p.getClasesPlaneadas() != null ? p.getClasesPlaneadas() : 0;
        p.setClasesPlaneadas((short) (actual + 1));
        progresoRepository.save(p);
        log.info("ProgresoAcademico estudianteId={} clasesPlaneadas {} -> {}", estudianteId, actual, p.getClasesPlaneadas());
    }

    /** Incrementa clasesCompletadas. Llamado al recibir asignacion.completada. */
    public void incrementarCompletadas(Long estudianteId) {
        ProgresoAcademico p = upsertVacio(estudianteId);
        short actual = p.getClasesCompletadas() != null ? p.getClasesCompletadas() : 0;
        p.setClasesCompletadas((short) (actual + 1));
        progresoRepository.save(p);
        log.info("ProgresoAcademico estudianteId={} clasesCompletadas {} -> {}", estudianteId, actual, p.getClasesCompletadas());
    }

    /** Incrementa clasesCanceladas. Llamado al recibir asignacion.cancelada. */
    public void incrementarCanceladas(Long estudianteId) {
        ProgresoAcademico p = upsertVacio(estudianteId);
        short actual = p.getClasesCanceladas() != null ? p.getClasesCanceladas() : 0;
        p.setClasesCanceladas((short) (actual + 1));
        progresoRepository.save(p);
        log.info("ProgresoAcademico estudianteId={} clasesCanceladas {} -> {}", estudianteId, actual, p.getClasesCanceladas());
    }

    // =========================================================================
    //  Upsert manual (admin/staff)
    // =========================================================================

    public ProgresoAcademicoResponse actualizar(Long estudianteId, UpdateProgresoAcademicoRequest request) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        ProgresoAcademico p = progresoRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> crearProgresoVacio(estudiante));

        if (request.clasesPlaneadas() != null) p.setClasesPlaneadas(request.clasesPlaneadas());
        if (request.clasesCompletadas() != null) p.setClasesCompletadas(request.clasesCompletadas());
        if (request.clasesPendientes() != null) p.setClasesPendientes(request.clasesPendientes());
        if (request.clasesCanceladas() != null) p.setClasesCanceladas(request.clasesCanceladas());
        if (request.calificacionPromedio() != null) p.setCalificacionPromedio(request.calificacionPromedio());
        if (request.aprobado() != null) p.setAprobado(request.aprobado());

        p = progresoRepository.save(p);
        log.info("ProgresoAcademico actualizado manualmente estudianteId={} completadas={}",
                estudianteId, p.getClasesCompletadas());
        return toResponse(p, estudiante);
    }

    // =========================================================================
    //  Helpers internos
    // =========================================================================

    private ProgresoAcademico upsertVacio(Long estudianteId) {
        return progresoRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> {
                    Estudiante est = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                            .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));
                    return progresoRepository.save(crearProgresoVacio(est));
                });
    }

    private ProgresoAcademico crearProgresoVacio(Estudiante estudiante) {
        return ProgresoAcademico.builder()
                .estudiante(estudiante)
                .clasesPlaneadas((short) 0)
                .clasesCompletadas((short) 0)
                .clasesPendientes((short) 0)
                .clasesCanceladas((short) 0)
                .build();
    }

    private ProgresoAcademicoResponse toResponse(ProgresoAcademico p, Estudiante e) {
        short planeadas = p.getClasesPlaneadas() != null ? p.getClasesPlaneadas() : 0;
        short completadas = p.getClasesCompletadas() != null ? p.getClasesCompletadas() : 0;
        short canceladas = p.getClasesCanceladas() != null ? p.getClasesCanceladas() : 0;
        int pendientes = Math.max(0, planeadas - completadas - canceladas);

        BigDecimal horasCompletadas = (e.getMinutosCompletados() != null)
                ? BigDecimal.valueOf(e.getMinutosCompletados())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Integer horasRequeridas = obtenerHorasRequeridas(e.getTipoCursoId());

        int porcentaje = 0;
        if (horasRequeridas != null && horasRequeridas > 0) {
            porcentaje = horasCompletadas
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(horasRequeridas), 0, RoundingMode.HALF_UP)
                    .intValue();
            porcentaje = Math.min(100, Math.max(0, porcentaje));
        }

        return new ProgresoAcademicoResponse(
                p.getId(),
                p.getEstudiante().getId(),
                p.getClasesPlaneadas(),
                p.getClasesCompletadas(),
                (short) pendientes,
                p.getClasesCanceladas(),
                p.getCalificacionPromedio(),
                p.getAprobado(),
                horasCompletadas,
                horasRequeridas != null ? horasRequeridas : 0,
                porcentaje,
                (int) completadas,
                pendientes
        );
    }

    /**
     * Consulta a ms-auth la duracion total del curso del estudiante. Si el
     * estudiante no tiene curso asignado, o ms-auth no responde, devuelve null
     * (porcentaje quedara en 0% sin romper la respuesta).
     */
    private Integer obtenerHorasRequeridas(Long tipoCursoId) {
        if (tipoCursoId == null) return null;
        TipoCursoClient client = tipoCursoClientProvider.getIfAvailable();
        if (client == null) {
            log.warn("TipoCursoClient no disponible, no se puede calcular horasRequeridas");
            return null;
        }
        try {
            Map<String, Object> tc = client.obtener(tipoCursoId);
            Object dur = tc.get("duracionTotalHoras");
            if (dur instanceof Number n) return n.intValue();
            return null;
        } catch (Exception ex) {
            log.warn("Error consultando tipoCurso {}: {}", tipoCursoId, ex.getMessage());
            return null;
        }
    }
}
