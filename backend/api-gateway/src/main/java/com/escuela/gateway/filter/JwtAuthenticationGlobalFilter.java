package com.escuela.gateway.filter;

import com.escuela.common.security.headers.UserHeaders;
import com.escuela.common.security.jwt.JwtTokenProvider;
import com.escuela.common.security.jwt.ParsedJwt;
import com.escuela.common.security.jwt.TokenType;
import com.escuela.gateway.config.GatewayProperties;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Filtro global que valida el JWT en cada request al Gateway.
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>Si la ruta esta en {@code escuela.gateway.public-paths} → pasa sin
 *       verificar token (auth, actuator, swagger).</li>
 *   <li>Lee token de cookie {@code accessToken} o header
 *       {@code Authorization: Bearer X}.</li>
 *   <li>Sin token → 401 RFC 7807.</li>
 *   <li>Con token: lo parsea con {@link JwtTokenProvider}. Si falla → 401.</li>
 *   <li>Si valido pero tipo REFRESH (no ACCESS) → 401 (refresh token solo
 *       sirve en {@code POST /auth/refresh}).</li>
 *   <li>Si OK: agrega headers {@link UserHeaders#USER_ID}, {@code X-User-Email},
 *       {@code X-User-Roles}, {@code X-Correlation-Id} a la request hacia el
 *       MS downstream.</li>
 * </ol>
 *
 * <p>Orden: -100 (alto, antes que el route filter por defecto).</p>
 */
@Component
public class JwtAuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationGlobalFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_COOKIE_NAME = "accessToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final GatewayProperties gatewayProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationGlobalFilter(JwtTokenProvider jwtTokenProvider,
                                         GatewayProperties gatewayProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Generar/propagar Correlation-Id (lo agregamos siempre, incluso en publicas)
        String correlationId = request.getHeaders().getFirst(UserHeaders.CORRELATION_ID);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        if (esPathPublico(path)) {
            log.debug("Path publico, skip JWT: {} (correlationId={})", path, correlationId);
            return chain.filter(addCorrelationId(exchange, correlationId));
        }

        String token = extraerToken(request);
        if (token == null) {
            log.debug("Path protegido sin token: {} (correlationId={})", path, correlationId);
            return write401(exchange, "missing-token",
                    "Token de autenticacion requerido", correlationId);
        }

        try {
            ParsedJwt parsed = jwtTokenProvider.parse(token);

            if (parsed.tokenType() != TokenType.ACCESS) {
                log.warn("Token tipo {} usado donde se esperaba ACCESS (path={}, correlationId={})",
                        parsed.tokenType(), path, correlationId);
                return write401(exchange, "invalid-token-type",
                        "Solo se acepta access token aqui", correlationId);
            }

            log.debug("JWT valido: userId={} email={} path={} correlationId={}",
                    parsed.userId(), parsed.email(), path, correlationId);

            ServerHttpRequest enriched = enriquecerRequest(request, parsed, correlationId);
            return chain.filter(exchange.mutate().request(enriched).build());

        } catch (JwtException e) {
            log.info("JWT invalido en path {} (correlationId={}): {}",
                    path, correlationId, e.getMessage());
            return write401(exchange, "invalid-token",
                    "Token invalido o expirado", correlationId);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean esPathPublico(String path) {
        for (String pattern : gatewayProperties.getPublicPaths()) {
            if (pathMatcher.match(pattern, path)) return true;
        }
        return false;
    }

    private String extraerToken(ServerHttpRequest request) {
        // Prioridad 1: header Authorization: Bearer X (clientes API/Postman/tests)
        String auth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth != null && auth.startsWith(BEARER_PREFIX)) {
            String token = auth.substring(BEARER_PREFIX.length()).trim();
            if (!token.isEmpty()) return token;
        }

        // Prioridad 2: cookie HttpOnly accessToken (frontend Vue)
        HttpCookie cookie = request.getCookies().getFirst(ACCESS_COOKIE_NAME);
        if (cookie != null && !cookie.getValue().isBlank()) {
            return cookie.getValue();
        }

        return null;
    }

    private ServerHttpRequest enriquecerRequest(ServerHttpRequest request,
                                                ParsedJwt parsed,
                                                String correlationId) {
        List<String> roles = parsed.roles() != null ? parsed.roles() : List.of();
        return request.mutate()
                .header(UserHeaders.USER_ID, String.valueOf(parsed.userId()))
                .header(UserHeaders.USER_EMAIL, parsed.email())
                .header(UserHeaders.USER_ROLES, String.join(",", roles))
                .header(UserHeaders.CORRELATION_ID, correlationId)
                .build();
    }

    private ServerWebExchange addCorrelationId(ServerWebExchange exchange, String correlationId) {
        ServerHttpRequest req = exchange.getRequest().mutate()
                .header(UserHeaders.CORRELATION_ID, correlationId)
                .build();
        return exchange.mutate().request(req).build();
    }

    private Mono<Void> write401(ServerWebExchange exchange, String errorCode,
                                String detail, String correlationId) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        response.getHeaders().add(UserHeaders.CORRELATION_ID, correlationId);

        String body = String.format(
                "{\"type\":\"https://escuela.local/errors/%s\"," +
                        "\"title\":\"No autorizado\"," +
                        "\"status\":401," +
                        "\"detail\":\"%s\"," +
                        "\"correlationId\":\"%s\"}",
                errorCode, detail, correlationId);

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
