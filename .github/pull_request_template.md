<!--
  Plantilla de Pull Request - Sistema de Control Administrativo
  =============================================================
  Completa cada sección. Las que no apliquen, escribe "N/A".
-->

## Resumen

<!-- Qué se hizo en este PR, en 1-3 frases. -->



## Sprint y tarea

- **Sprint:** N
- **Tarea:** TN.X (referencia al SPRINTS_PLAN.xlsx)
- **Tipo de cambio:**
  - [ ] feature (nueva funcionalidad)
  - [ ] fix (corrección de bug)
  - [ ] docs (solo documentación)
  - [ ] chore (mantenimiento, deps, configuración, CI)
  - [ ] refactor (sin cambio funcional)
  - [ ] test (agregar/mejorar tests)

## Cambios principales

<!-- Lista bullet de los cambios más relevantes. -->

-
-
-

## Cómo probarlo

<!--
  Instrucciones para que el reviewer (o tu yo del futuro) verifique que funciona.
  Comandos exactos, URLs, pasos manuales si aplica.
-->

```bash
# ejemplos:
# cd backend && mvn -B clean install
# docker compose -f infrastructure/docker/docker-compose.yml up -d
# curl http://localhost:8080/actuator/health
```

## Screenshots / capturas (si aplica)

<!-- Para cambios visuales (frontend, dashboards, etc.) -->



## Checklist (Definition of Done)

<!-- Marca con [x] solo los que correspondan al alcance de este PR. -->

- [ ] Código sigue las convenciones de `DECISIONES.md` (sección 7)
- [ ] Tests unitarios escritos y pasando
- [ ] Tests de integración (si aplica) pasando
- [ ] Cobertura JaCoCo aceptable (threshold 80% se exige desde Sprint 4)
- [ ] OpenAPI / Swagger actualizado (si hay endpoints nuevos)
- [ ] Migraciones Flyway probadas (si tocó BD)
- [ ] Sin warnings nuevos del compilador
- [ ] Sin `TODO` o `FIXME` sin issue asociado
- [ ] CI verde (Backend CI + Docker Build si aplica)
- [ ] Probado manualmente en local
- [ ] Documentación actualizada (`README.md`, `backend/README.md`, etc. si aplica)
- [ ] No hay vulnerabilidades nuevas (OWASP Top 10)
- [ ] Memoria del proyecto actualizada (cuando se cierra una tarea/sprint)

## Notas adicionales para el reviewer

<!--
  Decisiones de diseño, alternativas consideradas, deuda técnica conocida,
  cualquier cosa que ayude a entender el "por qué" del PR.
-->


