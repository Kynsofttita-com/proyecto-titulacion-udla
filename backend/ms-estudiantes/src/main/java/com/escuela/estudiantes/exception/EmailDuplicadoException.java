package com.escuela.estudiantes.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lanzada al intentar usar un email ya registrado por otro estudiante (activo o
 * soft-deleted, dado que el UNIQUE de la tabla es global).
 * El handler global la mapea a HTTP 409 Conflict.
 */
public class EmailDuplicadoException extends RuntimeException {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public EmailDuplicadoException(String email) {
        super("Ya existe un estudiante con el email " + email);
    }

    /**
     * @param email      email duplicado
     * @param deletedAt  fecha de baja si el registro esta soft-deleted, o {@code null} si esta activo
     */
    public EmailDuplicadoException(String email, LocalDateTime deletedAt) {
        super(deletedAt == null
                ? "Ya existe un estudiante activo con el email " + email
                : "El email " + email + " pertenece a un estudiante dado de baja el "
                        + deletedAt.format(FMT)
                        + ". Contacte al administrador para liberarlo o reactivar el registro.");
    }
}
