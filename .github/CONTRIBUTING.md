# Guía de contribución

Esta guía describe el flujo de trabajo para colaborar en este proyecto. Sigue estas reglas para mantener historia clara, código verificado por CI y trazabilidad por sprint.

> **TL;DR:** **un branch por sprint completo**. Crea `feature/sprint-N-descripcion` desde `main`, hacé commits granulares por tarea con formato `Sprint N (Tarea)`, al cerrar el sprint abrí Pull Request, esperá CI verde y **squash and merge**.

> **Política actualizada en Sprint 2.2:** un sprint = un PR (no un PR por tarea). Esto da mejor trazabilidad para titulación y reduce overhead. Los commits granulares siguen siendo por tarea.

---

## 1. Estrategia de branching: GitHub Flow

```
main  ─────●───────────●───────────●───────────●─────────►
           │           │           │           │
           ▼           ▼           ▼           ▼
        feature/    feature/    fix/        docs/
        sprint-2-1  sprint-2-2  bug-cedula  sprint-3-er
            │           │           │           │
        ●───●●          ●───●        ●           ●─●
        commits     commits     commit       commits
        + PR        + PR        + PR         + PR
```

- **`main`**: única branch larga. Siempre estable y desplegable.
- **Branches efímeras**: una por feature/fix/docs. Vida corta (1-3 días).
- **Sin `develop`**: no hace falta, simplifica el flujo.

### Tipos de branches

| Prefijo | Para | Ejemplo |
|---------|------|---------|
| `feature/` | Nueva funcionalidad | `feature/sprint-2-1-disenar-schema-bd` |
| `fix/` | Corrección de bug | `fix/validacion-cedula-edge-case` |
| `docs/` | Solo documentación | `docs/sprint-3-actualizar-readme` |
| `chore/` | Mantenimiento (deps, configs, CI) | `chore/upgrade-spring-boot-3-4-1` |
| `refactor/` | Refactor sin cambio funcional | `refactor/extraer-validador-comun` |

### Convención de naming

- Usar **kebab-case** (palabras separadas con guión): `sprint-2-1-disenar-schema`
- Sin acentos, sin caracteres especiales, sin underscores
- Corto pero descriptivo (3-5 palabras)
- Incluir el número de sprint cuando aplica: `feature/sprint-N-...`

---

## 2. Flujo paso a paso

### 2.1 Antes de empezar

```bash
# Asegúrate de estar en main actualizado
git checkout main
git pull origin main
```

### 2.2 Crear branch para tu trabajo

```bash
git checkout -b feature/sprint-2-1-disenar-schema-bd
```

### 2.3 Trabajar y hacer commits

```bash
# ... haces cambios ...
git add <archivos>
git commit -m "Sprint 2 (Diseñar schema MS-Auth)"

# ... más cambios ...
git commit -m "Sprint 2 (Diseñar schema MS-Estudiantes)"
```

**Formato de commit:** `Sprint N (Tarea descripción concreta)`

Ver [DECISIONES.md sección 9.2](./DECISIONES.md) para ejemplos completos.

### 2.4 Push del branch

```bash
git push -u origin feature/sprint-2-1-disenar-schema-bd
```

### 2.5 Abrir Pull Request

Hay dos formas:

**Opción A: Desde la URL que GitHub te muestra al hacer push**
```
remote: Create a pull request for 'feature/sprint-2-1-disenar-schema-bd' on GitHub by visiting:
remote:      https://github.com/Kynsofttita-com/proyecto-titulacion-udla/pull/new/feature/sprint-2-1-disenar-schema-bd
```
Click y se abre la página del PR.

**Opción B: Desde GitHub CLI (`gh`)** (si lo tienes instalado)
```bash
gh pr create --base main --title "Sprint 2.1 — Diseñar schema BD" --body-file <archivo>
```

**Opción C: Manual desde GitHub**
- Ir a https://github.com/Kynsofttita-com/proyecto-titulacion-udla
- Click en "Pull requests" → "New pull request"
- Base: `main`, compare: `feature/sprint-2-1-disenar-schema-bd`

### 2.6 Llenar el template

GitHub muestra automáticamente el template en `.github/pull_request_template.md`. Llena las secciones que apliquen.

### 2.7 Esperar CI

Tras crear el PR, GitHub Actions ejecuta:
- ✅ **Backend CI** (siempre): build + tests + JaCoCo
- ✅ **Docker Build** (si tocó `backend/` o `infrastructure/docker/`)

Espera a que ambos checks pasen (verde). Ver progreso en la sección "Checks" del PR.

Si algo falla:
- Click en el check rojo → ver logs
- Arreglar localmente, hacer commit nuevo, push al mismo branch
- CI se ejecuta automáticamente otra vez

### 2.8 Merge

**Cuando todo está verde:**

1. Botón "Squash and merge" en el PR (no usar "Create a merge commit")
2. Editar el título del commit final si es necesario (mantener formato `Sprint N (Tarea)`)
3. Click "Confirm squash and merge"
4. Click "Delete branch" (limpia el remote)
5. Localmente:

```bash
git checkout main
git pull origin main
git branch -d feature/sprint-2-1-disenar-schema-bd  # borra branch local
```

---

## 3. Convenciones de commits

Ver [DECISIONES.md sección 9.2](./DECISIONES.md) para el detalle completo.

**Formato corto:** `Sprint N (Tarea)`

**Ejemplos buenos:**
- `Sprint 2 (Diseñar schema MS-Auth)`
- `Sprint 4 (Implementar JwtTokenProvider)`
- `Sprint 5 (Fix validación cédula Ecuador edge case)`
- `Sprint 6 (Refactor mapper Estudiante - extraer DTOs)`

**Ejemplos malos:**
- `wip` ❌ (sin contexto)
- `arreglos varios` ❌ (no descriptivo)
- `feat: add jwt` ❌ (no sigue el formato del proyecto)

---

## 4. Checklist antes de abrir PR

- [ ] Compila localmente (`mvn clean install` desde `backend/`)
- [ ] Tests pasan localmente (`mvn test` desde `backend/`)
- [ ] Sin warnings nuevos del compilador
- [ ] Probado manualmente la feature
- [ ] Si tocó BD: migración Flyway probada
- [ ] Si tocó Docker: imagen build sin errores
- [ ] Documentación actualizada si aplica
- [ ] Commits con formato `Sprint N (Tarea)`

---

## 5. Branch protection en `main`

`main` está protegida. **No se puede hacer push directo**. Las reglas activas son:

- ✅ Require pull request before merging
- ✅ Require Backend CI to pass
- ✅ Require branches to be up to date with main
- ✅ Require conversation resolution
- ❌ Force pushes prohibidos
- ❌ Deletion prohibida

Si intentas `git push origin main` directamente, GitHub rechaza el push. Esto es intencional.

**Excepción:** la fase de setup inicial del repo (Sprints 0-2.0) tuvo commits directos a main porque la estructura no estaba lista. A partir del Sprint 2.1, todo via PR.

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

- **Hernán**: branches `feature/sprint-N-...` (código)
- **Sebas**: branches `docs/sprint-N-...` (documentación)
- Cada uno reviewea el PR del otro antes de merge cuando sea posible

---

## 7. Cuándo NO seguir este flujo

**Únicas excepciones permitidas para commits directos a `main`:**
- Fix urgente del workflow de CI/CD que rompió todo (raro)
- Setup inicial del repo (ya pasó, en Sprints 0-2.0)

Cualquier otro caso, **abrir PR**.

---

## 8. Referencias

- [DECISIONES.md](./DECISIONES.md) — Decisiones técnicas del proyecto (sección 9 sobre Git)
- [SPRINTS_PLAN.xlsx](./SPRINTS_PLAN.xlsx) — Plan de los 12 sprints
- [GitHub Flow oficial](https://docs.github.com/en/get-started/using-github/github-flow)

---

¿Dudas? Preguntar antes de hacer un push directo a `main` — es la regla.
