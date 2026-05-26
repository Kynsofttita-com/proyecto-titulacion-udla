# Guía de contribución

Esta guía describe el flujo de trabajo del proyecto. Sigue estas reglas para mantener historial claro, código verificado por CI y trazabilidad por sprint.

> **TL;DR:** **1 PR por cada commit/tarea**. Crear branch `feature/sprint-N-X-descripcion-corta` desde `main`, hacer **un solo commit** con formato `Sprint N (Tarea X descripcion)`, abrir PR a `main`, esperar CI verde, **squash and merge**.

> **Política vigente desde Sprint 3:** un commit/tarea = un PR. La política anterior ("un sprint = un PR") fue descartada porque acumulaba demasiados commits con tests fallando al final del sprint. Ahora cada tarea se valida individualmente.

---

## 1. Estrategia de branching: GitHub Flow

```
main ────●─────────●─────────●─────────●────►
         │         │         │         │
         ▼         ▼         ▼         ▼
      feature/  feature/  fix/      docs/
      sprint-   sprint-   bug-      sprint-
      10-1      10-2      cedula    10-readme
         │         │         │         │
        ●          ●         ●        ●
       commit    commit   commit   commit
       + PR      + PR     + PR     + PR
```

- **`main`**: única branch larga, siempre estable y desplegable. Protegida.
- **Branches efímeras**: una por tarea/fix/docs. Vida muy corta (horas a 1-2 días).
- **Sin `develop`**: no hace falta, GitHub Flow simplifica.

### Tipos de branches

| Prefijo | Para | Ejemplo |
|---------|------|---------|
| `feature/` | Nueva funcionalidad (1 tarea del sprint) | `feature/sprint-10-1-notif-plantillas` |
| `fix/` | Corrección de bug | `fix/validacion-cedula-edge-case` |
| `docs/` | Solo documentación | `docs/sprint-10-readme-update` |
| `chore/` | Mantenimiento (deps, configs, CI) | `chore/upgrade-spring-boot-3-4-1` |
| `refactor/` | Refactor sin cambio funcional | `refactor/extraer-validador-comun` |

### Convención de naming

- Usar **kebab-case**: `sprint-10-1-notif-plantillas`
- Sin acentos ni caracteres especiales ni underscores
- Corto pero descriptivo (3-5 palabras)
- Incluir número de sprint + tarea: `feature/sprint-N-X-descripcion`

---

## 2. Flujo paso a paso (1 tarea = 1 PR)

### 2.1 Antes de empezar

```bash
git checkout main
git pull origin main
```

### 2.2 Crear branch para la tarea

```bash
git checkout -b feature/sprint-10-1-notif-plantillas
```

### 2.3 Trabajar y hacer UN SOLO commit por tarea

```bash
# ... haces los cambios de la tarea completa ...
git add <archivos>
git commit -m "Sprint 10 (Tarea 1 - MS-Notificaciones plantillas CRUD)"
```

**Formato de commit:** `Sprint N (Tarea X descripcion concreta)`

Si hay que corregir algo del commit antes de pushear:
```bash
git commit --amend  # solo si el commit no se ha pusheado todavía
```

Ver [DECISIONES.md §9.2](../DECISIONES.md) para ejemplos completos.

### 2.4 Push del branch

```bash
git push -u origin feature/sprint-10-1-notif-plantillas
```

### 2.5 Abrir Pull Request

**Opción A:** URL que GitHub muestra al hacer push (la más rápida).

**Opción B:** GitHub CLI si lo tenés instalado:
```bash
gh pr create --base main --title "Sprint 10 (Tarea 1 - MS-Notificaciones plantillas CRUD)"
```

**Opción C:** Manual desde la web → "Pull requests" → "New pull request" → base `main`, compare tu branch.

### 2.6 Llenar el template

GitHub muestra automáticamente `.github/pull_request_template.md`. Completá las secciones aplicables (al menos Resumen, Sprint/Tarea, Cómo probarlo, checklist DoD).

### 2.7 Esperar CI

Workflows que se disparan según `paths:` del cambio:

| Workflow | Cuándo corre | Acción |
|----------|-------------|--------|
| `backend-ci.yml` | Siempre en PR a `main` | Maven build + unit tests + JaCoCo |
| `docker-build.yml` | Si cambia `backend/**` o `infrastructure/docker/**` | Build imagen de prueba (eureka-server) |
| `frontend-ci.yml` | Si cambia `frontend/**` | `npm ci` + `vite build` |
| `integration-tests.yml` | Si cambia `backend/**` | `mvn verify -Dgroups=integration` con Postgres + RabbitMQ |
| `smoke-e2e.yml` | Si cambia `backend/**` o `infrastructure/**` | Stack completo Docker + 12 endpoints REST + 404/400 ProblemDetail |

**Backend CI es obligatorio para mergear.** Los otros workflows pueden no correr según los `paths:`.

Si algo falla:
- Click en el check rojo → ver logs
- Arreglar local, hacer commit nuevo (o `--amend` + force-push si el commit anterior aún no se mergeó), push al mismo branch
- CI corre de nuevo automáticamente

### 2.8 Merge

Cuando todo está verde:

1. Botón **"Squash and merge"** en el PR (NO usar "Create a merge commit")
2. Verificá que el título del commit final tenga formato `Sprint N (Tarea X descripcion)`
3. Click "Confirm squash and merge"
4. Click "Delete branch" (limpia el remote)
5. Local:
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/sprint-10-1-notif-plantillas
   ```

---

## 3. Convenciones de commits

Ver [DECISIONES.md §9.2](../DECISIONES.md) para el detalle completo.

**Formato:** `Sprint N (Tarea descripcion concreta)` para features. `Sprint N (Fix tarea descripcion)` para fixes.

**Buenos ejemplos:**
- `Sprint 5 (Tarea 1 - Cierre módulo Configuración MS-Auth)`
- `Sprint 9 (Fix admin password seed - hash bcrypt invalido en V1_5)`
- `Sprint 10 (Tarea 3 - MS-Reportes reportes operativos via Feign)`

**Malos ejemplos:**
- `wip` (sin contexto)
- `arreglos varios` (no descriptivo)
- `feat: add jwt` (no sigue formato del proyecto)

---

## 4. Checklist antes de abrir PR

- [ ] Compila local (`mvn clean install` desde `backend/`)
- [ ] Tests pasan local (`mvn test`)
- [ ] Sin warnings nuevos del compilador
- [ ] Probado manualmente en local
- [ ] Si tocó BD: migración Flyway probada (V<N+1> sin gaps en numeración)
- [ ] Si tocó frontend: `vite build` sin errores
- [ ] Si tocó Docker: imagen build sin errores
- [ ] Documentación actualizada si aplica (README, schema.md, etc.)
- [ ] Commit con formato `Sprint N (Tarea X descripcion)`

---

## 5. Branch protection en `main`

`main` está protegida. **No se puede hacer push directo**. Reglas activas:

- ✅ Require pull request before merging
- ✅ Require Backend CI to pass
- ✅ Require branches to be up to date with main
- ✅ Require conversation resolution
- ❌ Force pushes prohibidos
- ❌ Deletion prohibida
- ✅ Aplica a administradores (no bypass)

Si intentás `git push origin main` directo, GitHub rechaza. Es intencional.

**Excepción:** la fase de setup inicial (Sprints 0-2.0) tuvo commits directos a main porque la estructura no estaba lista. Desde Sprint 2.1, todo via PR.

---

## 6. Trabajando en equipo (Hernán y Sebas)

### Cuando ambos trabajan al mismo tiempo:

- Cada uno crea su propio branch desde `main`
- Si hay merge conflict en el PR, se resuelve **localmente**:

```bash
git checkout feature/mi-branch
git fetch origin
git rebase origin/main
# resolver conflictos en los archivos marcados
git add <archivos>
git rebase --continue
git push --force-with-lease  # SOLO en branches propios, NUNCA en main
```

### División informal del trabajo

- **Hernán**: branches `feature/sprint-N-X-...` (código)
- **Sebas**: branches `docs/sprint-N-...` (documentación)
- Cada uno reviewea el PR del otro antes de merge cuando sea posible

---

## 7. Cuándo NO seguir este flujo

**Únicas excepciones permitidas para commits directos a `main`:**
- Fix urgente del workflow de CI/CD que rompió todo (raro)
- Setup inicial del repo (ya pasó, Sprints 0-2.0)

Cualquier otro caso, **abrir PR**.

---

## 8. Referencias

- [DECISIONES.md](../DECISIONES.md) — Decisiones técnicas (§9 sobre Git, §25.5 sobre workflows)
- [PLAN_FASES.md](../PLAN_FASES.md) — Plan vigente (13 sprints)
- [SPRINTS_PLAN.xlsx](../SPRINTS_PLAN.xlsx) — Plan tabular detallado
- [GitHub Flow oficial](https://docs.github.com/en/get-started/using-github/github-flow)

---

¿Dudas? Preguntar antes de hacer un push directo a `main` — es la regla.
