package com.escuela.instructores.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstructorResponse(
        Long id,
        String cedula,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion,
        LocalDate fechaNacimiento,
        String licenciaNumero,
        String licenciaCategoria,
        LocalDate licenciaEmision,
        LocalDate licenciaCaducidad,
        String estado,
        LocalDate fechaContratacion,
        BigDecimal salarioMensual,
        String tipoContrato,
        Short horasContratoSemanales,
        BigDecimal tarifaHora,
        Long usuarioId,
        String observaciones
) {}
