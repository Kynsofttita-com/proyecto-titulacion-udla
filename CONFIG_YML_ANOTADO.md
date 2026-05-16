# 📄 config.yml - Archivo Completo Explicado Línea por Línea

## 📍 UBICACIÓN

```
.circleci/config.yml
5.2 KB | 168 líneas
```

---

## 📋 ESTRUCTURA VISUAL DEL ARCHIVO

```
.circleci/config.yml
│
├─ SECCIÓN 1: VERSION (línea 1)
│  └─ version: 2.1
│
├─ SECCIÓN 2: ORBS (líneas 6-7)
│  └─ node: circleci/node@5
│
├─ SECCIÓN 3: JOBS (líneas 12-126)
│  ├─ checkout_and_build (13-45)
│  ├─ unit_tests (47-60)
│  ├─ integration_tests (62-75)
│  ├─ deploy_development (77-90)
│  ├─ deploy_production (92-105)
│  └─ report (107-126)
│
└─ SECCIÓN 4: WORKFLOWS (líneas 131-169)
   └─ pipeline_principal
      ├─ checkout_and_build
      ├─ unit_tests (requiere: checkout_and_build)
      ├─ integration_tests (requiere: checkout_and_build)
      ├─ deploy_development (requiere: unit_tests + integration_tests, SOLO rama develop)
      ├─ deploy_production (requiere: unit_tests + integration_tests, SOLO rama main)
      └─ report (requiere: unit_tests + integration_tests)
```

---

## 🔍 CONTENIDO DEL ARCHIVO ANOTADO

### SECCIÓN 1: VERSION (Línea 1)

```yaml
version: 2.1                                     # ← Versión moderna de Circle CI
                                                # Solo hay 2 versiones principales:
                                                # 2.0 = antigua
                                                # 2.1 = actual (USAMOS ESTA)
```

**¿Qué significa?**
- Circle CI necesita saber qué sintaxis usamos
- `2.1` es la versión moderna con features como orbs

---

### SECCIÓN 2: ORBS (Líneas 6-7)

```yaml
# =============================================================================
# ORBS (Librerias reutilizables)
# =============================================================================
orbs:                                           # ← Sección de librerías
  node: circleci/node@5                         # ← Importar librería Node.js
```

**¿Qué significa?**
- `orbs:` = "Importar librerías"
- `node: circleci/node@5` = "Importar Node.js versión 5"
- Circle CI proporciona librerías para lenguajes comunes
- En nuestro caso no la usamos mucho, pero es buena práctica importarla

**Otras librerías disponibles:**
```yaml
orbs:
  python: circleci/python@2
  go: circleci/go@1
  aws-cli: circleci/aws-cli@2
  docker: circleci/docker@2
```

---

### SECCIÓN 3: JOBS (Líneas 12-126)

Un job es una unidad de trabajo independiente.

#### JOB 1: checkout_and_build (Líneas 13-45)

```yaml
jobs:                                           # ← Inicio de jobs
  checkout_and_build:                           # ← NOMBRE DEL JOB
    docker:                                     # ← Especificar contenedor
      - image: cimg/base:current                # ← Imagen Docker a usar
                                                #    cimg = Circle CI Image
                                                #    base = Sistema base
                                                #    current = Última versión
    environment:                                # ← Variables de entorno
      APP_NAME: "jenkins-deber-demo"            # ← Variable personalizada
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"   # ← Variable con # de build
    steps:                                      # ← Pasos a ejecutar en orden
      - checkout                                # ← Step 1: Descargar código
      - run:                                    # ← Step 2: Ejecutar comando
          name: "Display Build Info"            # ← Nombre descriptivo
          command: |                            # ← Comando bash
            echo "================================================"
            echo "  Pipeline iniciado"
            echo "  Rama   : ${CIRCLE_BRANCH}"  # ← Variable de Circle CI
            echo "  Commit : ${CIRCLE_SHA1:0:8}" # ← Primeros 8 caracteres
            echo "  Build #: ${CIRCLE_BUILD_NUM}"
            echo "================================================"
      - run:                                    # ← Step 3: Ejecutar comando
          name: "Build Stage"                   # ← Nombre descriptivo
          command: |                            # ← Comando bash
            echo "================================================"
            echo "  ETAPA: BUILD"
            echo "================================================"
            echo ">> Compilando proyecto: ${APP_NAME}"
            echo ">> Version: ${APP_VERSION}"
            echo "[BUILD] Verificando estructura del proyecto..."
            ls -la src/                         # ← Comando real (list src/)
            echo "[BUILD] Compilacion finalizada (simulada)."
            echo ">> Build completado correctamente."
      - persist_to_workspace:                  # ← Step 4: Guardar archivos
          root: .                               # ← Guardar desde carpeta root
          paths:                                # ← Qué guardar
            - .                                 # ← Guardar TODO
                                                #    (otros jobs lo usarán)
```

**¿Qué hace este job?**
1. Descarga el código (`checkout`)
2. Muestra información del build
3. Ejecuta el "build" (solo verifica estructura)
4. Guarda todos los archivos para que los otros jobs los usen

**Variables de Circle CI disponibles:**
| Variable | Significa |
|----------|-----------|
| `${CIRCLE_BUILD_NUM}` | Número del build (42) |
| `${CIRCLE_BRANCH}` | Rama (main, develop) |
| `${CIRCLE_SHA1}` | Commit SHA completo |
| `${CIRCLE_SHA1:0:8}` | Primeros 8 caracteres |
| `${CIRCLE_USERNAME}` | Usuario de Git |

---

#### JOB 2: unit_tests (Líneas 47-60)

```yaml
  unit_tests:                                   # ← NOMBRE DEL JOB
    docker:                                     # ← Usar contenedor
      - image: cimg/base:current                # ← Imagen Docker
    steps:                                      # ← Pasos
      - attach_workspace:                       # ← Step 1: Recuperar archivos
          at: .                                 # ← Recuperar desde checkout_and_build
      - run:                                    # ← Step 2: Ejecutar tests
          name: "Unit Tests"                    # ← Nombre del step
          command: |                            # ← Comando
            echo "================================================"
            echo "  ETAPA: TEST [UNIT]"
            echo "================================================"
            chmod +x jenkins-deber/tests/unit-tests.sh    # ← Dar permiso
            ./jenkins-deber/tests/unit-tests.sh           # ← Ejecutar
```

**¿Qué hace?**
1. Recupera los archivos guardados por `checkout_and_build`
2. Da permisos al script
3. Ejecuta `unit-tests.sh`

**¿Por qué `attach_workspace`?**
- Cada job corre en un contenedor separado
- Necesitamos compartir archivos entre jobs
- `persist_to_workspace` en job 1 guardó los archivos
- `attach_workspace` en job 2 los recupera

---

#### JOB 3: integration_tests (Líneas 62-75)

```yaml
  integration_tests:                            # ← NOMBRE DEL JOB
    docker:                                     # ← Usar contenedor
      - image: cimg/base:current                # ← Imagen Docker
    steps:                                      # ← Pasos
      - attach_workspace:                       # ← Recuperar archivos
          at: .
      - run:                                    # ← Ejecutar tests
          name: "Integration Tests"
          command: |
            echo "================================================"
            echo "  ETAPA: TEST [INTEGRATION]"
            echo "================================================"
            chmod +x jenkins-deber/tests/integration-tests.sh
            ./jenkins-deber/tests/integration-tests.sh
```

**¿Qué hace?**
- Exactamente lo mismo que `unit_tests`
- Pero ejecuta `integration-tests.sh`

**¿Por qué dos jobs de tests?**
- Se pueden ejecutar EN PARALELO
- Ahorra tiempo (no espera a que terminen uno tras otro)

---

#### JOB 4: deploy_development (Líneas 77-90)

```yaml
  deploy_development:                           # ← NOMBRE DEL JOB
    docker:                                     # ← Usar contenedor
      - image: cimg/base:current
    environment:                                # ← Variables de entorno
      APP_NAME: "jenkins-deber-demo"
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
    steps:                                      # ← Pasos
      - checkout                                # ← Descargar código
      - run:                                    # ← Ejecutar deploy
          name: "Deploy to Development"
          command: |
            echo ">> Desplegando ${APP_NAME} v${APP_VERSION} a entorno DEV (simulado)"
            echo "   Branch: develop"
            echo "   Commit: ${CIRCLE_SHA1:0:8}"
```

**¿Qué hace?**
- Simula un deploy a ambiente de desarrollo
- Solo imprime mensajes (es simulado)

**En producción real:**
```bash
# Aquí iría código para:
aws s3 sync . s3://my-bucket/dev/
docker push my-registry/app:dev
kubectl apply -f deployment-dev.yaml
```

---

#### JOB 5: deploy_production (Líneas 92-105)

```yaml
  deploy_production:                            # ← NOMBRE DEL JOB
    docker:                                     # ← Usar contenedor
      - image: cimg/base:current
    environment:                                # ← Variables de entorno
      APP_NAME: "jenkins-deber-demo"
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
    steps:                                      # ← Pasos
      - checkout                                # ← Descargar código
      - run:                                    # ← Ejecutar deploy
          name: "Deploy to Production"
          command: |
            echo ">> Desplegando ${APP_NAME} v${APP_VERSION} a entorno PROD (simulado)"
            echo "   Branch: main"
            echo "   Commit: ${CIRCLE_SHA1:0:8}"
```

**¿Qué hace?**
- Simula un deploy a ambiente de producción
- Solo imprime mensajes

---

#### JOB 6: report (Líneas 107-126)

```yaml
  report:                                       # ← NOMBRE DEL JOB
    docker:                                     # ← Usar contenedor
      - image: cimg/base:current
    environment:                                # ← Variables de entorno
      APP_NAME: "jenkins-deber-demo"
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
    steps:                                      # ← Pasos
      - run:                                    # ← Ejecutar reporte
          name: "Final Report"
          command: |
            echo "================================================"
            echo "  ETAPA: REPORTE FINAL"
            echo "================================================"
            echo ">> Aplicacion : ${APP_NAME}"
            echo ">> Version    : ${APP_VERSION}"
            echo ">> Rama       : ${CIRCLE_BRANCH}"
            echo ">> Build      : #${CIRCLE_BUILD_NUM}"
            echo ">> Commit     : ${CIRCLE_SHA1:0:8}"
            echo ">> Resultado  : OK"
            echo "================================================"
```

**¿Qué hace?**
- Imprime un reporte final bonito
- Último job que se ejecuta

---

### SECCIÓN 4: WORKFLOWS (Líneas 131-169)

El workflow define el ORDEN y las DEPENDENCIAS.

```yaml
# =============================================================================
# WORKFLOWS (Orquestacion de trabajos)
# =============================================================================
workflows:                                      # ← Sección de workflows
  version: 2                                    # ← Versión del workflow
  
  pipeline_principal:                           # ← NOMBRE DEL WORKFLOW
    jobs:                                       # ← Lista de jobs a ejecutar
      # Paso 1: Checkout y Build
      - checkout_and_build                      # ← Job 1: primero
      
      # Paso 2: Tests en paralelo (dependen de checkout_and_build)
      - unit_tests:                             # ← Job 2a: después del 1
          requires:                             # ← Condición: requiere
            - checkout_and_build                # ← Este job primero
      - integration_tests:                      # ← Job 2b: después del 1
          requires:                             # ← Condición: requiere
            - checkout_and_build                # ← Este job primero
      
      # Paso 3: Deploy condicional (depende de tests)
      - deploy_development:                     # ← Job 3a: si rama=develop
          requires:                             # ← Condición: requiere
            - unit_tests                        # ← Estos dos jobs
            - integration_tests                 # ← deben terminar primero
          filters:                              # ← Condición: rama
            branches:                           # ← Filtro de rama
              only: develop                     # ← SOLO en rama 'develop'
      
      - deploy_production:                      # ← Job 3b: si rama=main
          requires:                             # ← Condición: requiere
            - unit_tests                        # ← Estos dos jobs
            - integration_tests                 # ← deben terminar primero
          filters:                              # ← Condición: rama
            branches:                           # ← Filtro de rama
              only: main                        # ← SOLO en rama 'main'
      
      # Paso 4: Reporte final (depende de todo)
      - report:                                 # ← Job 4: al final
          requires:                             # ← Condición: requiere
            - unit_tests                        # ← Estos dos jobs
            - integration_tests                 # ← deben terminar primero
```

**¿Qué significa cada cosa?**

| Palabra clave | Significa | Ejemplo |
|---------------|-----------|---------|
| `workflows:` | Sección de workflows | Una colección de workflows |
| `pipeline_principal:` | Nombre del workflow | Este es el principal |
| `jobs:` | Lista de jobs | 6 jobs en total |
| `requires:` | Dependencias | Este job requiere que otros terminen |
| `filters:` | Condiciones | Ejecutar solo en ciertas ramas |
| `branches:` | Filtro de rama | Especificar rama(s) |
| `only:` | Solo en | Ejecutar SOLO en esta rama |

**Flujo de ejecución:**

```
1. checkout_and_build                 (2-3s)
   │
   ├─→ 2. unit_tests (paralelo)      (1-2s) ─┐
   │                                          │
   └─→ 3. integration_tests (paralelo) (3-4s)┤
                                              ├─→ Si rama=develop:
                                              │   4. deploy_development (3-5s)
                                              │
                                              ├─→ Si rama=main:
                                              │   5. deploy_production (3-5s)
                                              │
                                              └─→ 6. report (1s)

DURACIÓN TOTAL: 15-25 segundos (con paralelismo)
```

---

## 📊 RESUMEN DE SECCIONES

| Sección | Líneas | Propósito | Cambiable |
|---------|--------|-----------|-----------|
| Version | 1 | Versión de Circle CI | ❌ NO |
| Orbs | 6-7 | Librerías | ⚠️ SI (experto) |
| Jobs | 12-126 | Definir trabajos | ⚠️ SI (experto) |
| Workflows | 131-169 | Orquestación | ⚠️ SI (experto) |

---

## ✅ VALIDACIÓN RÁPIDA

Para verificar que el archivo está correcto:

```bash
# Verificar que existe
test -f .circleci/config.yml && echo "✓" || echo "✗"

# Ver línea 1
head -1 .circleci/config.yml

# Ver cantidad de líneas
wc -l .circleci/config.yml

# Ver archivos section
grep "^  [a-z_]*:" .circleci/config.yml

# Ver workflows
grep -A 50 "^workflows:" .circleci/config.yml
```

---

## 🎯 PUNTOS CLAVE

✅ El archivo está **100% correcto y configurado**

✅ **NO necesitas cambiarlo**

✅ Solo necesitas **entender** cómo funciona

✅ Puedes **usarlo tal como está** en Circle CI

---

## 📝 SI NECESITAS CAMBIAR ALGO

### Cambiar nombre de la app
```yaml
# Línea 17, ANTES:
APP_NAME: "jenkins-deber-demo"

# CAMBIAR A:
APP_NAME: "mi-app-nueva"
```

### Agregar un nuevo step
```yaml
# En la sección steps, agregar:
- run:
    name: "Mi nuevo paso"
    command: |
      echo "Esto es un nuevo paso"
```

### Cambiar rama de deployment
```yaml
# En deploy_development, línea 154, cambiar:
only: develop
# A:
only: feature/*  # Ejecutar en ramas que empiezan con feature/
```

---

## 🔗 REFERENCIAS

- [Circle CI 2.1 Documentation](https://circleci.com/docs/)
- [Jobs Configuration](https://circleci.com/docs/jobs-steps/)
- [Workflows Configuration](https://circleci.com/docs/workflows/)
- [Environment Variables](https://circleci.com/docs/env-vars/)

---

**¡Ahora entiendes completamente cómo funciona el config.yml!** 🎉
