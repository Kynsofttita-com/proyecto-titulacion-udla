package com.escuela.auth.service;

import com.escuela.auth.config.AuthProperties;
import com.escuela.auth.dto.LoginRequest;
import com.escuela.auth.dto.LoginResponse;
import com.escuela.auth.entity.PasswordResetToken;
import com.escuela.auth.entity.RefreshToken;
import com.escuela.auth.entity.Rol;
import com.escuela.auth.entity.Usuario;
import com.escuela.auth.repository.PasswordResetTokenRepository;
import com.escuela.auth.repository.RefreshTokenRepository;
import com.escuela.auth.repository.UsuarioRepository;
import com.escuela.common.events.auth.PasswordResetSolicitadoEvent;
import com.escuela.common.events.auth.UsuarioBloqueadoEvent;
import com.escuela.common.security.jwt.JwtProperties;
import com.escuela.common.security.jwt.JwtTokenProvider;
import com.escuela.common.security.jwt.ParsedJwt;
import com.escuela.common.security.jwt.TokenType;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio central de autenticacion: login, refresh con rotation, logout,
 * forgot-password y reset-password. Implementa account lockout tras N
 * intentos fallidos y publica eventos de dominio cuando corresponda.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthProperties authProperties;
    private final AuthEventDispatcher eventDispatcher;

    public AuthService(UsuarioRepository usuarioRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       JwtProperties jwtProperties,
                       AuthProperties authProperties,
                       AuthEventDispatcher eventDispatcher) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
        this.authProperties = authProperties;
        this.eventDispatcher = eventDispatcher;
    }

    // -----------------------------------------------------------------------
    // LOGIN
    // -----------------------------------------------------------------------

    /**
     * <p><b>noRollbackFor:</b> sin esto, cuando lanzamos
     * {@link InvalidCredentialsException} para un password incorrecto, Spring
     * hace rollback de la transaccion y los {@code failed_attempts}
     * incrementados se descartan — el lockout nunca se activaria. Marcando
     * estas excepciones como "no provocan rollback" garantizamos que el
     * cambio en la BD se persista.</p>
     */
    @Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciales invalidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new InvalidCredentialsException("Cuenta inactiva");
        }

        if (estaBloqueado(usuario)) {
            log.info("Login rechazado: cuenta bloqueada {} hasta {}",
                    usuario.getEmail(), usuario.getLockUntil());
            throw new AccountLockedException(
                    "Cuenta bloqueada temporalmente. Intentar despues de " + usuario.getLockUntil());
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            registrarIntentoFallido(usuario);
            throw new InvalidCredentialsException("Credenciales invalidas");
        }

        // Login exitoso: reset contador, registrar last_login
        usuario.setFailedAttempts((short) 0);
        usuario.setLocked(false);
        usuario.setLockUntil(null);
        usuario.setLastLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return generarRespuestaLogin(usuario);
    }

    private boolean estaBloqueado(Usuario u) {
        if (!Boolean.TRUE.equals(u.getLocked())) {
            return false;
        }
        // Si el lockUntil ya paso, desbloquear automaticamente
        if (u.getLockUntil() != null && u.getLockUntil().isBefore(LocalDateTime.now())) {
            u.setLocked(false);
            u.setFailedAttempts((short) 0);
            u.setLockUntil(null);
            return false;
        }
        return true;
    }

    private void registrarIntentoFallido(Usuario usuario) {
        short intentos = (short) (usuario.getFailedAttempts() + 1);
        usuario.setFailedAttempts(intentos);

        if (intentos >= authProperties.getMaxFailedAttempts()) {
            LocalDateTime lockUntil = LocalDateTime.now()
                    .plusMinutes(authProperties.getLockoutDurationMinutes());
            usuario.setLocked(true);
            usuario.setLockUntil(lockUntil);
            usuarioRepository.save(usuario);

            // Publicar evento de bloqueo
            UsuarioBloqueadoEvent event = UsuarioBloqueadoEvent.builder()
                    .usuarioId(usuario.getId())
                    .email(usuario.getEmail())
                    .nombre(usuario.getNombre())
                    .lockUntil(lockUntil.toInstant(ZoneOffset.UTC))
                    .intentosFallidos(intentos)
                    .build();
            eventDispatcher.publishUsuarioBloqueado(event);

            log.warn("Cuenta bloqueada por {} intentos fallidos: {}", intentos, usuario.getEmail());
        } else {
            usuarioRepository.save(usuario);
        }
    }

    private LoginResponse generarRespuestaLogin(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();

        String accessToken = jwtTokenProvider.generateAccessToken(
                usuario.getId(), usuario.getEmail(), roles);
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(
                usuario.getId(), usuario.getEmail(), roles);

        // Persistir el refresh token (para rotation y revocacion)
        ParsedJwt parsedRefresh = jwtTokenProvider.parse(refreshTokenStr);
        RefreshToken refreshEntity = RefreshToken.builder()
                .usuarioId(usuario.getId())
                .jti(parsedRefresh.jti())
                .expiraEn(LocalDateTime.ofInstant(parsedRefresh.expiresAt(), ZoneOffset.UTC))
                .revocado(false)
                .build();
        refreshTokenRepository.save(refreshEntity);

        long accessExpiresInSec = jwtProperties.getAccessTokenExpirationMinutes() * 60L;
        long refreshExpiresInSec = jwtProperties.getRefreshTokenExpirationDays() * 24L * 60 * 60;

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                usuario.getId(), usuario.getEmail(),
                usuario.getNombre(), usuario.getApellido(),
                roles,
                Boolean.TRUE.equals(usuario.getPasswordChangeRequired()));

        return new LoginResponse(accessToken, refreshTokenStr,
                accessExpiresInSec, refreshExpiresInSec, userInfo);
    }

    // -----------------------------------------------------------------------
    // REFRESH (con rotation)
    // -----------------------------------------------------------------------

    @Transactional
    public LoginResponse refresh(String refreshTokenStr) {
        ParsedJwt parsed;
        try {
            parsed = jwtTokenProvider.parse(refreshTokenStr);
        } catch (JwtException e) {
            throw new InvalidTokenException("Refresh token invalido o expirado");
        }

        if (parsed.tokenType() != TokenType.REFRESH) {
            throw new InvalidTokenException("Token no es de tipo REFRESH");
        }

        RefreshToken stored = refreshTokenRepository.findByJti(parsed.jti())
                .orElseThrow(() -> new InvalidTokenException("Refresh token desconocido"));

        if (!stored.isUsable()) {
            // Token revocado o expirado → posible robo. Revocar todos los del usuario por seguridad.
            log.warn("Intento de uso de refresh token revocado/expirado: jti={}, usuario={}. " +
                    "Revocando todos los tokens del usuario.",
                    parsed.jti(), parsed.userId());
            refreshTokenRepository.revocarTodosDelUsuario(parsed.userId(), LocalDateTime.now());
            throw new InvalidTokenException("Refresh token ya usado o revocado");
        }

        // Rotation: revocar el actual y emitir uno nuevo
        stored.revocar();
        refreshTokenRepository.save(stored);

        Usuario usuario = usuarioRepository.findById(parsed.userId())
                .orElseThrow(() -> new InvalidTokenException("Usuario asociado al token no existe"));

        return generarRespuestaLogin(usuario);
    }

    // -----------------------------------------------------------------------
    // LOGOUT
    // -----------------------------------------------------------------------

    @Transactional
    public void logout(String refreshTokenStr) {
        try {
            ParsedJwt parsed = jwtTokenProvider.parse(refreshTokenStr);
            refreshTokenRepository.findByJti(parsed.jti())
                    .ifPresent(rt -> {
                        rt.revocar();
                        refreshTokenRepository.save(rt);
                    });
        } catch (JwtException e) {
            // Logout es idempotente: si el token es invalido, igual respondemos 204
            log.debug("Logout con token invalido (ignorado): {}", e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // FORGOT PASSWORD
    // -----------------------------------------------------------------------

    @Transactional
    public void forgotPassword(String email) {
        // Anti-enumeration: respondemos siempre 202, exista o no el usuario
        Optional<Usuario> opt = usuarioRepository.findByEmail(email);
        if (opt.isEmpty()) {
            log.info("Forgot-password con email inexistente (no se publica evento): {}", email);
            return;
        }

        Usuario usuario = opt.get();
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            log.info("Forgot-password para cuenta inactiva (no se publica evento): {}", email);
            return;
        }

        UUID token = UUID.randomUUID();
        LocalDateTime expira = LocalDateTime.now()
                .plusMinutes(authProperties.getResetTokenExpirationMinutes());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .usuario(usuario)
                .token(token)
                .expiraEn(expira)
                .usado(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        PasswordResetSolicitadoEvent event = PasswordResetSolicitadoEvent.builder()
                .usuarioId(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .resetToken(token.toString())
                .expiraEnMinutos(authProperties.getResetTokenExpirationMinutes())
                .build();
        eventDispatcher.publishPasswordResetSolicitado(event);
    }

    // -----------------------------------------------------------------------
    // RESET PASSWORD
    // -----------------------------------------------------------------------

    @Transactional
    public void resetPassword(UUID token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de reset invalido"));

        if (!resetToken.isValid()) {
            throw new InvalidTokenException("Token de reset expirado o ya usado");
        }

        Usuario usuario = resetToken.getUsuario();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        // Reset de lockout en caso de que estuviera bloqueado
        usuario.setFailedAttempts((short) 0);
        usuario.setLocked(false);
        usuario.setLockUntil(null);
        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        passwordResetTokenRepository.save(resetToken);

        // Revocar todos los refresh tokens del usuario por seguridad
        refreshTokenRepository.revocarTodosDelUsuario(usuario.getId(), LocalDateTime.now());

        log.info("Password reseteado para usuario {}", usuario.getEmail());
    }

    // -----------------------------------------------------------------------
    // CAMBIAR PASSWORD (self-service: usuario autenticado)
    // -----------------------------------------------------------------------

    /**
     * Cambio de contrasenia self-service: requiere que el usuario indique su
     * contrasenia actual y la nueva. Util para el flujo de
     * {@code password_change_required = true} (cambio forzado al primer login).
     *
     * <p>Tras un cambio exitoso:</p>
     * <ul>
     *   <li>Se desactiva el flag {@code passwordChangeRequired}.</li>
     *   <li>Se resetea el lockout (intentos fallidos).</li>
     *   <li>Se revocan todos los refresh tokens activos (forzamos relogin).</li>
     * </ul>
     */
    @Transactional
    public void cambiarPasswordSelfService(String email, String currentPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new InvalidCredentialsException("La contrasenia actual no coincide");
        }

        if (passwordEncoder.matches(newPassword, usuario.getPassword())) {
            throw new InvalidCredentialsException("La nueva contrasenia debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setPasswordChangeRequired(Boolean.FALSE);
        usuario.setFailedAttempts((short) 0);
        usuario.setLocked(false);
        usuario.setLockUntil(null);
        usuarioRepository.save(usuario);

        refreshTokenRepository.revocarTodosDelUsuario(usuario.getId(), LocalDateTime.now());

        log.info("Password cambiada self-service para {}", email);
    }

    // -----------------------------------------------------------------------
    // GET USER (Sprint 4 / T4.2)
    // -----------------------------------------------------------------------

    /**
     * Devuelve el perfil del usuario para el endpoint {@code GET /auth/me}.
     *
     * <p><b>Por que readOnly = true:</b> los roles son {@code FetchType.LAZY}.
     * Sin la transaccion abierta, al iterar los roles fuera del servicio se
     * lanza {@code LazyInitializationException}. Aqui los materializamos a
     * {@code List<String>} antes de retornar, para que el controller no
     * dependa de una sesion Hibernate.</p>
     */
    @Transactional(readOnly = true)
    public LoginResponse.UserInfo getUserInfoByEmail(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado"));
        List<String> roles = u.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();
        return new LoginResponse.UserInfo(u.getId(), u.getEmail(),
                u.getNombre(), u.getApellido(), roles,
                Boolean.TRUE.equals(u.getPasswordChangeRequired()));
    }

    // -----------------------------------------------------------------------
    // Excepciones (clases internas, capturadas por el ExceptionHandler en T4.x)
    // -----------------------------------------------------------------------

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String msg) { super(msg); }
    }

    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException(String msg) { super(msg); }
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String msg) { super(msg); }
    }
}
