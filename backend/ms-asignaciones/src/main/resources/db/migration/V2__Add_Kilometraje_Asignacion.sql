-- =============================================================================
-- V2__Add_Kilometraje_Asignacion.sql  -  MS-Asignaciones
-- Sprint 12: agrega registro de kilometraje al iniciar y finalizar cada clase.
-- Cuando un instructor finaliza, el km_final se sincroniza con ms-vehiculos
-- (PATCH /vehiculos/{id}/kilometraje) para mantener el odometro maestro al dia.
-- =============================================================================

ALTER TABLE asignaciones_schema.asignaciones
    ADD COLUMN km_inicial          INTEGER,
    ADD COLUMN km_final            INTEGER,
    ADD COLUMN hora_inicio_real    TIMESTAMP,
    ADD COLUMN hora_fin_real       TIMESTAMP,
    ADD COLUMN observaciones_recorrido TEXT;

ALTER TABLE asignaciones_schema.asignaciones
    ADD CONSTRAINT ck_asignaciones_km_no_negativos
        CHECK ((km_inicial IS NULL OR km_inicial >= 0)
           AND (km_final   IS NULL OR km_final   >= 0));

ALTER TABLE asignaciones_schema.asignaciones
    ADD CONSTRAINT ck_asignaciones_km_final_mayor
        CHECK (km_final IS NULL OR km_inicial IS NULL OR km_final >= km_inicial);

COMMENT ON COLUMN asignaciones_schema.asignaciones.km_inicial IS
    'Kilometraje del vehiculo al iniciar la clase. Lo registra el instructor.';
COMMENT ON COLUMN asignaciones_schema.asignaciones.km_final IS
    'Kilometraje del vehiculo al finalizar. Triggers sync con ms-vehiculos.';
COMMENT ON COLUMN asignaciones_schema.asignaciones.hora_inicio_real IS
    'Hora real en que se inicio la clase (puede diferir de fecha_hora programada).';
COMMENT ON COLUMN asignaciones_schema.asignaciones.hora_fin_real IS
    'Hora real en que se finalizo la clase.';
COMMENT ON COLUMN asignaciones_schema.asignaciones.observaciones_recorrido IS
    'Notas del instructor sobre el recorrido (incidentes, desempeno del estudiante).';
