package com.escuela.auth.controller;

import com.escuela.auth.dto.ForgotPasswordRequest;
import com.escuela.auth.dto.LoginRequest;
import com.escuela.auth.dto.LoginResponse;
import com.escuela.auth.dto.RefreshRequest;
import com.escuela.auth.dto.ResetPasswordRequest;
import com.escuela.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints de autenticacion.
 *
 * <p>Todos publicos (no requieren JWT previo). Configurados en
 * {@link com.escuela.auth.config.SecurityConfig} con {@code permitAll}.</p>
 *
 * <p>Los tokens se devuelven tanto en JSON (clientes Postman/test) como en
 * cookies HttpOnly (clientes web Vue, mejor proteccion contra XSS).</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String ACCESS_COOKIE = "accessToken";
    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        HttpHeaders headers = buildAuthCookies(
                response.accessToken(), response.accessTokenExpiresInSeconds(),
                response.refreshToken(), response.refreshTokenExpiresInSeconds());
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request.refreshToken());
        HttpHeaders headers = buildAuthCookies(
                response.accessToken(), response.accessTokenExpiresInSeconds(),
                response.refreshToken(), response.refreshTokenExpiresInSeconds());
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        HttpHeaders headers = clearAuthCookies();
        return ResponseEntity.noContent().headers(headers).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        // Anti-enumeration: 202 siempre, exista o no el email
        return ResponseEntity.accepted().body(Map.of(
                "status", "Si el email existe, se envio un link de recuperacion"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    // -----------------------------------------------------------------------
    // Cookie helpers
    // -----------------------------------------------------------------------

    private HttpHeaders buildAuthCookies(String accessToken, long accessMaxAge,
                                         String refreshToken, long refreshMaxAge) {
        ResponseCookie access = ResponseCookie.from(ACCESS_COOKIE, accessToken)
                .httpOnly(true)
                .secure(false)  // true en produccion HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(accessMaxAge)
                .build();
        ResponseCookie refresh = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)  // true en produccion HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshMaxAge)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, access.toString());
        headers.add(HttpHeaders.SET_COOKIE, refresh.toString());
        return headers;
    }

    private HttpHeaders clearAuthCookies() {
        ResponseCookie access = ResponseCookie.from(ACCESS_COOKIE, "")
                .httpOnly(true).path("/").maxAge(0).build();
        ResponseCookie refresh = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true).path("/").maxAge(0).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, access.toString());
        headers.add(HttpHeaders.SET_COOKIE, refresh.toString());
        return headers;
    }
}
