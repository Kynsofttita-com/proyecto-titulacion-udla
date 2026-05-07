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
@Table(name = "mantenimientos", schema = "vehiculos_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Mantenimiento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    private String taller;

    @Column(name = "kilometraje_servicio")
    private Integer kilometrajeServicio;

    @Column(name = "proxima_fecha")
    private LocalDate proximaFecha;

    @Column(name = "proximo_kilometraje")
    private Integer proximoKilometraje;

    @Column(name = "archivo_factura_url", length = 500)
    private String archivoFacturaUrl;
}
