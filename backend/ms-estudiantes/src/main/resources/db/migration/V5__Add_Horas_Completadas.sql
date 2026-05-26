-- =============================================================================
-- V5__Add_Horas_Completadas.sql  -  MS-Estudiantes
-- Agrega contador de horas (minutos) acumuladas de clases COMPLETADAS.
-- Lo incrementa ms-asignaciones cada vez que se finaliza una clase.
-- Cuando alcanza el total requerido por el tipo de curso, transición automática
-- MATRICULADO/CURSANDO -> COMPLETADO.
-- =============================================================================

ALTER TABLE estudiantes_schema.estudiantes
    ADD COLUMN minutos_completados INTEGER NOT NULL DEFAULT 0;

ALTER TABLE estudiantes_schema.estudiantes
    ADD CONSTRAINT ck_estudiantes_minutos_no_negativos
        CHECK (minutos_completados >= 0);

COMMENT ON COLUMN estudiantes_schema.estudiantes.minutos_completados IS
    'Total de minutos de clases COMPLETADAS. Lo incrementa ms-asignaciones via
     PUT /estudiantes/{id}/horas-completadas/incrementar cada vez que un instructor
     finaliza una clase. Se compara contra el total del curso para auto-transicionar
     a estado COMPLETADO.';
