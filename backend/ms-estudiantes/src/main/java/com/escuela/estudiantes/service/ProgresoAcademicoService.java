package com.escuela.estudiantes.service;

import com.escuela.estudiantes.dto.ProgresoAcademicoHorasResponse;
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

    @Transactional(readOnly = true)
    public ProgresoAcademicoResponse obtener(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));
        ProgresoAcademico p = progresoRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> crearProgresoVacio(estudiante));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public ProgresoAcademicoHorasResponse obtenerHoras(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findByIdAndDeletedAtIsNull(estudianteId)
                .orElseThrow(() -> new EstudianteNotFoundException(estudianteId));

        // Convertir minutos completados a horas (redondeando hacia arriba)
        Integer horasCompletadas = estudiante.getMinutosCompletados() != null
            ? (int) Math.ceil(estudiante.getMinutosCompletados() / 60.0)
            : 0;

        // Horas requeridas: consulta al catalogo tipos-curso via MS-Auth.
        // Fallback a 0 si el estudiante no tiene tipoCursoId asignado o si
        // MS-Auth esta caido (mejor 0 honesto que 120 hardcodeado).
        Integer horasRequeridas = obtenerHorasRequeridasSafe(estudiante.getTipoCursoId());
        if (horasRequeridas == null) horasRequeridas = 0;

        // Calcular porcentaje
        Integer porcentajeComplecion = horasRequeridas > 0
            ? Math.min((int) ((horasCompletadas * 100.0) / horasRequeridas), 100)
            : 0;

        // Obtener asignaciones completadas/pendientes
        ProgresoAcademico p = progresoRepository.findByEstudianteId(estudianteId)
                .orElseGet(() -> crearProgresoVacio(estudiante));

        Integer asignacionesCompletadas = p.getClasesCompletadas() != null ? p.getClasesCompletadas().intValue() : 0;
        Integer asignacionesPendientes = p.getClasesPendientes() != null ? p.getClasesPendientes().intValue() : 0;

        return new ProgresoAcademicoHorasResponse(
            horasCompletadas,
            horasRequeridas,
            porcentajeComplecion,
            asignacionesCompletadas,
            asignacionesPendientes
        );
    }

    /** Upsert: crea o actualiza el progreso del estudiante. */
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
        log.info("ProgresoAcademico actualizado estudianteId={} completadas={}",
                estudianteId, p.getClasesCompletadas());
        return toResponse(p);
    }

    // --------- helpers ---------

    /**
     * Consulta MS-Auth por el tipo de curso y devuelve {@code duracionTotalHoras}.
     * Devuelve {@code null} si el estudiante no tiene tipo_curso_id asignado, si
     * MS-Auth esta caido, o si la respuesta no incluye el campo.
     */
    private Integer obtenerHorasRequeridasSafe(Long tipoCursoId) {
        if (tipoCursoId == null) return null;
        TipoCursoClient client = tipoCursoClientProvider.getIfAvailable();
        if (client == null) {
            log.debug("TipoCursoClient no disponible; horas requeridas = null");
            return null;
        }
        try {
            Map<String, Object> tc = client.obtener(tipoCursoId);
            Object horas = tc == null ? null : tc.get("duracionTotalHoras");
            if (horas instanceof Number n) return n.intValue();
            if (horas != null) return Integer.parseInt(horas.toString());
            return null;
        } catch (Exception ex) {
            log.warn("Fallo consultar tipoCurso id={} para horas requeridas: {}", tipoCursoId, ex.getMessage());
            return null;
        }
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

    private ProgresoAcademicoResponse toResponse(ProgresoAcademico p) {
        return new ProgresoAcademicoResponse(p.getId(),
                p.getEstudiante().getId(),
                p.getClasesPlaneadas(),
                p.getClasesCompletadas(),
                p.getClasesPendientes(),
                p.getClasesCanceladas(),
                p.getCalificacionPromedio(),
                p.getAprobado());
    }
}
