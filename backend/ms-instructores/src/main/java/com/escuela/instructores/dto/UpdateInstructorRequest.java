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
        String observaciones
) {}
