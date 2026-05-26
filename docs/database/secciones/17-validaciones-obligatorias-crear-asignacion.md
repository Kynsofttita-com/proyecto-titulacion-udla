# 17. Validaciones obligatorias al crear asignación

[← Volver al índice](../schema.md)

> Reglas de negocio cross-microservicio que se ejecutan al ejecutar `POST /asignaciones`. Introducidas en Sprint 10. Cualquier falla devuelve `409 Conflict` con `ProblemDetail` (RFC 7807).

---

## Las 6 validaciones obligatorias

| # | Validación | MS consultado | Mecanismo |
|---|------------|---------------|-----------|
| 1 | La categoría de licencia del **instructor** habilita la categoría que el estudiante está cursando | MS-Instructores + MS-Estudiantes | Feign |
| 2 | La categoría del **vehículo** coincide con la categoría que el estudiante está cursando | MS-Vehículos + MS-Estudiantes | Feign |
| 3 | El vehículo tiene **SOAT vigente** a la fecha de la asignación | MS-Vehículos | Feign |
| 4 | El vehículo tiene **RTV (revisión técnica vehicular) vigente** a la fecha | MS-Vehículos | Feign |
| 5 | El **horario semanal del instructor** cubre el rango horario solicitado | MS-Instructores | Feign (`disponibilidad` + `horarios_trabajo`) |
| 6 | El instructor no está en **AUSENCIA** (vacaciones/licencia) en esa fecha | MS-Instructores | Feign (`horarios_trabajo` con `tipo='AUSENCIA'`) |

---

## Validaciones adicionales

Además de las 6 cross-MS, el sistema valida:

- El **estudiante** está en estado `MATRICULADO` o `CURSANDO` (no se puede asignar clase a `PRE_MATRICULADO` o `RETIRADO`).
- La `situacion_pago` del estudiante es `PAGADO_TOTAL`.
- No hay **conflicto de horario** entre el mismo instructor o el mismo vehículo (búsqueda en `asignaciones` con `estado IN ('PROGRAMADA', 'CONFIRMADA', 'EN_CURSO')`).
- La fecha de la asignación es **futura** (no se pueden crear asignaciones retroactivas).
- La duración solicitada es coherente con el `duracion_clase_default_min` configurado en la escuela (o se indica explícitamente).

---

## Formato de error (RFC 7807)

Cuando una validación falla, la respuesta sigue el estándar Problem Details:

```json
{
  "type": "https://api.escuela.com/errors/instructor-sin-categoria",
  "title": "Instructor no habilitado para esta categoría",
  "status": 409,
  "detail": "El instructor 42 tiene licencia categoría B, pero el estudiante 87 está cursando categoría C",
  "instance": "/asignaciones",
  "timestamp": "2026-05-26T10:30:00",
  "errors": []
}
```

Cada validación tiene su propio `type` URI:

| Validación | Type URI |
|------------|----------|
| #1 Instructor sin categoría | `/errors/instructor-sin-categoria` |
| #2 Vehículo sin categoría | `/errors/vehiculo-sin-categoria` |
| #3 SOAT vencido | `/errors/vehiculo-soat-vencido` |
| #4 RTV vencida | `/errors/vehiculo-rtv-vencida` |
| #5 Fuera de horario instructor | `/errors/instructor-fuera-horario` |
| #6 Instructor en ausencia | `/errors/instructor-ausencia` |

---

## Referencia ADR

Esta sección consolida lo decidido en `DECISIONES.md §24.4` (ADR Sprint 10: Refactor de dominio y endurecimiento operativo del Grupo A).
