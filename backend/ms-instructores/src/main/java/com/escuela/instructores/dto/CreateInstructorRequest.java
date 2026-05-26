package com.escuela.instructores.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInstructorRequest(
        @NotBlank(message = "Cédula requerida")
        @Size(min = 10, max = 10, message = "Cédula debe tener 10 dígitos")
        String cedula,

        @NotBlank(message = "Nombre requerido")
        @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
        String nombre,

        @NotBlank(message = "Apellido requerido")
        @Size(min = 2, max = 100, message = "Apellido debe tener entre 2 y 100 caracteres")
        String apellido,

        @NotBlank(message = "Email requerido")
        @Email(message = "Email debe ser válido")
        String email,

        @NotBlank(message = "Teléfono requerido")
        @Size(min = 7, max = 10, message = "Teléfono debe tener entre 7 y 10 dígitos")
        String telefono,

        String direccion,

        LocalDate fechaNacimiento,

        @NotBlank(message = "Número de licencia requerido")
        @Size(min = 5, max = 50, message = "Número de licencia debe tener entre 5 y 50 caracteres")
        String licenciaNumero,

        @NotBlank(message = "Categoría de licencia requerida")
        @Size(min = 1, max = 20, message = "Categoría debe tener entre 1 y 20 caracteres")
        String licenciaCategoria,

        @NotNull(message = "Fecha de emisión de licencia requerida")
        LocalDate licenciaEmision,

        @NotNull(message = "Fecha de caducidad de licencia requerida")
        LocalDate licenciaCaducidad,

        LocalDate fechaContratacion,

        @DecimalMin(value = "0.0", inclusive = false, message = "Salario debe ser mayor a 0")
        BigDecimal salarioMensual,

        @NotNull(message = "Tipo de contrato requerido")
        @Pattern(regexp = "^(TIEMPO_COMPLETO|MEDIO_TIEMPO|POR_HORAS)$",
                message = "Tipo de contrato debe ser TIEMPO_COMPLETO, MEDIO_TIEMPO o POR_HORAS")
        String tipoContrato,

        @NotNull(message = "Horas semanales de contrato requeridas")
        @Min(value = 1, message = "Horas semanales debe ser al menos 1")
        @Max(value = 60, message = "Horas semanales no puede superar 60")
        Short horasContratoSemanales,

        @DecimalMin(value = "0.0", inclusive = false, message = "Tarifa por hora debe ser mayor a 0")
        BigDecimal tarifaHora,

        String observaciones
) {
    @AssertTrue(message = "En Ecuador el número de licencia debe coincidir con la cédula")
    public boolean isLicenciaIgualCedula() {
        if (cedula == null || licenciaNumero == null) return true; // ya cubierto por @NotBlank
        return cedula.equals(licenciaNumero);
    }

    @AssertTrue(message = "Tarifa por hora es obligatoria para contratos POR_HORAS")
    public boolean isTarifaCoherente() {
        if (tipoContrato == null) return true;
        if ("POR_HORAS".equals(tipoContrato)) {
            return tarifaHora != null && tarifaHora.signum() > 0;
        }
        return true;
    }

    @AssertTrue(message = "Salario mensual es obligatorio para contratos TIEMPO_COMPLETO o MEDIO_TIEMPO")
    public boolean isSalarioCoherente() {
        if (tipoContrato == null) return true;
        if ("TIEMPO_COMPLETO".equals(tipoContrato) || "MEDIO_TIEMPO".equals(tipoContrato)) {
            return salarioMensual != null && salarioMensual.signum() > 0;
        }
        return true;
    }
}
