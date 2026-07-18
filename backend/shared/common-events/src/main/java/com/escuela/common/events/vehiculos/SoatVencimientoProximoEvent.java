package com.escuela.common.events.vehiculos;

import com.escuela.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * Publicado diariamente por MS-Vehiculos (scheduler) por cada vehiculo cuyo SOAT esta
 * proximo a vencer (<=30 dias) o ya vencido.
 *
 * <p>Routing key: {@code vehiculos.soat.vencimiento.proximo}
 * Exchange: {@code vehiculos.exchange}</p>
 *
 * <p>Idempotencia: el {@code eventId} es deterministico a partir de
 * (vehiculoId, fecha del dia) para evitar duplicar la notificacion si el
 * scheduler se ejecuta mas de una vez el mismo dia.</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SoatVencimientoProximoEvent extends BaseEvent {

    public static final String ROUTING_KEY = "vehiculos.soat.vencimiento.proximo";

    private Long vehiculoId;
    private String placa;
    private String marca;
    private String modelo;
    private LocalDate soatVencimiento;
    private int diasParaVencer;
    private boolean vencido;
}
