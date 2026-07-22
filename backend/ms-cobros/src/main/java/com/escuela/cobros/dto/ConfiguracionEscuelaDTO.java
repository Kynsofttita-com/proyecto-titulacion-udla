package com.escuela.cobros.dto;

/**
 * Proyeccion parcial de la configuracion de escuela de ms-auth, con solo los
 * campos que ms-cobros necesita hoy (cuentas contables por defecto).
 *
 * <p>Uso Jackson en modo lax (properties extras del JSON se ignoran) para no
 * romper si ms-auth agrega o quita campos.</p>
 */
public record ConfiguracionEscuelaDTO(
        Long cuentaDefaultCobrosId,
        Long cuentaDefaultCombustibleId,
        Long cuentaDefaultMantenimientoId
) {}
