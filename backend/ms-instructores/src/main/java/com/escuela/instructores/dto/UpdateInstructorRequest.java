package com.escuela.instructores.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateInstructorRequest(
        @Size(min = 2, max = 100) String nombre,
        @Size(min = 2, max = 100) String apellido,
        @Email String email,
        @Pattern(regexp = "^0\\d{9}$|^$", message = "Telefono ecuatoriano invalido") String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        @Size(min = 5, max = 50) String licenciaNumero,
        @Size(min = 1, max = 20) String licenciaCategoria,
        LocalDate licenciaEmision,
        LocalDate licenciaCaducidad,
        @Pattern(regexp = "^(ACTIVO|INACTIVO|SUSPENDIDO)$") String estado,
        LocalDate fechaContratacion,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal salarioMensual,

        @Pattern(regexp = "^(TIEMPO_COMPLETO|MEDIO_TIEMPO|POR_HORAS)$",
                message = "Tipo de contrato debe ser TIEMPO_COMPLETO, MEDIO_TIEMPO o POR_HORAS")
        String tipoContrato,

        @Min(value = 1, message = "Horas semanales debe ser al menos 1")
        @Max(value = 60, message = "Horas semanales no puede superar 60")
        Short horasContratoSemanales,

        @DecimalMin(value = "0.0", inclusive = false, message = "Tarifa por hora debe ser mayor a 0")
        BigDecimal tarifaHora,

        String observaciones
) {}
