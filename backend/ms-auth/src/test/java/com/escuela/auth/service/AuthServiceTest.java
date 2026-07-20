package com.escuela.auth.service;

import com.escuela.auth.config.AuthProperties;
import com.escuela.auth.dto.LoginRequest;
import com.escuela.auth.dto.LoginResponse;
import com.escuela.auth.entity.PasswordResetToken;
import com.escuela.auth.entity.RefreshToken;
import com.escuela.auth.entity.Rol;
import com.escuela.auth.entity.Usuario;
import com.escuela.auth.repository.ConfiguracionEscuelaRepository;
import com.escuela.auth.repository.PasswordResetTokenRepository;
import com.escuela.auth.repository.RefreshTokenRepository;
import com.escuela.auth.repository.UsuarioRepository;
import com.escuela.common.security.jwt.JwtProperties;
import com.escuela.common.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String SECRET_64 =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private static final Long USER_ID = 42L;
    private static final String EMAIL = "test@escuela.com";
    private static final String PASSWORD_PLAIN = "MiPassword123!";
    private static final String PASSWORD_HASH = "$2a$10$hashSimuladoBcrypt";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ConfiguracionEscuelaRepository configuracionRepository;
    @Mock private AuthEventDispatcher eventDispatcher;

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    private AuthProperties authProperties;
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET_64);
        jwtProperties.setAccessTokenExpirationMinutes(15);
        jwtProperties.setRefreshTokenExpirationDays(7);
        jwtProperties.setIssuer("escuela-conduccion");
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);

        authProperties = new AuthProperties();
        authProperties.setMaxFailedAttempts(3);
        authProperties.setLockoutDurationMinutes(15);
        authProperties.setResetTokenExpirationMinutes(60);

        authService = new AuthService(
                usuarioRepository, refreshTokenRepository, passwordResetTokenRepository,
                passwordEncoder, jwtTokenProvider, jwtProperties, authProperties,
                configuracionRepository, eventDispatcher);

        Rol rolAdmin = Rol.builder().id(1L).nombre("ADMIN").build();
        Set<Rol> roles = new HashSet<>();
        roles.add(rolAdmin);

        usuario = Usuario.builder()
                .id(USER_ID)
                .email(EMAIL)
                .password(PASSWORD_HASH)
                .nombre("Test")
                .apellido("User")
                .activo(true)
                .locked(false)
                .failedAttempts((short) 0)
                .roles(roles)
                .build();
    }

    // -----------------------------------------------------------------------
    // LOGIN
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Login exitoso devuelve tokens y resetea fallos")
    void loginExitoso() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(PASSWORD_PLAIN, PASSWORD_HASH)).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD_PLAIN));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().id()).isEqualTo(USER_ID);
        assertThat(response.user().roles()).containsExactly("ADMIN");
        assertThat(usuario.getFailedAttempts()).isZero();
        assertThat(usuario.getLastLogin()).isNotNull();

        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(eventDispatcher, never()).publishUsuarioBloqueado(any());
    }

    @Test
    @DisplayName("Login con email inexistente lanza InvalidCredentials")
    void loginEmailInexistente() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("nope@x.com", "x")))
                .isInstanceOf(AuthService.InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("Login con password incorrecta incrementa failedAttempts")
    void loginPasswordIncorrecta() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "mala")))
                .isInstanceOf(AuthService.InvalidCredentialsException.class);

        assertThat(usuario.getFailedAttempts()).isEqualTo((short) 1);
        assertThat(usuario.getLocked()).isFalse();
        verify(eventDispatcher, never()).publishUsuarioBloqueado(any());
    }

    @Test
    @DisplayName("Lockout se activa al 3er intento fallido y publica evento")
    void lockoutTras3Fallos() {
        usuario.setFailedAttempts((short) 2);  // Ya 2 fallos previos
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "mala")))
                .isInstanceOf(AuthService.InvalidCredentialsException.class);

        assertThat(usuario.getFailedAttempts()).isEqualTo((short) 3);
        assertThat(usuario.getLocked()).isTrue();
        assertThat(usuario.getLockUntil()).isAfter(LocalDateTime.now());
        verify(eventDispatcher, times(1)).publishUsuarioBloqueado(any());
    }

    @Test
    @DisplayName("Login a cuenta bloqueada lanza AccountLocked")
    void loginCuentaBloqueada() {
        usuario.setLocked(true);
        usuario.setLockUntil(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "x")))
                .isInstanceOf(AuthService.AccountLockedException.class);
    }

    @Test
    @DisplayName("Login a cuenta bloqueada cuyo lockUntil ya paso, se desbloquea automaticamente")
    void lockoutAutoExpiracion() {
        usuario.setLocked(true);
        usuario.setLockUntil(LocalDateTime.now().minusMinutes(1));  // Ya paso
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(PASSWORD_PLAIN, PASSWORD_HASH)).thenReturn(true);

        LoginResponse response = authService.login(new LoginRequest(EMAIL, PASSWORD_PLAIN));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(usuario.getLocked()).isFalse();
    }

    @Test
    @DisplayName("Login a cuenta inactiva lanza InvalidCredentials")
    void loginCuentaInactiva() {
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> authService.login(new LoginRequest(EMAIL, "x")))
                .isInstanceOf(AuthService.InvalidCredentialsException.class);
    }

    // -----------------------------------------------------------------------
    // REFRESH (rotation)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Refresh exitoso revoca el token actual y emite uno nuevo")
    void refreshConRotation() {
        // Generar refresh token real
        String oldRefresh = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, java.util.List.of("ADMIN"));
        UUID oldJti = jwtTokenProvider.parse(oldRefresh).jti();

        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .usuarioId(USER_ID)
                .jti(oldJti)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(false)
                .build();

        when(refreshTokenRepository.findByJti(oldJti)).thenReturn(Optional.of(stored));
        when(usuarioRepository.findById(USER_ID)).thenReturn(Optional.of(usuario));

        LoginResponse response = authService.refresh(oldRefresh);

        assertThat(response.refreshToken()).isNotEqualTo(oldRefresh);
        assertThat(stored.isRevocado()).isTrue();
        verify(refreshTokenRepository).save(stored);  // Guardar el viejo revocado
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));  // Viejo revocado + nuevo
    }

    @Test
    @DisplayName("Refresh con token revocado revoca todos los del usuario y rechaza")
    void refreshTokenRevocado() {
        String oldRefresh = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, java.util.List.of("ADMIN"));
        UUID oldJti = jwtTokenProvider.parse(oldRefresh).jti();

        RefreshToken stored = RefreshToken.builder()
                .usuarioId(USER_ID).jti(oldJti)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(true)  // ya revocado
                .build();

        when(refreshTokenRepository.findByJti(oldJti)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(oldRefresh))
                .isInstanceOf(AuthService.InvalidTokenException.class);

        verify(refreshTokenRepository).revocarTodosDelUsuario(eq(USER_ID), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Refresh con access token (no refresh) lanza InvalidToken")
    void refreshConAccessToken() {
        String access = jwtTokenProvider.generateAccessToken(USER_ID, EMAIL, java.util.List.of("ADMIN"));

        assertThatThrownBy(() -> authService.refresh(access))
                .isInstanceOf(AuthService.InvalidTokenException.class);
    }

    @Test
    @DisplayName("Refresh con token malformado lanza InvalidToken")
    void refreshTokenMalformado() {
        assertThatThrownBy(() -> authService.refresh("xxx.yyy.zzz"))
                .isInstanceOf(AuthService.InvalidTokenException.class);
    }

    // -----------------------------------------------------------------------
    // FORGOT PASSWORD
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ForgotPassword crea token y publica evento")
    void forgotPasswordPublicaEvento() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        authService.forgotPassword(EMAIL);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isNotNull();
        assertThat(captor.getValue().getExpiraEn()).isAfter(LocalDateTime.now());

        verify(eventDispatcher).publishPasswordResetSolicitado(any());
    }

    @Test
    @DisplayName("ForgotPassword con email inexistente NO publica evento (anti-enumeration)")
    void forgotPasswordEmailInexistente() {
        when(usuarioRepository.findByEmail("nope@x.com")).thenReturn(Optional.empty());

        authService.forgotPassword("nope@x.com");  // No lanza

        verify(passwordResetTokenRepository, never()).save(any());
        verify(eventDispatcher, never()).publishPasswordResetSolicitado(any());
    }

    // -----------------------------------------------------------------------
    // RESET PASSWORD
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ResetPassword exitoso actualiza hash y revoca refresh tokens")
    void resetPasswordExitoso() {
        UUID token = UUID.randomUUID();
        PasswordResetToken stored = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(60))
                .usado(false)
                .build();

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(stored));
        when(passwordEncoder.encode("NuevaPass123!")).thenReturn("$2a$10$nuevoHash");

        authService.resetPassword(token, "NuevaPass123!");

        assertThat(usuario.getPassword()).isEqualTo("$2a$10$nuevoHash");
        assertThat(stored.getUsado()).isTrue();
        verify(refreshTokenRepository).revocarTodosDelUsuario(eq(USER_ID), any());
    }

    @Test
    @DisplayName("ResetPassword con token expirado lanza InvalidToken")
    void resetPasswordExpirado() {
        UUID token = UUID.randomUUID();
        PasswordResetToken stored = PasswordResetToken.builder()
                .token(token).usuario(usuario)
                .expiraEn(LocalDateTime.now().minusMinutes(1))  // expirado
                .usado(false).build();
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.resetPassword(token, "x"))
                .isInstanceOf(AuthService.InvalidTokenException.class);
    }

    @Test
    @DisplayName("ResetPassword con token usado lanza InvalidToken")
    void resetPasswordYaUsado() {
        UUID token = UUID.randomUUID();
        PasswordResetToken stored = PasswordResetToken.builder()
                .token(token).usuario(usuario)
                .expiraEn(LocalDateTime.now().plusMinutes(60))
                .usado(true).build();
        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.resetPassword(token, "x"))
                .isInstanceOf(AuthService.InvalidTokenException.class);
    }

    // -----------------------------------------------------------------------
    // LOGOUT
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Logout revoca el refresh token")
    void logoutRevoca() {
        String refresh = jwtTokenProvider.generateRefreshToken(USER_ID, EMAIL, java.util.List.of("ADMIN"));
        UUID jti = jwtTokenProvider.parse(refresh).jti();

        RefreshToken stored = RefreshToken.builder()
                .usuarioId(USER_ID).jti(jti)
                .expiraEn(LocalDateTime.now().plusDays(7))
                .revocado(false).build();
        when(refreshTokenRepository.findByJti(jti)).thenReturn(Optional.of(stored));

        authService.logout(refresh);

        assertThat(stored.isRevocado()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    @DisplayName("Logout con token invalido es idempotente (no lanza)")
    void logoutTokenInvalido() {
        // No debe lanzar excepcion
        authService.logout("xxx.yyy.zzz");
        verify(refreshTokenRepository, never()).save(any());
    }

    // Helper para mockito.eq (se importa como eq)
    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
