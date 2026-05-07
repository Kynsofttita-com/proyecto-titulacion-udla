package com.escuela.common.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET_64 =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Long USER_ID = 42L;
    private static final String EMAIL = "test@escuela.com";
    private static final List<String> ROLES = List.of("ADMIN", "STAFF");

    private JwtProperties props;
    private JwtTokenProvider provider;

    @BeforeEach
    void setup() {
        props = new JwtProperties();
        props.setSecret(SECRET_64);
        props.setAccessTokenExpirationMinutes(15);
        props.setRefreshTokenExpirationDays(7);
        props.setIssuer("escuela-conduccion");
        provider = new JwtTokenProvider(props);
    }

    @Test
    @DisplayName("Secret < 64 caracteres lanza IllegalArgumentException")
    void secretCorto() {
        JwtProperties p = new JwtProperties();
        p.setSecret("corto");
        assertThatThrownBy(() -> new JwtTokenProvider(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("64 caracteres");
    }

    @Test
    @DisplayName("Access token: parse devuelve los claims correctos")
    void generarYParsearAccessToken() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);
        ParsedJwt parsed = provider.parse(token);

        assertThat(parsed.userId()).isEqualTo(USER_ID);
        assertThat(parsed.email()).isEqualTo(EMAIL);
        assertThat(parsed.roles()).containsExactly("ADMIN", "STAFF");
        assertThat(parsed.tokenType()).isEqualTo(TokenType.ACCESS);
        assertThat(parsed.jti()).isNotNull();
        assertThat(parsed.issuedAt()).isBefore(Instant.now().plusSeconds(2));
        assertThat(parsed.expiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("Refresh token: parse devuelve los claims correctos")
    void generarYParsearRefreshToken() {
        String token = provider.generateRefreshToken(USER_ID, EMAIL, ROLES);
        ParsedJwt parsed = provider.parse(token);

        assertThat(parsed.tokenType()).isEqualTo(TokenType.REFRESH);
        assertThat(parsed.expiresAt())
                .isAfter(Instant.now().plusSeconds(60L * 60 * 24 * 6));  // > 6 dias
    }

    @Test
    @DisplayName("isValid devuelve true para token valido")
    void isValidTokenValido() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);
        assertThat(provider.isValid(token)).isTrue();
    }

    @Test
    @DisplayName("isValid devuelve false para token corrupto")
    void isValidTokenCorrupto() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);
        String corrupto = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.isValid(corrupto)).isFalse();
    }

    @Test
    @DisplayName("Parse de token con firma alterada lanza JwtException")
    void firmaIncorrecta() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);
        int lastDot = token.lastIndexOf('.');
        // Reemplazar la firma completa con bytes base64url validos pero
        // distintos a la firma real. Cambiar 1 solo char no es suficientemente
        // robusto: dependiendo de la base64url decoding, puede caer en bytes
        // que coincidan en padding y la verificacion no falle (visto en CI Linux).
        String alterado = token.substring(0, lastDot + 1) +
                "ZmFrZS1zaWduYXR1cmUtdGhhdC13aWxsLW5vdC1tYXRjaA";

        assertThatThrownBy(() -> provider.parse(alterado))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Parse con secret distinto lanza SignatureException")
    void secretDistinto() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);

        JwtProperties otrasProps = new JwtProperties();
        otrasProps.setSecret(
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        otrasProps.setIssuer("escuela-conduccion");
        JwtTokenProvider otroProvider = new JwtTokenProvider(otrasProps);

        assertThatThrownBy(() -> otroProvider.parse(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Token expirado lanza ExpiredJwtException")
    void tokenExpirado() throws Exception {
        // Crear provider con expiracion 0 minutos para que expire al instante
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret(SECRET_64);
        shortProps.setAccessTokenExpirationMinutes(-1);  // Ya expirado al crearse
        shortProps.setIssuer("escuela-conduccion");
        JwtTokenProvider shortProvider = new JwtTokenProvider(shortProps);

        String token = shortProvider.generateAccessToken(USER_ID, EMAIL, ROLES);

        assertThatThrownBy(() -> shortProvider.parse(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Issuer distinto al esperado lanza JwtException")
    void issuerDistinto() {
        String token = provider.generateAccessToken(USER_ID, EMAIL, ROLES);

        JwtProperties otroIssuerProps = new JwtProperties();
        otroIssuerProps.setSecret(SECRET_64);
        otroIssuerProps.setIssuer("otro-issuer");
        JwtTokenProvider otroProvider = new JwtTokenProvider(otroIssuerProps);

        assertThatThrownBy(() -> otroProvider.parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Cada token generado tiene un jti unico")
    void jtiUnicoPorToken() {
        String t1 = provider.generateAccessToken(USER_ID, EMAIL, ROLES);
        String t2 = provider.generateAccessToken(USER_ID, EMAIL, ROLES);

        assertThat(provider.parse(t1).jti()).isNotEqualTo(provider.parse(t2).jti());
    }

    /** Helper para forzar un campo via reflection (no usado actualmente, dejado por si se necesita). */
    @SuppressWarnings("unused")
    private static void setField(Object target, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
