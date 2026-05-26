package com.escuela.cobros.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler global de excepciones de MS-Cobros. Devuelve respuestas RFC 7807
 * (Problem Details) consistentes con los demas microservicios (ms-auth,
 * ms-estudiantes).
 *
 * <p>Sin este handler todas las excepciones de negocio se mapean a 500 por
 * default de Spring — el cliente recibia errores opacos. Este advice las
 * traduce a codigos HTTP apropiados (404 not found, 409 conflict, 400 bad
 * request) con detalle legible.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --------- Not found (404) ---------

    @ExceptionHandler(FacturaNotFoundException.class)
    public ProblemDetail handleFacturaNotFound(FacturaNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "factura-not-found",
                "Factura no encontrada", ex.getMessage());
    }

    @ExceptionHandler(PagoNotFoundException.class)
    public ProblemDetail handlePagoNotFound(PagoNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "pago-not-found",
                "Pago no encontrado", ex.getMessage());
    }

    @ExceptionHandler(EstudianteNotFoundException.class)
    public ProblemDetail handleEstudianteNotFound(EstudianteNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "estudiante-not-found",
                "Estudiante no encontrado", ex.getMessage());
    }

    // --------- Conflict (409) ---------

    @ExceptionHandler(EstudianteInactivoException.class)
    public ProblemDetail handleEstudianteInactivo(EstudianteInactivoException ex) {
        return problem(HttpStatus.CONFLICT, "estudiante-inactivo",
                "Estudiante inactivo", ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ProblemDetail handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return problem(HttpStatus.CONFLICT, "saldo-insuficiente",
                "Saldo insuficiente", ex.getMessage());
    }

    // --------- Bad request (400) ---------

    /**
     * Sin este handler los errores de @Valid quedan con body vacio y Spring
     * Security los traduce a 403 silencioso. Aqui devolvemos 400 explicito
     * con el detalle de cada campo invalido.
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "?";
        String detail = "Parametro '" + ex.getName() + "' con valor '" + ex.getValue()
                + "' no es del tipo esperado (" + expected + ")";
        return problem(HttpStatus.BAD_REQUEST, "param-type-mismatch",
                "Tipo de parametro invalido", detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArg(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "argumento-invalido",
                "Argumento invalido", ex.getMessage());
    }

    // --------- Helper ---------

    private ProblemDetail problem(HttpStatus status, String type, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://escuela.local/errors/" + type));
        pd.setTitle(title);
        return pd;
    }
}
