package com.escuela.instructores.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Instructor de la escuela. Vinculado opcionalmente a un {@code Usuario}
 * (referencia por ID en {@code auth_schema}, sin FK cross-schema).
 */
@Entity
@Table(name = "instructores", schema = "instructores_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Instructor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String cedula;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 10)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "licencia_numero", nullable = false, length = 50, unique = true)
    private String licenciaNumero;

    @Column(name = "licencia_categoria", nullable = false, length = 20)
    private String licenciaCategoria;

    @Column(name = "licencia_emision", nullable = false)
    private LocalDate licenciaEmision;

    @Column(name = "licencia_caducidad", nullable = false)
    private LocalDate licenciaCaducidad;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "salario_mensual", precision = 10, scale = 2)
    private BigDecimal salarioMensual;

    @Column(name = "tipo_contrato", nullable = false, length = 20)
    private String tipoContrato = "TIEMPO_COMPLETO";

    @Column(name = "horas_contrato_semanales", nullable = false)
    private Short horasContratoSemanales = 40;

    @Column(name = "tarifa_hora", precision = 8, scale = 2)
    private BigDecimal tarifaHora;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
