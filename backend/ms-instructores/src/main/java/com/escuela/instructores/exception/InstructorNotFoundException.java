package com.escuela.instructores.exception;

public class InstructorNotFoundException extends RuntimeException {
    public InstructorNotFoundException(Long id) {
        super("Instructor no encontrado con id=" + id);
    }
}
