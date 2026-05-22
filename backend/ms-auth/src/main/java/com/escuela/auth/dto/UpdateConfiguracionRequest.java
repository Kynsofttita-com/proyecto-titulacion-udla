package com.escuela.auth.dto;

import com.escuela.common.validation.annotation.RucEcuador;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record UpdateConfiguracionRequest(
        @NotBlank @Size(max = 255) String nombre,
        @NotBlank @RucEcuador String ruc,
        String direccion,
        @Size(max = 20) String telefono,
        @Email String email,
        @Size(max = 500) String logoUrl,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Formato HEX requerido (#RRGGBB)")
        String colorPrimario,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Formato HEX requerido (#RRGGBB)")
        String colorSecundario,
        @Min(15) Short duracionClaseDefaultMin,
        LocalTime horarioApertura,
        LocalTime horarioCierre,
        @Min(1) Short horasRecordatorioClase,
        @Min(1) Short diasAlertaSoat
) {}
