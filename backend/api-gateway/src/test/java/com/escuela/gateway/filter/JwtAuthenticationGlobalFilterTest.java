package com.escuela.gateway.filter;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.common.security.jwt.JwtProperties;
import com.escuela.common.security.jwt.JwtTokenProvider;
import com.escuela.gateway.config.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationGlobalFilterTest {

    private static final String SECRET_64 =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Long USER_ID = 42L;
    private static final String EMAIL = "test@escuela.com";
    private static final List<String> ROLES = List.of("ADMIN", "STAFF");

    private JwtTokenProvider jwtTokenProvider;
    private GatewayProperties gatewayProperties;
    private JwtAuthenticationGlobalFilter filter;
    private GatewayFilterChain chain;
    private ServerWebExchangeCapture chainCapture;

    @BeforeEach
    void setup() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET_64);
        props.setAccessTokenExpirationMinutes(15);
        props.setRefreshTokenExpirationDays(7);
        props.setIssuer("escuela-conduccion");
        jwtTokenProvider = new JwtTokenProvider(props);

        gatewayProperties = new GatewayProperties();
        gatewayProperties.setPublicPaths(List.of(
                "/auth/**", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**"));

        filter = new JwtAuthenticationGlobalFilter(jwtTokenProvider, gatewayProperties);
        chainCapture = new ServerWebExchangeCapture();
        chain = chainCapture;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Path publico /auth/login pasa sin token y agrega Correlation-Id")
    void pathPublicoSinToken() {
        MockServerWebExchange exchange = exchangeFor("POST", "/auth/login", null, null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();  // chain pasó
        ServerHttpRequest forwarded = chainCapture.captured.getRequest();
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.CORRELATION_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Path publico /actuator/health pasa sin token")
    void actuatorPasa() {
        MockServerWebExchange exchange = exchangeFor("GET", "/actuator/health", null, null);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    @DisplayName("Path protegido sin token devuelve 401")
    void protegidoSinToken() {
        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1", null, null);
        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Path protegido con header Bearer valido pasa y agrega X-User-* headers")
    void protegidoConBearerValido() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLES);
        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1",
                "Bearer " + token, null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();  // chain pasó
        ServerHttpRequest forwarded = chainCapture.captured.getRequest();
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.USER_ID)).isEqualTo("42");
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.USER_EMAIL)).isEqualTo(EMAIL);
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.USER_ROLES)).isEqualTo("ADMIN,STAFF");
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.CORRELATION_ID)).isNotBlank();
    }

    @Test
    @DisplayName("Path protegido con cookie accessToken valida pasa")
    void protegidoConCookieValida() {
        String token = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, ROLES);
        MockServerHttpRequest req = MockServerHttpRequest
                .get("/estudiantes/1")
                .cookie(new HttpCookie("accessToken", token))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        ServerHttpRequest forwarded = chainCapture.captured.getRequest();
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.USER_ID)).isEqualTo("42");
    }

    @Test
    @DisplayName("Token malformado devuelve 401")
    void tokenMalformado() {
        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1",
                "Bearer xxx.yyy.zzz", null);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Refresh token en endpoint protegido devuelve 401 (solo se acepta access)")
    void refreshTokenRechazadoEnRutaProtegida() {
        String refresh = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, ROLES);
        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1",
                "Bearer " + refresh, null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Header Authorization sin Bearer prefix se ignora")
    void authSinBearerPrefix() {
        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1",
                "Basic xyz", null);
        filter.filter(exchange, chain).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Token con secret distinto devuelve 401")
    void tokenConSecretDistinto() {
        JwtProperties otraProps = new JwtProperties();
        otraProps.setSecret("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff");
        otraProps.setIssuer("escuela-conduccion");
        JwtTokenProvider otroProvider = new JwtTokenProvider(otraProps);
        String tokenAjeno = otroProvider.generateAccessToken(USER_ID, EMAIL, ROLES);

        MockServerWebExchange exchange = exchangeFor("GET", "/estudiantes/1",
                "Bearer " + tokenAjeno, null);
        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Si request trae X-Correlation-Id, se respeta (no se sobreescribe)")
    void correlationIdExistenteSeRespeta() {
        String preexistente = "abc-123-def";
        MockServerHttpRequest req = MockServerHttpRequest
                .get("/auth/login")  // path publico
                .header(UserHeaders.CORRELATION_ID, preexistente)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(req);

        filter.filter(exchange, chain).block();

        ServerHttpRequest forwarded = chainCapture.captured.getRequest();
        assertThat(forwarded.getHeaders().getFirst(UserHeaders.CORRELATION_ID))
                .isEqualTo(preexistente);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MockServerWebExchange exchangeFor(String method, String path,
                                              String authHeader, String cookieHeader) {
        MockServerHttpRequest.BaseBuilder<?> builder = switch (method) {
            case "GET" -> MockServerHttpRequest.get(path);
            case "POST" -> MockServerHttpRequest.post(path);
            default -> MockServerHttpRequest.get(path);
        };
        if (authHeader != null) {
            builder = builder.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        if (cookieHeader != null) {
            builder = builder.header(HttpHeaders.COOKIE, cookieHeader);
        }
        return MockServerWebExchange.from(builder.build());
    }

    /** Captura el exchange que recibe la chain (para validar headers agregados). */
    private static class ServerWebExchangeCapture implements GatewayFilterChain {
        org.springframework.web.server.ServerWebExchange captured;

        @Override
        public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange) {
            this.captured = exchange;
            return Mono.empty();
        }
    }
}
