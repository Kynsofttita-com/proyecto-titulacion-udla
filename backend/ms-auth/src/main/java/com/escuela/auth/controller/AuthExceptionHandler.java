package com.escuela.auth.controller;

import com.escuela.auth.service.AuthService.AccountLockedException;
import com.escuela.auth.service.AuthService.InvalidCredentialsException;
import com.escuela.auth.service.AuthService.InvalidTokenException;
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
 * Maneja excepciones del flujo de autenticacion devolviendo respuestas RFC 7807
 * (Problem Details). Aplica solo al package del AuthController para no
 * interferir con otros handlers globales.
 */
@RestControllerAdvice(basePackageClasses = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setType(URI.create("https://escuela.local/errors/invalid-credentials"));
        pd.setTitle("Credenciales invalidas");
        return pd;
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleAccountLocked(AccountLockedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.LOCKED, ex.getMessage());
        pd.setType(URI.create("https://escuela.local/errors/account-locked"));
        pd.setTitle("Cuenta bloqueada");
        return pd;
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidToken(InvalidTokenException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        pd.setType(URI.create("https://escuela.local/errors/invalid-token"));
        pd.setTitle("Token invalido");
        return pd;
    }

    /**
     * Sin este handler, los errores de @Valid quedan con response body vacio y
     * Spring Security los traduce a 403 (porque httpBasic/formLogin estan disabled
     * y el entry point por defecto es Http403ForbiddenEntryPoint). Aqui devolvemos
     * 400 explicito con detalle de cada campo invalido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                errores.put(fe.getField(), fe.getDefaultMessage()));
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Validacion fallida en uno o mas campos");
        pd.setType(URI.create("https://escuela.local/errors/validation"));
        pd.setTitle("Datos invalidos");
        pd.setProperty("errors", errores);
        return pd;
    }

    /** Body JSON malformado o tipo incompatible (ej. UUID con formato invalido). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Cuerpo de request invalido o malformado");
        pd.setType(URI.create("https://escuela.local/errors/malformed-body"));
        pd.setTitle("Body invalido");
        return pd;
    }
}
