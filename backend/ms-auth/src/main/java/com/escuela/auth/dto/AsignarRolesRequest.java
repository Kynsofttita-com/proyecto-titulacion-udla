package com.escuela.auth.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AsignarRolesRequest(
        @NotEmpty(message = "Debe especificarse al menos un rol")
        List<String> roles
) {}
