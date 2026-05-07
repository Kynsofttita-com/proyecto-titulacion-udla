package com.escuela.auth.controller;

import com.escuela.auth.service.AuthService.AccountLockedException;
import com.escuela.auth.service.AuthService.InvalidCredentialsException;
import com.escuela.auth.service.AuthService.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

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
}
