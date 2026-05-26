package com.escuela.vehiculos.entity;

import com.escuela.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehiculos", schema = "vehiculos_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Vehiculo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8, unique = true)
    private String placa;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false)
    private Short anio;

    @Column(length = 17, unique = true)
    private String vin;

    @Column(length = 30)
    private String color;

    @Column(nullable = false)
    private Integer kilometraje = 0;

    @Column(nullable = false, length = 20)
    private String estado = "ACTIVO";

    @Column(name = "soat_vencimiento")
    private LocalDate soatVencimiento;

    @Column(name = "revision_vencimiento")
    private LocalDate revisionVencimiento;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "valor_compra", precision = 10, scale = 2)
    private BigDecimal valorCompra;

    @Column(name = "categoria_licencia_id")
    private Long categoriaLicenciaId;

    @Column(name = "numero_motor", length = 50)
    private String numeroMotor;

    @Column(name = "numero_chasis", length = 50)
    private String numeroChasis;

    @Column(name = "capacidad_pasajeros")
    private Short capacidadPasajeros;

    @Column(name = "tipo_combustible_id")
    private Long tipoCombustibleId;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
