package com.escuela.common.security.headers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserContextTest {

    @AfterEach
    void cleanup() {
        UserContextHolder.clear();
    }

    @Test
    @DisplayName("hasRole verifica rol exacto")
    void hasRole() {
        UserContext u = new UserContext(1L, "a@b.com", List.of("ADMIN", "STAFF"));
        assertThat(u.hasRole("ADMIN")).isTrue();
        assertThat(u.hasRole("STAFF")).isTrue();
        assertThat(u.hasRole("INSTRUCTOR")).isFalse();
        assertThat(u.hasRole("admin")).isFalse();  // case-sensitive
    }

    @Test
    @DisplayName("hasAnyRole encuentra al menos uno")
    void hasAnyRole() {
        UserContext u = new UserContext(1L, "a@b.com", List.of("ADMIN"));
        assertThat(u.hasAnyRole("ADMIN", "STAFF")).isTrue();
        assertThat(u.hasAnyRole("STAFF", "INSTRUCTOR")).isFalse();
    }

    @Test
    @DisplayName("hasRole con roles null devuelve false")
    void hasRoleConRolesNull() {
        UserContext u = new UserContext(1L, "a@b.com", null);
        assertThat(u.hasRole("ADMIN")).isFalse();
        assertThat(u.hasAnyRole("ADMIN")).isFalse();
    }

    @Test
    @DisplayName("UserContextHolder set/get/clear funciona")
    void holderBasico() {
        assertThat(UserContextHolder.getContext()).isNull();

        UserContext u = new UserContext(1L, "a@b.com", List.of("ADMIN"));
        UserContextHolder.setContext(u);

        assertThat(UserContextHolder.getContext()).isEqualTo(u);
        assertThat(UserContextHolder.requireContext()).isEqualTo(u);

        UserContextHolder.clear();
        assertThat(UserContextHolder.getContext()).isNull();
    }

    @Test
    @DisplayName("requireContext sin context lanza IllegalStateException")
    void requireContextSinUsuario() {
        UserContextHolder.clear();
        assertThatThrownBy(UserContextHolder::requireContext)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UserContext");
    }
}
