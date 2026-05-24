package com.escuela.estudiantes.specification;

import com.escuela.estudiantes.entity.Estudiante;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtros dinamicos JPA para listados de Estudiantes.
 *
 * <p>Uso tipico:</p>
 * <pre>
 *   Specification&lt;Estudiante&gt; spec = Specification.where(notDeleted())
 *       .and(estado(filtroEstado))
 *       .and(searchTerm(busqueda));
 *   Page&lt;Estudiante&gt; page = repository.findAll(spec, pageable);
 * </pre>
 */
public final class EstudianteSpecifications {

    private EstudianteSpecifications() {
        // utility class
    }

    /** Filtra registros no eliminados (deleted_at IS NULL). */
    public static Specification<Estudiante> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    /** Filtra por estado exacto (PRE_MATRICULADO, MATRICULADO, CURSANDO, COMPLETADO, RETIRADO). */
    public static Specification<Estudiante> estado(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    /** Filtra por situacion_pago exacto (SIN_DEUDA, PAGO_PARCIAL, AL_DIA, EN_MORA, PAGADO_TOTAL). */
    public static Specification<Estudiante> situacionPago(String situacionPago) {
        if (situacionPago == null || situacionPago.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("situacionPago"), situacionPago);
    }

    /**
     * Busqueda case-insensitive en nombre, apellido, cedula y email.
     * Si {@code search} es null o vacio devuelve null (no filtra).
     */
    public static Specification<Estudiante> searchTerm(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> {
            List<Predicate> ors = new ArrayList<>();
            ors.add(cb.like(cb.lower(root.get("nombre")), pattern));
            ors.add(cb.like(cb.lower(root.get("apellido")), pattern));
            ors.add(cb.like(root.get("cedula"), pattern));
            ors.add(cb.like(cb.lower(root.get("email")), pattern));
            return cb.or(ors.toArray(new Predicate[0]));
        };
    }
}
