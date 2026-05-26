package com.escuela.estudiantes.exception;

import com.escuela.estudiantes.controller.EstudianteController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mapea excepciones del MS a respuestas RFC 7807 (Problem Details).
 *
 * <p>Convencion de URIs de tipo: {@code https://escuela.local/errors/&lt;slug&gt;}.
 * Cada respuesta incluye un {@code correlationId} (UUID) para trazabilidad.</p>
 *
 * <p>Limitado al paquete del {@link EstudianteController} para no interferir
 * con otros handlers eventuales en este MS.</p>
 */
@RestControllerAdvice(basePackageClasses = EstudianteController.class)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_URI = "https://escuela.local/errors/";

    // -----------------------------------------------------------------------
    // 4 excepciones de dominio
    // -----------------------------------------------------------------------

    @ExceptionHandler(EstudianteNotFoundException.class)
    public ProblemDetail handleNotFound(EstudianteNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "estudiante-not-found",
                "Estudiante no encontrado", ex.getMessage());
    }

    @ExceptionHandler(CedulaDuplicadaException.class)
    public ProblemDetail handleCedulaDuplicada(CedulaDuplicadaException ex) {
        return build(HttpStatus.CONFLICT, "cedula-duplicada",
                "Cedula duplicada", ex.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ProblemDetail handleEmailDuplicado(EmailDuplicadoException ex) {
        return build(HttpStatus.CONFLICT, "email-duplicado",
                "Email duplicado", ex.getMessage());
    }

    @ExceptionHandler(CedulaInvalidaException.class)
    public ProblemDetail handleCedulaInvalida(CedulaInvalidaException ex) {
        return build(HttpStatus.BAD_REQUEST, "cedula-invalida",
                "Cedula invalida", ex.getMessage());
    }

    @ExceptionHandler(RecursoNotFoundException.class)
    public ProblemDetail handleRecursoNotFound(RecursoNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "recurso-not-found",
                "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ProblemDetail handleTransicionInvalida(TransicionEstadoInvalidaException ex) {
        return build(HttpStatus.BAD_REQUEST, "transicion-estado-invalida",
                "Transicion de estado no permitida", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "argumento-invalido",
                "Argumento invalido", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Excepciones de seguridad del Controller
    // -----------------------------------------------------------------------

    @ExceptionHandler(EstudianteController.NoAutenticadoException.class)
    public ProblemDetail handleNoAutenticado(EstudianteController.NoAutenticadoException ex) {
        return build(HttpStatus.UNAUTHORIZED, "missing-token",
                "No autorizado", ex.getMessage());
    }

    @ExceptionHandler(EstudianteController.SinPermisoException.class)
    public ProblemDetail handleSinPermiso(EstudianteController.SinPermisoException ex) {
        return build(HttpStatus.FORBIDDEN, "forbidden",
                "Sin permisos suficientes", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Validacion (@Valid) y body malformado
    // -----------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errores.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = build(HttpStatus.BAD_REQUEST, "validation",
                "Datos invalidos", "Validacion fallida en uno o mas campos");
        pd.setProperty("errors", errores);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "malformed-body",
                "Body invalido", "Cuerpo de request invalido o malformado");
    }

    // -----------------------------------------------------------------------
    // Catch-all (500)
    // -----------------------------------------------------------------------

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ProblemDetail handleNoHandler(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create(BASE_URI + "not-found"));
        pd.setTitle("Ruta no encontrada");
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        String correlationId = UUID.randomUUID().toString();
        log.error("Error no controlado [correlationId={}]: {}", correlationId, ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor. Contacte al administrador.");
        pd.setType(URI.create(BASE_URI + "internal"));
        pd.setTitle("Error interno");
        pd.setProperty("correlationId", correlationId);
        return pd;
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private ProblemDetail build(HttpStatus status, String slug, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE_URI + slug));
        pd.setTitle(title);
        pd.setProperty("correlationId", UUID.randomUUID().toString());
        return pd;
    }
}
