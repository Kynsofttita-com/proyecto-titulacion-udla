---
name: configure-spring-security
description: Configure Spring Security 6 with JWT authentication, RBAC authorization, password encoding (bcrypt), CORS, security headers, and method-level @PreAuthorize. Use when setting up authentication for a microservice or adding new security rules.
---

# Configure Spring Security Skill

Sets up Spring Security 6 with JWT for the project's standard auth model.

## When to Use

- Initial setup of Spring Security in a new microservice
- Adding JWT validation to API Gateway
- Modifying authorization rules (new endpoints, new roles)
- Configuring CORS for frontend
- Adding security headers

## Standard Security Stack

- **Auth**: JWT (HS256 in dev, RS256 in prod)
- **Password**: BCryptPasswordEncoder (cost 12)
- **Sessions**: Stateless
- **CSRF**: disabled (stateless API)
- **Roles**: ADMIN, PERSONAL_ADMINISTRATIVO, INSTRUCTOR, ESTUDIANTE
- **Token Expiry**: 24h access, 7d refresh

## Templates

### 1. SecurityConfig (in each microservice)

```java
package com.kynsoft.<context>.infrastructure.config;

import com.kynsoft.<context>.infrastructure.security.JwtAuthenticationFilter;
import com.kynsoft.<context>.infrastructure.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(c -> {})
                .xssProtection(xss -> xss.disable())
                .httpStrictTransportSecurity(hsts -> hsts.maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            )
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://app.proyecto.local"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Idempotency-Key"));
        config.setExposedHeaders(List.of("X-RateLimit-Remaining", "Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 2. JWT Authentication Filter

```java
package com.kynsoft.<context>.infrastructure.security;

import com.kynsoft.<context>.infrastructure.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProperties properties;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {

        var token = extractToken(request);

        if (token != null) {
            try {
                var claims = parseToken(token);
                var auth = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(),
                    null,
                    extractAuthorities(claims)
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private List<SimpleGrantedAuthority> extractAuthorities(Claims claims) {
        @SuppressWarnings("unchecked")
        var roles = (List<String>) claims.get("roles", List.class);
        return roles == null ? List.of() : roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .toList();
    }
}
```

### 3. JWT Properties

```java
package com.kynsoft.<context>.infrastructure.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @NotBlank String secret,
    String issuer,
    String audience,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration
) {}
```

```yaml
# application.yml
jwt:
  secret: ${JWT_SECRET:change-me-in-prod-min-256-bits}
  issuer: https://auth.proyecto.local
  audience: proyecto-titulacion
  access-token-expiration: PT24H
  refresh-token-expiration: P7D
```

### 4. Authentication Entry Point (returns 401)

```java
package com.kynsoft.<context>.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Authentication required"
        );
        problem.setInstance(java.net.URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getWriter(), problem);
    }
}
```

### 5. Token Generation (in MS-Auth only)

```java
@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    public String generateAccessToken(User user) {
        var now = Instant.now();
        var expiration = now.plus(properties.accessTokenExpiration());

        return Jwts.builder()
            .subject(user.getId().toString())
            .issuer(properties.issuer())
            .audience().add(properties.audience()).and()
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .id(UUID.randomUUID().toString())
            .claim("email", user.getEmail())
            .claim("roles", user.getRoles().stream().map(Role::getName).toList())
            .signWith(Keys.hmacShaKeyFor(properties.secret().getBytes(UTF_8)))
            .compact();
    }
}
```

### 6. Method-Level Authorization Examples

```java
// Single role
@PreAuthorize("hasRole('ADMIN')")

// Multiple roles (any)
@PreAuthorize("hasAnyRole('ADMIN', 'PERSONAL_ADMINISTRATIVO')")

// Role + ownership
@PreAuthorize("hasRole('ADMIN') or @studentSecurity.isOwner(#id, authentication)")

// Custom logic
@PreAuthorize("@vehicleSecurity.canViewMaintenance(#vehicleId, authentication)")
```

```java
@Component("studentSecurity")
@RequiredArgsConstructor
public class StudentSecurity {

    private final StudentRepository repository;

    public boolean isOwner(Long studentId, Authentication auth) {
        if (auth == null) return false;
        var userId = Long.parseLong(auth.getName());
        return repository.findById(studentId)
            .map(s -> s.getUserId().equals(userId))
            .orElse(false);
    }
}
```

## Workflow

1. **Identify** scope: new microservice / new endpoint / config change
2. **Generate** files based on scope
3. **Add** JWT secret to environment variables (never hardcode)
4. **Test** with `curl`:
   ```bash
   # Without token (should be 401)
   curl http://localhost:8081/v1/estudiantes
   
   # With valid token (should be 200)
   TOKEN=$(curl -s -X POST http://localhost:8081/v1/auth/login \
     -d '{"username":"admin","password":"admin"}' \
     -H 'Content-Type: application/json' | jq -r '.accessToken')
   curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/v1/estudiantes
   ```
5. **Verify** roles work correctly
6. **Document** new auth rules in OpenAPI spec

## Security Checklist

- [ ] JWT secret loaded from environment (not hardcoded)
- [ ] Stateless session (no `JSESSIONID` cookie)
- [ ] CSRF disabled (stateless API)
- [ ] CORS configured with explicit origins (not `*`)
- [ ] Security headers applied (HSTS, CSP, X-Content-Type-Options, etc.)
- [ ] BCrypt with cost ≥ 12
- [ ] Public endpoints minimal (`/health`, `/swagger-ui`)
- [ ] All other endpoints require authentication
- [ ] Method-level @PreAuthorize on protected operations
- [ ] Login throttling configured (3 attempts → 15 min lockout)
- [ ] Audit log for security events (login, lockout, password change)

## Notes

- Use `RS256` (asymmetric) in production, `HS256` (symmetric) only in dev
- Refresh tokens stored in Redis with TTL (revocable)
- Access tokens are short-lived (24h max)
- Always validate JWT in API Gateway AND in each microservice (defense in depth)
- For password reset, generate single-use token (UUID) stored in DB with 15-min TTL
- Refer to `security-engineer` agent for OWASP audits
