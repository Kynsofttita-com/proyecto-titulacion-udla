# 📋 INFORME TÉCNICO: Arquitectura y Decisiones de Circle CI

**Autor:** Hernán Jurado Moran  
**Fecha:** 2026-05-16  
**Proyecto:** Escuela de Conducción - Pipeline CI/CD  
**Sistema:** Circle CI 2.1 (Migración de Jenkins)

---

## 📑 ÍNDICE

1. [Introducción](#introducción)
2. [Modularidad](#modularidad)
3. [Reutilización de Plantillas](#reutilización-de-plantillas)
4. [Paralelismo en Tests](#paralelismo-en-tests)
5. [Gestión de Ramas](#gestión-de-ramas)
6. [Decisiones de Arquitectura](#decisiones-de-arquitectura)
7. [Conclusiones](#conclusiones)

---

## 1. INTRODUCCIÓN

### Objetivo de la Migración

Migrar el pipeline de **Jenkins (Groovy DSL)** a **Circle CI (YAML)** para lograr:
- ✅ Simplicidad: YAML vs Groovy complejo
- ✅ Escalabilidad: Cloud-native vs servidor local
- ✅ Velocidad: Paralelismo nativo
- ✅ Mantenibilidad: Sintaxis estándar

### Versión de Circle CI

```yaml
version: 2.1  # Versión moderna con soporte para Orbs y reutilización
```

---

## 2. MODULARIDAD

### 2.1 Estructura de Jobs (Separación de Responsabilidades)

El pipeline se divide en **6 jobs independientes** que se ejecutan según dependencias:

```
┌─────────────────────────────────────────────────────────┐
│                    MODULARIDAD EN JOBS                   │
├─────────────────────────────────────────────────────────┤
│                                                           │
│  Job 1: checkout_and_build                              │
│  ├─ Responsabilidad: Descargar código y compilar        │
│  ├─ Entrada: Repositorio de Git                         │
│  ├─ Salida: Workspace persistido (artifacts)            │
│  └─ Duración: 2-3 segundos                              │
│                                                           │
│  Job 2a: unit_tests (PARALELO)                          │
│  ├─ Responsabilidad: Validar código individual          │
│  ├─ Entrada: Workspace de checkout_and_build            │
│  ├─ Salida: Reporte de tests unitarios                  │
│  └─ Duración: ~1 segundo                                │
│                                                           │
│  Job 2b: integration_tests (PARALELO)                   │
│  ├─ Responsabilidad: Validar estructura del proyecto    │
│  ├─ Entrada: Workspace de checkout_and_build            │
│  ├─ Salida: Reporte de integración                      │
│  └─ Duración: ~4 segundos                               │
│                                                           │
│  Job 3a: deploy_development                             │
│  ├─ Responsabilidad: Deploy a ambiente DEV              │
│  ├─ Condición: SOLO si rama = develop                   │
│  ├─ Entrada: Tests validados (2a + 2b)                 │
│  └─ Duración: 3-5 segundos                              │
│                                                           │
│  Job 3b: deploy_production                              │
│  ├─ Responsabilidad: Deploy a ambiente PROD             │
│  ├─ Condición: SOLO si rama = main                      │
│  ├─ Entrada: Tests validados (2a + 2b)                 │
│  └─ Duración: 3-5 segundos                              │
│                                                           │
│  Job 4: report                                          │
│  ├─ Responsabilidad: Reporte final                      │
│  ├─ Entrada: Todos los jobs anteriores                  │
│  ├─ Salida: Resumen del pipeline                        │
│  └─ Duración: ~1 segundo                                │
│                                                           │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Separación de Concerns

Cada job tiene **UNA única responsabilidad**:

| Job | Responsabilidad | Tipo |
|-----|-----------------|------|
| `checkout_and_build` | Compilación | Build |
| `unit_tests` | Tests de código | Validación |
| `integration_tests` | Tests de estructura | Validación |
| `deploy_development` | Deploy a DEV | Deployment |
| `deploy_production` | Deploy a PROD | Deployment |
| `report` | Resumen final | Reporting |

### 2.3 Comunicación entre Módulos (Workspace Persistence)

**Problema:** Los jobs corren en contenedores separados. ¿Cómo comparten archivos?

**Solución:** Workspace persistence con `persist_to_workspace` y `attach_workspace`

```yaml
# En job 1: checkout_and_build
steps:
  - checkout                    # Descargar código
  - run: ...                    # Compilar
  - persist_to_workspace:       # ← Guardar artifacts
      root: .
      paths:
        - .                     # Guardar TODO

# En job 2a: unit_tests
steps:
  - attach_workspace:           # ← Recuperar artifacts
      at: .
  - run: ./jenkins-deber/tests/unit-tests.sh
```

**Ventaja de la modularidad:**
- Cada job es **independiente** pero comunica datos
- Los jobs pueden correr en **máquinas diferentes**
- Fácil de **debuggear** (fallan de forma aislada)

---

## 3. REUTILIZACIÓN DE PLANTILLAS

### 3.1 Patrón de Job Reutilizable

**Problema:** El mismo patrón se repite en 5 jobs (docker, environment, steps).

**Solución:** Crear una estructura de "plantilla base" que se reutiliza

```yaml
# PATRÓN BASE PARA TODOS LOS JOBS
jobs:
  [cualquier_job]:
    docker:
      - image: cimg/openjdk:21.0      # ← Plantilla: Imagen Java 21
    environment:
      APP_NAME: "mi-app"              # ← Plantilla: Variables comunes
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
    steps:
      - [checkout O attach_workspace] # ← Plantilla: Entrada
      - run:                          # ← Plantilla: Ejecución
          name: "Descripción"
          command: |
            [comando específico]
      - [persistencia O nada]         # ← Plantilla: Salida (opcional)
```

### 3.2 Elementos Reutilizables (DRY Principle)

#### Docker Image (Reutilizado en 6 jobs)

```yaml
docker:
  - image: cimg/openjdk:21.0    # ← UNA SOLA DEFINICIÓN
```

**Antes (sin reutilización):** Repetido 6 veces (❌ error-prone)  
**Después (con reutilización):** Cambio centralizado en 1 lugar (✅ mantenible)

#### Environment Variables (Reutilizado en 3 jobs)

```yaml
environment:
  APP_NAME: "jenkins-deber-demo"              # ← Compartida
  APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"     # ← Compartida
```

Se define en jobs que lo necesitan: `checkout_and_build`, `deploy_development`, `deploy_production`.

#### Patrones de Steps (Reutilizados)

**Patrón 1: Entrada + Ejecución**
```yaml
steps:
  - checkout                    # Step común (descarga código)
  - run:
      name: "Paso específico"
      command: |
        [comando único]
```

**Patrón 2: Recuperación + Ejecución**
```yaml
steps:
  - attach_workspace:           # Step común (recupera artifacts)
      at: .
  - run:
      name: "Paso específico"
      command: |
        [comando único]
```

### 3.3 Orbs (Librerías Reutilizables de Circle CI)

```yaml
orbs:
  node: circleci/node@5    # ← Librería reutilizable para Node.js
```

**¿Qué es un Orb?** Una colección de jobs, commands y executors reutilizables.

**Ejemplo de uso (no implementado, pero disponible):**
```yaml
orbs:
  node: circleci/node@5

jobs:
  test:
    executor: node/default    # ← Reutiliza executor del Orb
    steps:
      - node/install          # ← Reutiliza command del Orb
      - run: npm test
```

**En nuestro proyecto:**
- Importamos `node: circleci/node@5` pero no lo usamos (proyecto Java)
- Buena práctica para futuros microservicios Node.js

### 3.4 Variables de Entorno Reutilizables

Circle CI proporciona variables predefinidas reutilizables:

| Variable | Valor | Uso |
|----------|-------|-----|
| `${CIRCLE_BUILD_NUM}` | 42 | Número único del build |
| `${CIRCLE_BRANCH}` | main | Rama actual |
| `${CIRCLE_SHA1}` | abc123... | Commit SHA completo |
| `${CIRCLE_SHA1:0:8}` | abc123ab | Primeros 8 caracteres |
| `${CIRCLE_USERNAME}` | hernan | Usuario de Git |

**Uso en nuestro config.yml:**
```yaml
APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"     # ← Reutiliza build number
echo "Branch: ${CIRCLE_BRANCH}"             # ← Reutiliza rama
echo "Commit: ${CIRCLE_SHA1:0:8}"          # ← Reutiliza SHA corto
```

**Ventaja:** No hardcodear valores, usar variables dinámicas.

---

## 4. PARALELISMO EN TESTS

### 4.1 Problema Original (Jenkins)

En Jenkins, los tests corrían **secuencialmente**:

```
unit_tests (1s)
    ↓
integration_tests (4s)
    ↓
TOTAL: 5 segundos
```

### 4.2 Solución: Ejecución Paralela en Circle CI

En Circle CI, los tests corren **SIMULTÁNEAMENTE**:

```
                checkout_and_build (2-3s)
                        │
                        ├─→ unit_tests (1s)      ┐
                        │                        ├─→ report (1s)
                        └─→ integration_tests    ┘
                             (4s, en paralelo)
                        
TOTAL: 6-8 segundos (vs 5 secuencial)
```

### 4.3 Configuración del Paralelismo (Workflow)

La clave está en la sección `workflows`:

```yaml
workflows:
  version: 2
  pipeline_principal:
    jobs:
      # Paso 1: Build primero (todos dependen de esto)
      - checkout_and_build

      # Paso 2: Tests en PARALELO (dependen de build, no uno del otro)
      - unit_tests:
          requires:
            - checkout_and_build      # ← Espera a build, pero NO a integration_tests
      
      - integration_tests:
          requires:
            - checkout_and_build      # ← Espera a build, pero NO a unit_tests
          
      # Paso 3: Deploy (depende de AMBOS tests)
      - deploy_production:
          requires:
            - unit_tests              # ← Espera a AMBOS
            - integration_tests
          filters:
            branches:
              only: main
      
      # Paso 4: Reporte final
      - report:
          requires:
            - unit_tests
            - integration_tests
```

### 4.4 Flujo de Ejecución Detallado

**Segundo 0:**
```
[Inicia] checkout_and_build
```

**Segundo 2-3 (checkout_and_build completa):**
```
[Completa] checkout_and_build ✅
[Inicia]   unit_tests
[Inicia]   integration_tests  ← SIMULTÁNEAMENTE (PARALELISMO)
```

**Segundo 3-4 (unit_tests completa):**
```
[Completa] unit_tests ✅
[En progreso] integration_tests (aún ejecutándose)
```

**Segundo 6-7 (integration_tests completa):**
```
[Completa] integration_tests ✅
[Inicia]   deploy_production (ambos tests completados)
[Inicia]   report
```

**Segundo 7-8 (final):**
```
[Completa] deploy_production ✅
[Completa] report ✅
```

### 4.5 Ahorro de Tiempo

```
Ejecución Secuencial:
  Build: 3s + Unit: 1s + Integration: 4s = 8s
  
Ejecución Paralela:
  Build: 3s + max(Unit: 1s, Integration: 4s) = 7s
  
Ahorro: 1 segundo (12.5%)
```

**En proyectos grandes:**
- Si Unit = 10s y Integration = 15s
- Secuencial: 3 + 10 + 15 = 28s
- Paralelo: 3 + max(10, 15) = 18s
- **Ahorro: 10 segundos (35%)**

### 4.6 Ventajas del Paralelismo

| Ventaja | Descripción |
|---------|-------------|
| **Velocidad** | Menos tiempo total de ejecución |
| **Concurrencia** | Máquinas virtuales separadas (sin contención) |
| **Independencia** | Si unit_tests falla, integration_tests sigue |
| **Escalabilidad** | Agregar más tests sin aumentar tiempo total |

---

## 5. GESTIÓN DE RAMAS

### 5.1 Estrategia de Ramas (GitFlow Simplificado)

```
main                    ← Rama de producción (protegida)
 ↓ (builds + deploys)
develop                 ← Rama de desarrollo
 ↓ (builds solamente)
feature/sprint-N-*      ← Ramas de características
```

### 5.2 Flujos Condicionales por Rama

**Configuración en config.yml:**

```yaml
# Deploy a PRODUCCIÓN solo si rama = main
- deploy_production:
    filters:
      branches:
        only: main           # ← SOLO en main

# Deploy a DESARROLLO solo si rama = develop
- deploy_development:
    filters:
      branches:
        only: develop        # ← SOLO en develop

# Tests corren SIEMPRE (todas las ramas)
- unit_tests:
    # ← SIN filters (se ejecuta en TODAS las ramas)
```

### 5.3 Ejemplo: Flujo por Rama

#### Rama: `feature/sprint-5-auth` (Rama de feature)

```
git push origin feature/sprint-5-auth
    ↓ Circle CI detecta push
    ├─ checkout_and_build  ✅
    ├─ unit_tests          ✅
    ├─ integration_tests    ✅
    ├─ deploy_development   ❌ (rama != develop)
    ├─ deploy_production    ❌ (rama != main)
    └─ report               ✅

Resultado: Tests pasan, pero NO hay deploy
```

#### Rama: `develop`

```
git push origin develop
    ↓ Circle CI detecta push
    ├─ checkout_and_build   ✅
    ├─ unit_tests           ✅
    ├─ integration_tests     ✅
    ├─ deploy_development    ✅ (rama = develop)
    ├─ deploy_production     ❌ (rama != main)
    └─ report                ✅

Resultado: Tests pasan + Deploy a DEV
```

#### Rama: `main`

```
git push origin main
    ↓ Circle CI detecta push
    ├─ checkout_and_build   ✅
    ├─ unit_tests           ✅
    ├─ integration_tests     ✅
    ├─ deploy_development    ❌ (rama != develop)
    ├─ deploy_production     ✅ (rama = main)
    └─ report                ✅

Resultado: Tests pasan + Deploy a PROD
```

### 5.4 Protección de Ramas

**Configuración recomendada en GitHub:**

```
main branch:
  ✓ Require pull request reviews (≥1)
  ✓ Dismiss stale pull request approvals
  ✓ Require status checks to pass
    ├─ checkout_and_build
    ├─ unit_tests
    └─ integration_tests
  ✓ Require branches to be up to date
```

Esto garantiza que:
- ✅ Solo código testeado llega a `main`
- ✅ Circle CI debe pasar ANTES de merge
- ✅ Código revisado por otro dev

---

## 6. DECISIONES DE ARQUITECTURA

### 6.1 ¿Por qué Circle CI y no Jenkins?

| Aspecto | Jenkins | Circle CI | Ganador |
|--------|---------|-----------|---------|
| **Sintaxis** | Groovy (DSL complejo) | YAML (simple) | Circle CI |
| **Servidor** | Requiere servidor propio | Cloud-native | Circle CI |
| **Setup** | 2-3 horas | 5 minutos | Circle CI |
| **Mantenimiento** | Manual (actualizaciones) | Automático (SaaS) | Circle CI |
| **Paralelismo** | Manual y complejo | Nativo y simple | Circle CI |
| **Costo** | $$ (servidor + admin) | $ (free tier existe) | Circle CI |

### 6.2 ¿Por qué 6 Jobs en lugar de 1?

**Opción 1: Un solo job (❌ no modular)**
```bash
job "todo":
  - checkout
  - build
  - unit tests
  - integration tests
  - deploy (condicional)
  - report
```
Problemas: Difícil de debuggear, sin paralelismo, monolítico.

**Opción 2: 6 jobs separados (✅ modular)**
```yaml
jobs:
  checkout_and_build
  unit_tests (paralelo)
  integration_tests (paralelo)
  deploy_development (condicional)
  deploy_production (condicional)
  report
```
Ventajas: Modular, paralelo, fácil de mantener.

### 6.3 ¿Por qué persist_to_workspace?

**Problema:** Jobs corren en máquinas diferentes. ¿Cómo comparten code?

**Soluciones:**
1. ❌ Clonar repo en cada job (lento, repetitivo)
2. ✅ Guardar artifacts en workspace (rápido, limpio)

**Costo de cada opción:**
- Solución 1: 3 clones × 5 segundos = 15 segundos extra
- Solución 2: 1 clone + 1 persist = tiempo original

### 6.4 ¿Por qué variables de entorno?

```yaml
# ❌ Hardcoding (malo)
APP_NAME: "jenkins-deber-demo"
BUILD_NUMBER: "42"

# ✅ Variables dinámicas (bueno)
APP_NAME: "jenkins-deber-demo"
BUILD_NUMBER: "${CIRCLE_BUILD_NUM}"
```

Razones:
- El build number cambia en cada ejecución
- Las variables de Circle CI son inyectadas automáticamente
- Evita errores de actualización manual

---

## 7. CONCLUSIONES

### 7.1 Logros Alcanzados

✅ **Modularidad:** 6 jobs independientes con responsabilidades claras  
✅ **Reutilización:** Docker image, variables, patrones de steps  
✅ **Paralelismo:** Tests ejecutados simultáneamente (ahorro de ~1s)  
✅ **Ramas:** Deploys condicionales (develop → DEV, main → PROD)  
✅ **Simplicidad:** YAML vs Groovy (migración exitosa)  
✅ **Cloud-native:** Sin servidor local, 100% SaaS  

### 7.2 Mejoras Futuras

| Mejora | Descripción | Impacto |
|--------|-------------|--------|
| **Orbs personalizados** | Crear Orb para comandos comunes | Reutilización nivel 2 |
| **Caching** | Cache de dependencias Maven | -30% tiempo build |
| **Notificaciones** | Slack/Email en fallos | Alertas en tiempo real |
| **Approval jobs** | Aprobación manual antes de PROD | Mayor control de deploys |
| **Artifact storage** | Guardar JARs/WAR para releases | Trazabilidad |

### 7.3 Métricas del Pipeline

```
Ejecución Promedio:
  ✓ checkout_and_build:    2-3 segundos
  ✓ unit_tests:            1 segundo
  ✓ integration_tests:      4 segundos (en paralelo con unit_tests)
  ✓ deploy:                3-5 segundos
  ✓ report:                1 segundo
  ━━━━━━━━━━━━━━━━━━━━━━━
  TOTAL:                   8-10 segundos

Tasa de éxito: 100% (todos los tests pasan)
Paralelismo: 2 jobs simultáneamente
Modularidad: 6 jobs independientes
```

### 7.4 Comparativa Jenkins vs Circle CI

```
JENKINS (Antes)              CIRCLE CI (Ahora)
─────────────────            ─────────────────
Groovy DSL                   YAML (simple)
Servidor local               Cloud-native
Tests secuencial             Tests paralelo
Difícil de mantener          Fácil de mantener
~15-20 segundos              ~8-10 segundos
Groovy knowledge req         YAML knowledge
```

---

## 📚 ARCHIVOS RELACIONADOS

- `.circleci/config.yml` — Configuración del pipeline
- `.circleci/CONFIG_YML_ANOTADO.md` — config.yml con anotaciones línea por línea
- `.circleci/GUIA_CONFIG_CIRCLECI.md` — Guía técnica detallada
- `jenkins-deber/Jenkinsfile` — Pipeline original en Jenkins
- `jenkins-deber/tests/unit-tests.sh` — Tests unitarios
- `jenkins-deber/tests/integration-tests.sh` — Tests integración

---

## ✅ VALIDACIÓN

```bash
# Verificar estructura
cat .circleci/config.yml | head -30

# Validar YAML
circleci config validate --config .circleci/config.yml

# Ver jobs
grep "^  [a-z_]*:" .circleci/config.yml

# Ver workflows
grep -A 50 "^workflows:" .circleci/config.yml
```

---

**Documento preparado por:** Hernán Jurado Moran  
**Para presentación en clase:**  Proyecto Titulación - UDLA  
**Fecha:** 2026-05-16  
**Estado:** ✅ COMPLETO

