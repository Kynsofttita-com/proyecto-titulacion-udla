-- =============================================================================
-- V7__movimientos_pagado_a.sql - MS-Cobros
-- Vincula un movimiento con la persona a la que se le paga (instructor o
-- administrativo). Usado para categorias SUELDO_INSTRUCTOR y
-- SUELDO_ADMINISTRATIVO desde el modulo Gastos.
--
-- Diseño: columnas genericas para no acoplar ms-cobros a los schemas
-- concretos (instructores_schema, auth_schema). El nombre se denormaliza
-- al momento del registro. Sin FK cross-MS.
-- =============================================================================

ALTER TABLE contabilidad_schema.movimientos_contables
    ADD COLUMN pagado_a_id      BIGINT,
    ADD COLUMN nombre_pagado_a  VARCHAR(200);

COMMENT ON COLUMN contabilidad_schema.movimientos_contables.pagado_a_id IS
    'ID de la persona a la que se le paga (instructor o usuario admin/staff). Semantica depende de la categoria del movimiento.';
COMMENT ON COLUMN contabilidad_schema.movimientos_contables.nombre_pagado_a IS
    'Nombre denormalizado al momento del pago (visible en tabla de Gastos y en el detalle de la persona).';

CREATE INDEX ix_mov_pagado_a ON contabilidad_schema.movimientos_contables (pagado_a_id)
    WHERE pagado_a_id IS NOT NULL;
