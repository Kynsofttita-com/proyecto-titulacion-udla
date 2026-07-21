package com.escuela.cobros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
        String nombre,

        @NotBlank(message = "El tipo es requerido")
        @Pattern(regexp = "^(EFECTIVO|BANCO|TARJETA)$",
                message = "Tipo debe ser EFECTIVO, BANCO o TARJETA")
        String tipo,

        /** Requerido si tipo=BANCO o TARJETA (validado en service). */
        @Size(max = 60, message = "El numero de cuenta no puede exceder 60 caracteres")
        String numeroCuenta,

        @NotNull(message = "El saldo inicial es requerido")
        BigDecimal saldoInicial,

        Boolean activo,

        @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
        String observaciones
) {}
