package com.escuela.instructores.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Handler global RFC 7807 para MS-Instructores. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_URI = "https://escuela.local/errors/";

    @ExceptionHandler(InstructorNotFoundException.class)
    public ProblemDetail handleInstructorNotFound(InstructorNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "instructor-not-found",
                "Instructor no encontrado", ex.getMessage());
    }

    @ExceptionHandler(RecursoNotFoundException.class)
    public ProblemDetail handleRecursoNotFound(RecursoNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "recurso-not-found",
                "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, "duplicado", "Recurso duplicado", ex.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ProblemDetail handleNoAutenticado(NoAutenticadoException ex) {
        return build(HttpStatus.UNAUTHORIZED, "no-autenticado", "No autenticado", ex.getMessage());
    }

    @ExceptionHandler(SinPermisoException.class)
    public ProblemDetail handleSinPermiso(SinPermisoException ex) {
        return build(HttpStatus.FORBIDDEN, "sin-permiso", "Sin permisos", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "argumento-invalido",
                "Argumento invalido", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errores.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "validation",
                "Datos invalidos", "Validacion fallida");
        pd.setProperty("errors", errores);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "malformed-body",
                "Body invalido", "Cuerpo de request invalido o malformado");
    }

    private ProblemDetail build(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE_URI + slug));
        pd.setTitle(title);
        pd.setProperty("correlationId", UUID.randomUUID().toString());
        return pd;
    }
}
