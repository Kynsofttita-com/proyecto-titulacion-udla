package com.escuela.auth.controller;

import com.escuela.auth.exception.DuplicateResourceException;
import com.escuela.auth.exception.NoAutenticadoException;
import com.escuela.auth.exception.ResourceNotFoundException;
import com.escuela.auth.exception.SinPermisoException;
import com.escuela.auth.service.AuthService.AccountLockedException;
import com.escuela.auth.service.AuthService.InvalidCredentialsException;
import com.escuela.auth.service.AuthService.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler global de excepciones de MS-Auth.
 *
 * <p>Aplica a TODOS los controllers de MS-Auth: AuthController, ConfiguracionController,
 * UsuarioController, TipoCursoController, ConceptoFacturacionController,
 * CategoriaLicenciaController, PlantillaEmailController.</p>
 *
 * <p>Devuelve respuestas RFC 7807 (Problem Details).</p>
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    // --------- Auth ---------

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials",
                "Credenciales invalidas", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleAccountLocked(AccountLockedException ex) {
        return problem(HttpStatus.LOCKED, "account-locked",
                "Cuenta bloqueada", ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "invalid-token",
                "Token invalido", ex.getMessage());
    }

    // --------- CRUD comunes ---------

    @ExceptionHandler(NoAutenticadoException.class)
    public ProblemDetail handleNoAutenticado(NoAutenticadoException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "no-autenticado",
                "Autenticacion requerida", ex.getMessage());
    }

    @ExceptionHandler(SinPermisoException.class)
    public ProblemDetail handleSinPermiso(SinPermisoException ex) {
        return problem(HttpStatus.FORBIDDEN, "sin-permiso",
                "Permisos insuficientes", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "not-found",
                "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return problem(HttpStatus.CONFLICT, "duplicado",
                "Recurso duplicado", ex.getMessage());
    }

    // --------- Validacion / body ---------

    /**
     * Sin este handler, los errores de @Valid quedan con response body vacio y
     * Spring Security los traduce a 403. Aqui devolvemos 400 explicito con detalle
     * de cada campo invalido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errores.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation",
                "Datos invalidos", "Validacion fallida en uno o mas campos");
        pd.setProperty("errors", errores);
        return pd;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "malformed-body",
                "Body invalido", "Cuerpo de request invalido o malformado");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "argumento-invalido",
                "Argumento invalido", ex.getMessage());
    }

    /**
     * Red de seguridad: convierte cualquier violacion de UNIQUE que se escape
     * del service (ej. usuario duplicado por email/cedula) en 409 con detail util.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause().getMessage();
        String detail;
        if (raw != null && raw.contains("uq_usuarios_email")) {
            detail = "El email ya esta registrado (posiblemente en un usuario dado de baja).";
        } else if (raw != null && raw.contains("uq_usuarios_cedula")) {
            detail = "La cedula ya esta registrada (posiblemente en un usuario dado de baja).";
        } else {
            detail = "Conflicto de integridad de datos.";
        }
        log.warn("DataIntegrityViolation atrapada por red de seguridad: {}", raw);
        return problem(HttpStatus.CONFLICT, "data-integrity", "Conflicto de datos", detail);
    }

    // --------- Helper ---------

    private ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://escuela.local/errors/" + type));
        pd.setTitle(title);
        return pd;
    }
}
