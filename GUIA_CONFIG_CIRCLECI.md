# 📖 Guía Completa: Configuración de .circleci/config.yml

## 📍 UBICACIÓN DEL ARCHIVO

```
C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto titulacion\
└── .circleci/
    └── config.yml  ← AQUÍ ESTÁ EL ARCHIVO
```

### Para verificar que existe:
```bash
ls -la .circleci/config.yml
# Debería mostrar: -rw-r--r-- ... config.yml
```

---

## 📋 ESTRUCTURA DEL ARCHIVO (120 líneas)

```
config.yml
├─ version: 2.1                    (línea 1)
├─ orbs:                           (líneas 6-7)
├─ jobs:                           (líneas 12-126)
│  ├─ checkout_and_build          (líneas 13-45)
│  ├─ unit_tests                  (líneas 47-60)
│  ├─ integration_tests           (líneas 62-75)
│  ├─ deploy_development          (líneas 77-90)
│  ├─ deploy_production           (líneas 92-105)
│  └─ report                      (líneas 107-126)
└─ workflows:                      (líneas 131-169)
   └─ pipeline_principal          (líneas 134-169)
```

---

## 🔍 SECCIÓN 1: VERSION (Línea 1)

```yaml
version: 2.1
```

**Qué es:**
- La versión de Circle CI que usamos
- `2.1` es la más moderna

**Para qué sirve:**
- Circle CI necesita saber qué versión de sintaxis uses
- Versiones: 2.0 (antigua), 2.1 (actual), 3.0 (futura)

**¿Lo cambio?**
- ❌ NO. Déjalo así.

---

## 📦 SECCIÓN 2: ORBS (Líneas 6-7)

```yaml
orbs:
  node: circleci/node@5
```

**Qué es:**
- Librerías reutilizables que proporciona Circle CI
- `circleci/node@5` es una librería de Node.js

**Para qué sirve:**
- Node sirve para proyectos con JavaScript/TypeScript
- En nuestro caso, casi no lo usamos (es simulado)

**¿Lo cambio?**
- ❌ NO. Está bien como está.

---

## 🔧 SECCIÓN 3: JOBS (Líneas 12-126)

Un **JOB** es una unidad de trabajo que Circle CI ejecuta.

Tenemos **6 jobs**:

### 3.1 JOB: checkout_and_build (Líneas 13-45)

```yaml
checkout_and_build:
  docker:
    - image: cimg/base:current        ← Imagen Docker a usar
  environment:                         ← Variables de entorno
    APP_NAME: "jenkins-deber-demo"
    APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
  steps:                               ← Pasos a ejecutar
    - checkout                         ← Descargar código
    - run:                             ← Ejecutar comando
        name: "Display Build Info"
        command: |
          echo "Información del build"
    - run:
        name: "Build Stage"
        command: |
          echo "Compilando..."
    - persist_to_workspace:            ← Guardar archivos
        root: .
        paths:
          - .
```

**¿Qué hace cada parte?**

| Parte | Qué es | Para qué |
|-------|--------|----------|
| `docker` | Imagen del contenedor | Ambiente donde corre |
| `image: cimg/base:current` | Imagen Linux | Sistema operativo |
| `environment` | Variables de entorno | Datos disponibles en todos los pasos |
| `APP_NAME` | Variable personalizada | Nombre de la app |
| `CIRCLE_BUILD_NUM` | Variable de Circle CI | Número del build (automática) |
| `steps` | Lista de pasos | Qué hacer en orden |
| `checkout` | Descargar git | Traer código del repositorio |
| `run` | Ejecutar comando | Cualquier comando bash |
| `persist_to_workspace` | Guardar archivos | Para que otros jobs usen estos archivos |

**¿Lo cambio?**
- ❌ NO. Todo está correcto.

---

### 3.2 JOB: unit_tests (Líneas 47-60)

```yaml
unit_tests:
  docker:
    - image: cimg/base:current
  steps:
    - attach_workspace:              ← Recuperar archivos del job anterior
        at: .
    - run:
        name: "Unit Tests"
        command: |
          chmod +x jenkins-deber/tests/unit-tests.sh
          ./jenkins-deber/tests/unit-tests.sh
```

**¿Qué hace?**

1. **attach_workspace** - Recupera los archivos que guardó `checkout_and_build`
2. **chmod +x** - Da permiso de ejecución al script
3. **./jenkins-deber/tests/unit-tests.sh** - Ejecuta los tests unitarios

**¿Lo cambio?**
- ❌ NO. Todo está correcto.

---

### 3.3 JOB: integration_tests (Líneas 62-75)

```yaml
integration_tests:
  docker:
    - image: cimg/base:current
  steps:
    - attach_workspace:
        at: .
    - run:
        name: "Integration Tests"
        command: |
          chmod +x jenkins-deber/tests/integration-tests.sh
          ./jenkins-deber/tests/integration-tests.sh
```

**¿Qué hace?**
- Exactamente lo mismo que `unit_tests` pero con el script de integración

**¿Lo cambio?**
- ❌ NO. Todo está correcto.

---

### 3.4 JOB: deploy_development (Líneas 77-90)

```yaml
deploy_development:
  docker:
    - image: cimg/base:current
  environment:
    APP_NAME: "jenkins-deber-demo"
    APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
  steps:
    - checkout
    - run:
        name: "Deploy to Development"
        command: |
          echo ">> Desplegando a DEV"
```

**¿Qué hace?**
- Simula un deploy a ambiente de desarrollo
- Solo imprime mensajes (es simulado)

**¿Lo cambio?**
- ❌ NO. Está bien para demostración.
- Si fuera real, aquí iría código para desplegar a AWS, Heroku, etc.

---

### 3.5 JOB: deploy_production (Líneas 92-105)

```yaml
deploy_production:
  docker:
    - image: cimg/base:current
  environment:
    APP_NAME: "jenkins-deber-demo"
    APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
  steps:
    - checkout
    - run:
        name: "Deploy to Production"
        command: |
          echo ">> Desplegando a PROD"
```

**¿Qué hace?**
- Simula un deploy a ambiente de producción
- Solo imprime mensajes

**¿Lo cambio?**
- ❌ NO. Está bien para demostración.

---

### 3.6 JOB: report (Líneas 107-126)

```yaml
report:
  docker:
    - image: cimg/base:current
  environment:
    APP_NAME: "jenkins-deber-demo"
    APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
  steps:
    - run:
        name: "Final Report"
        command: |
          echo "================================================"
          echo "  ETAPA: REPORTE FINAL"
          echo "================================================"
          echo ">> Aplicacion : ${APP_NAME}"
          echo ">> Version    : ${APP_VERSION}"
          echo ">> Rama       : ${CIRCLE_BRANCH}"
          echo ">> Build      : #${CIRCLE_BUILD_NUM}"
          echo ">> Resultado  : OK"
          echo "================================================"
```

**¿Qué hace?**
- Imprime un reporte final con información del build
- Es el último job que se ejecuta

**¿Lo cambio?**
- ❌ NO. Está perfecto.

---

## 🔀 SECCIÓN 4: WORKFLOWS (Líneas 131-169)

El **WORKFLOW** define el ORDEN y las DEPENDENCIAS de los jobs.

```yaml
workflows:
  version: 2
  
  pipeline_principal:
    jobs:
      - checkout_and_build              # Job 1
      
      - unit_tests:
          requires:
            - checkout_and_build        # Job 2 (después de Job 1)
      
      - integration_tests:
          requires:
            - checkout_and_build        # Job 2 (después de Job 1, paralelo con unit_tests)
      
      - deploy_development:
          requires:
            - unit_tests
            - integration_tests         # Job 3 (después de Job 2)
          filters:
            branches:
              only: develop             # ← SOLO en rama 'develop'
      
      - deploy_production:
          requires:
            - unit_tests
            - integration_tests         # Job 3 (después de Job 2)
          filters:
            branches:
              only: main                # ← SOLO en rama 'main'
      
      - report:
          requires:
            - unit_tests
            - integration_tests         # Job 4 (al final)
```

**¿Qué hace cada parte?**

| Línea | Qué es | Para qué |
|-------|--------|----------|
| `version: 2` | Versión del workflow | Indicar versión a Circle CI |
| `pipeline_principal` | Nombre del workflow | Identificador único |
| `jobs:` | Lista de jobs | Qué jobs ejecutar |
| `requires:` | Dependencias | "Este job requiere que terminen estos primero" |
| `filters:` | Condiciones | "Ejecutar solo en ciertas ramas" |
| `branches:` | Rama | Especificar rama |
| `only: develop` | Rama develop | Ejecutar SOLO en rama develop |
| `only: main` | Rama main | Ejecutar SOLO en rama main |

**¿Lo cambio?**
- ❌ NO. La lógica es perfecta como está.

---

## 📊 FLUJO DE EJECUCIÓN

Cuando haces **git push**:

```
1. checkout_and_build        (2-3 segundos)
   └─ Descarga código, verifica estructura
   
   ├─→ 2a. unit_tests         (1-2 segundos, EN PARALELO)
   │       └─ Ejecuta unit-tests.sh
   │
   └─→ 2b. integration_tests  (3-4 segundos, EN PARALELO)
           └─ Ejecuta integration-tests.sh
   
   ├─→ 3a. deploy_development (3-5 segundos, SOLO si rama = develop)
   │       └─ Simula deploy a DEV
   │
   ├─→ 3b. deploy_production  (3-5 segundos, SOLO si rama = main)
   │       └─ Simula deploy a PROD
   │
   └─→ 4. report              (1 segundo)
           └─ Imprime reporte final

DURACIÓN TOTAL: 15-25 segundos
```

---

## ✅ VALIDACIÓN DEL CONFIG.YML

### Paso 1: Verificar que el archivo existe

```bash
ls -la .circleci/config.yml

# Esperado:
# -rw-r--r-- 1 hmate 197609 5214 may 15 20:17 .circleci/config.yml
```

### Paso 2: Ver contenido del archivo

```bash
cat .circleci/config.yml | head -20

# Esperado: primeras 20 líneas del archivo
```

### Paso 3: Contar líneas

```bash
wc -l .circleci/config.yml

# Esperado: 169 (el archivo tiene 169 líneas)
```

### Paso 4: Validar sintaxis YAML (si tienes yamllint)

```bash
# Instalar yamllint (si no lo tienes)
pip install yamllint

# Validar
yamllint .circleci/config.yml

# Esperado: SIN ERRORES (sin output)
```

### Paso 5: Verificar que contiene lo correcto

```bash
# Ver jobs definidos
grep "^  [a-z_]*:" .circleci/config.yml | head -10

# Esperado:
#   checkout_and_build
#   unit_tests
#   integration_tests
#   deploy_development
#   deploy_production
#   report
```

### Paso 6: Verificar workflows

```bash
# Ver workflow
grep -A 50 "^workflows:" .circleci/config.yml | head -30

# Esperado: debe mostrar pipeline_principal con todos los jobs
```

---

## 🎯 CHECKLIST DE VALIDACIÓN

Ejecuta estos comandos uno por uno:

```bash
# ✅ Paso 1: Archivo existe
test -f .circleci/config.yml && echo "✓ Archivo existe" || echo "✗ Archivo NO existe"

# ✅ Paso 2: Tiene contenido
test -s .circleci/config.yml && echo "✓ Archivo tiene contenido" || echo "✗ Archivo vacío"

# ✅ Paso 3: Empieza con version 2.1
head -1 .circleci/config.yml | grep "version: 2.1" && echo "✓ Version correcta" || echo "✗ Version incorrecta"

# ✅ Paso 4: Contiene 6 jobs
grep -c "^  [a-z_]*:" .circleci/config.yml | grep "6" && echo "✓ 6 jobs encontrados" || echo "✗ Jobs incorrectos"

# ✅ Paso 5: Contiene workflows
grep -q "^workflows:" .circleci/config.yml && echo "✓ Workflows encontrados" || echo "✗ Workflows NO encontrados"

# ✅ Paso 6: Contiene pipeline_principal
grep -q "pipeline_principal:" .circleci/config.yml && echo "✓ Pipeline encontrado" || echo "✗ Pipeline NO encontrado"
```

**Ejecutar todo junto:**

```bash
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion

echo "=== VALIDACIÓN DE CONFIG.YML ==="
test -f .circleci/config.yml && echo "✓ Archivo existe" || echo "✗ NO existe"
test -s .circleci/config.yml && echo "✓ Tiene contenido" || echo "✗ Vacío"
head -1 .circleci/config.yml | grep "version: 2.1" && echo "✓ Version correcta" || echo "✗ Version incorrecta"
grep -q "^  checkout_and_build:" .circleci/config.yml && echo "✓ checkout_and_build encontrado" || echo "✗ NO encontrado"
grep -q "^  unit_tests:" .circleci/config.yml && echo "✓ unit_tests encontrado" || echo "✗ NO encontrado"
grep -q "^  integration_tests:" .circleci/config.yml && echo "✓ integration_tests encontrado" || echo "✗ NO encontrado"
grep -q "^workflows:" .circleci/config.yml && echo "✓ Workflows encontrados" || echo "✗ NO encontrados"
grep -q "pipeline_principal:" .circleci/config.yml && echo "✓ Pipeline principal encontrado" || echo "✗ NO encontrado"

echo ""
echo "Total de líneas:"
wc -l .circleci/config.yml
```

---

## 🔧 CÓMO MODIFICAR EL CONFIG.YML

**Si necesitas cambiar algo:**

### Ejemplo 1: Cambiar nombre de la app

```yaml
# Línea 17 - ANTES:
APP_NAME: "jenkins-deber-demo"

# DESPUÉS:
APP_NAME: "mi-app-especial"
```

### Ejemplo 2: Cambiar tiempo de timeout

```yaml
# Agregar después de "docker:" (línea 14):
docker:
  - image: cimg/base:current
  resource_class: medium  # ← Agregar esto
```

### Ejemplo 3: Agregar email de notificación

```yaml
# Al final del archivo, después de workflows:
notify:
  - email:
      to: tu@email.com
```

---

## 📱 VARIABLES DE ENTORNO DISPONIBLES

En el config.yml tienes acceso a estas variables (automáticas):

| Variable | Valor | Ejemplo |
|----------|-------|---------|
| `CIRCLE_BUILD_NUM` | Número del build | 42 |
| `CIRCLE_BRANCH` | Rama actual | main, develop |
| `CIRCLE_SHA1` | Commit SHA | a1b2c3d4... |
| `CIRCLE_USERNAME` | Usuario de Git | hernanj |
| `CI` | Verdadero en CI | true |

Usas así:
```yaml
echo "Build #${CIRCLE_BUILD_NUM}"
echo "Rama: ${CIRCLE_BRANCH}"
echo "Commit: ${CIRCLE_SHA1:0:8}"  # ← Primeros 8 caracteres
```

---

## ⚙️ CÓMO USAR EL CONFIG.YML EN CIRCLE CI

### Paso 1: Crear Cuenta Circle CI
```
https://circleci.com/signup
```

### Paso 2: Conectar Repositorio GitHub
```
Circle CI Dashboard → Add Projects → Seleccionar tu repo
```

### Paso 3: Auto-detección
```
Circle CI detectará automáticamente:
.circleci/config.yml
```

### Paso 4: Primer Pipeline
```
Cuando hagas git push, Circle CI:
1. Lee .circleci/config.yml
2. Ejecuta los jobs según el workflow
3. Muestra logs en tiempo real
```

### Paso 5: Ver Resultados
```
https://app.circleci.com/pipelines
└─ Tu repo → Build #X
   ├─ checkout_and_build
   ├─ unit_tests (paralelo)
   ├─ integration_tests (paralelo)
   ├─ deploy_development o deploy_production
   └─ report
```

---

## 🐛 ERRORES COMUNES

### ❌ Error: "Invalid config.yml"

**Causa:** Sintaxis YAML incorrecta

**Solución:**
```bash
# Validar
yamllint .circleci/config.yml

# Errores comunes:
# - Indentación (debe ser 2 espacios)
# - Comillas sin cerrar
# - Colones en el lugar equivocado
```

### ❌ Error: "Job not found"

**Causa:** El nombre del job no coincide

**Solución:**
```bash
# Verificar nombres:
grep "^  [a-z_]*:" .circleci/config.yml

# Deben coincidir con los "requires:"
```

### ❌ Error: "Syntax error on line X"

**Causa:** Error en la sintaxis YAML

**Solución:**
```bash
# Revisar la línea X
sed -n 'X p' .circleci/config.yml

# Comunes:
# - Falta indentación
# - Carácter raro
# - Espacio al inicio
```

---

## ✨ RESUMEN RÁPIDO

| Qué es | Dónde | Qué hace |
|--------|-------|----------|
| **version** | Línea 1 | Indica versión de Circle CI (2.1) |
| **orbs** | Líneas 6-7 | Importa librerías reutilizables |
| **jobs** | Líneas 12-126 | Define 6 trabajos diferentes |
| **workflows** | Líneas 131-169 | Orquesta el orden de ejecución |
| **checkout_and_build** | Job 1 | Descarga código y verifica |
| **unit_tests** | Job 2 | Ejecuta tests unitarios |
| **integration_tests** | Job 2 | Ejecuta tests de integración |
| **deploy_development** | Job 3 | Deploy a DEV (si rama develop) |
| **deploy_production** | Job 3 | Deploy a PROD (si rama main) |
| **report** | Job 4 | Reporte final |

---

## 🎯 PASOS FINALES

**Para validar que TODO funciona:**

```bash
# 1. Validar archivo
yamllint .circleci/config.yml

# 2. Ver el contenido
cat .circleci/config.yml

# 3. Ejecutar simulación local
bash scripts/validate.sh
bash scripts/run-all.sh

# 4. Subir a GitHub
git add .circleci/config.yml
git commit -m "config: Circle CI configurado"
git push origin main

# 5. Ver en Circle CI
# Ir a: https://app.circleci.com/pipelines
# Debería mostrar tu pipeline ejecutándose
```

---

## 💡 IMPORTANTE

✅ El archivo `.circleci/config.yml` está **COMPLETAMENTE CONFIGURADO**

❌ NO necesitas hacer cambios

✅ Solo necesitas **entender** cómo funciona

✅ Puedes **usarlo tal como está** en Circle CI

---

**¡Listo! Ya sabes exactamente dónde está y cómo funciona el config.yml** 🎉
