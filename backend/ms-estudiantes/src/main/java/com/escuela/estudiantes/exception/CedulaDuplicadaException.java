package com.escuela.estudiantes.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lanzada al intentar crear un Estudiante con una cedula que ya existe en la
 * tabla — sea de un registro activo o de uno dado de baja (soft-deleted).
 * El handler global la mapea a HTTP 409 Conflict.
 *
 * <p>Se distingue entre "activo" y "soft-deleted" para dar un mensaje util al
 * usuario: en el primer caso puede corregir el dato; en el segundo debe pedir
 * al admin liberar la cedula (hard-delete del registro antiguo).</p>
 */
public class CedulaDuplicadaException extends RuntimeException {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CedulaDuplicadaException(String cedula) {
        super("Ya existe un estudiante con la cedula " + cedula);
    }

    /**
     * @param cedula     cedula duplicada
     * @param deletedAt  fecha de baja si el registro esta soft-deleted, o {@code null} si esta activo
     */
    public CedulaDuplicadaException(String cedula, LocalDateTime deletedAt) {
        super(deletedAt == null
                ? "Ya existe un estudiante activo con la cedula " + cedula
                : "La cedula " + cedula + " pertenece a un estudiante dado de baja el "
                        + deletedAt.format(FMT)
                        + ". Contacte al administrador para liberarla o reactivar el registro.");
    }
}
