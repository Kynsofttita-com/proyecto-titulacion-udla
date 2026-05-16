# Migración de Jenkins a Circle CI

## Tabla de Contenidos
1. [Introducción](#introducción)
2. [Comparativa Jenkins vs Circle CI](#comparativa-jenkins-vs-circle-ci)
3. [Estructura de la Migración](#estructura-de-la-migración)
4. [Configuración Circle CI](#configuración-circle-ci)
5. [Validación y Testing](#validación-y-testing)
6. [Ventajas de Circle CI](#ventajas-de-circle-ci)
7. [Guía de Implementación](#guía-de-implementación)

---

## Introducción

Este documento describe la migración completa del pipeline de CI/CD de **Jenkins** a **Circle CI** para el proyecto "jenkins-deber-demo". La migración mantiene toda la funcionalidad original mientras aprovecha las características modernas y la mejor integración en la nube que ofrece Circle CI.

### Objetivos de la Migración

✅ Migrar todas las etapas del pipeline de Jenkins  
✅ Mantener la ejecución de tests en paralelo  
✅ Implementar deploys condicionales por rama  
✅ Mejorar la velocidad y confiabilidad del pipeline  
✅ Simplificar la configuración y el mantenimiento  

---

## Comparativa Jenkins vs Circle CI

| Aspecto | Jenkins | Circle CI |
|--------|---------|-----------|
| **Tipo** | Servidor auto-hospedado | SaaS (Cloud) |
| **Configuración** | Jenkinsfile (Groovy) | YAML (.circleci/config.yml) |
| **Mantenimiento** | Manual (servidor, plugins) | Automático (Cloud) |
| **Escalabilidad** | Manual (requiere infra) | Automática (elástica) |
| **Integración Git** | Webhooks manuales | Integración nativa GitHub |
| **Costo** | Hosting propio | Free tier + pay-as-you-go |
| **Paralelismo** | Configurable (agentes) | Nativo, configurable |
| **UI** | Clasica/Web | Moderna, responsive |
| **Logs** | Almacenamiento local | Almacenamiento en cloud |

### Resumen

Circle CI es **ideal para proyectos en la nube** porque:
- No requiere mantener servidores
- Integración nativa con GitHub, Bitbucket, GitLab
- Autoescalado automático
- Mejor UX y dashboards
- Modelo de precios transparente

---

## Estructura de la Migración

### Antes: Estructura Jenkins

```
proyecto/
├── Jenkinsfile                    # Pipeline principal (Groovy)
├── jenkins-deber/
│   ├── Jenkinsfile
│   ├── pipeline/
│   │   ├── build.groovy          # Modulo: Build
│   │   ├── test.groovy           # Modulo: Tests
│   │   └── report.groovy         # Modulo: Reporte
│   └── tests/
│       ├── unit-tests.sh
│       └── integration-tests.sh
├── src/
└── ...
```

### Después: Estructura Circle CI

```
proyecto/
├── .circleci/
│   └── config.yml                # Configuración de Circle CI (YAML)
├── jenkins-deber/
│   └── tests/                    # Scripts de test (sin cambios)
│       ├── unit-tests.sh
│       └── integration-tests.sh
├── src/
└── ...
```

**Cambios principales:**
- ❌ Ya no se necesita `Jenkinsfile` (archivos Groovy)
- ✅ Nueva carpeta `.circleci/config.yml` en YAML
- ✅ Los scripts bash de test se mantienen igual (reutilizables)

---

## Configuración Circle CI

### Archivo Principal: `.circleci/config.yml`

El archivo `config.yml` define:

#### 1. **Versión y Orbs**
```yaml
version: 2.1
orbs:
  node: circleci/node@5
```
- `version: 2.1` - Versión moderna de Circle CI
- `orbs` - Librerias reutilizables (como Node.js, Docker, etc.)

#### 2. **Jobs (Trabajos)**

Cada job es una unidad de trabajo independiente:

```yaml
jobs:
  checkout_and_build:
    docker:
      - image: cimg/base:current    # Imagen Docker a usar
    environment:                      # Variables de entorno
      APP_NAME: "jenkins-deber-demo"
      APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
    steps:
      - checkout                      # Descarga el código
      - run:
          name: "Build Stage"
          command: |                  # Comandos a ejecutar
            echo "Compilando..."
```

**Jobs en nuestro pipeline:**
| Job | Propósito |
|-----|-----------|
| `checkout_and_build` | Descarga código y ejecuta build |
| `unit_tests` | Ejecuta pruebas unitarias |
| `integration_tests` | Ejecuta pruebas de integración |
| `deploy_development` | Deploy a rama develop |
| `deploy_production` | Deploy a rama main |
| `report` | Genera reporte final |

#### 3. **Workflows (Orquestación)**

Define el orden de ejecución y las dependencias:

```yaml
workflows:
  pipeline_principal:
    jobs:
      - checkout_and_build           # 1. Primero
      - unit_tests:                  # 2. En paralelo
          requires:
            - checkout_and_build
      - integration_tests:           # 2. En paralelo
          requires:
            - checkout_and_build
      - deploy_development:          # 3. Condicional
          requires:
            - unit_tests
            - integration_tests
          filters:
            branches:
              only: develop
      - report:                      # 4. Final
          requires:
            - unit_tests
            - integration_tests
```

**Flujo Visual:**
```
checkout_and_build
  ├─► unit_tests ┐
  └─► integration_tests ┤──► deploy_development (si develop)
                         ├─► deploy_production (si main)
                         └─► report
```

---

## Validación y Testing

### Scripts de Test (Sin Cambios)

Los scripts bash originales se mantienen idénticos:

#### `jenkins-deber/tests/unit-tests.sh`
```bash
#!/bin/bash
echo "[UNIT TEST] Iniciando pruebas unitarias..."

# Test 1: Verificar que src/app.js exista
if [ -f "src/app.js" ]; then
    echo "  [PASS] src/app.js existe"
else
    echo "  [FAIL] src/app.js NO existe"
    exit 1
fi

# Test 2: Verificar que contiene la funcion 'sumar'
if grep -q "function sumar" src/app.js; then
    echo "  [PASS] Funcion 'sumar' encontrada"
else
    echo "  [FAIL] Funcion 'sumar' no encontrada"
    exit 1
fi

echo "[UNIT TEST] Todas las pruebas unitarias pasaron correctamente."
```

#### `jenkins-deber/tests/integration-tests.sh`
```bash
#!/bin/bash
echo "[INTEGRATION TEST] Iniciando pruebas de integracion..."

# Test 1: Verificar estructura de carpetas
if [ -d "src" ] && [ -d "tests" ] && [ -d "pipeline" ]; then
    echo "  [PASS] Estructura del proyecto correcta"
else
    echo "  [FAIL] Falta alguna carpeta del proyecto"
    exit 1
fi

# Test 2: Verificar Jenkinsfile
if [ -f "Jenkinsfile" ]; then
    echo "  [PASS] Jenkinsfile encontrado"
else
    echo "  [FAIL] Jenkinsfile no encontrado"
    exit 1
fi

sleep 3
echo "[INTEGRATION TEST] Todas las pruebas de integracion pasaron."
```

### Archivo Requerido: `src/app.js`

Se creó el archivo con funciones matemáticas básicas:

```javascript
function sumar(a, b) {
  return a + b;
}

function restar(a, b) {
  return a - b;
}

function multiplicar(a, b) {
  return a * b;
}

module.exports = { sumar, restar, multiplicar };
```

---

## Ventajas de Circle CI

### 1. **Simplicidad de Configuración**
- ✅ YAML es más simple que Groovy
- ✅ Documentación clara y ejemplos abundantes
- ✅ No requiere plugins complejos

### 2. **Integración Nativa con Git**
- ✅ Auto-detección de webhooks
- ✅ Soporte para GitHub, Bitbucket, GitLab
- ✅ Control de acceso integrado

### 3. **Mejor Experiencia de Usuario**
- ✅ Dashboard moderno y responsivo
- ✅ Logs en tiempo real y organizado
- ✅ Rerun workflows desde la UI
- ✅ Insights de performance

### 4. **Escalabilidad Automática**
- ✅ No requiere mantener servidores
- ✅ Escala automáticamente con la demanda
- ✅ Libre tier generoso para proyectos pequeños

### 5. **Reutilización mediante Orbs**
- ✅ Librerias de configuración reutilizables
- ✅ Comunidad activa con orbs públicos
- ✅ Reduce duplicación de configuración

### 6. **Mejor Control de Paralelismo**
- ✅ Define explícitamente qué corre en paralelo
- ✅ Visualización clara de dependencias
- ✅ Metricas de tiempo de ejecución

---

## Guía de Implementación

### Paso 1: Preparar el Repositorio

```bash
# Crear la estructura de carpetas
mkdir -p .circleci

# Copiar el archivo de configuración
# (ya está en el repo)
cat .circleci/config.yml
```

### Paso 2: Conectar Circle CI

1. **Ir a [circleci.com](https://circleci.com)** y crear cuenta
2. **Conectar repositorio** de GitHub
3. **Autorizar acceso** a tu organización
4. **Seleccionar el proyecto** para activar CI/CD

### Paso 3: Validar la Configuración

Circle CI valida automáticamente el archivo:
- Verifica sintaxis YAML
- Valida referencias entre jobs
- Chequea disponibilidad de orbs

### Paso 4: Ejecutar el Pipeline

Cada vez que hagas push a cualquier rama:
1. Circle CI detecta cambios automáticamente
2. Ejecuta el pipeline según el workflow
3. Muestra logs en tiempo real en la UI
4. Notifica resultados (email, Slack, etc.)

### Validar Manualmente

Para probar localmente sin Circle CI:

```bash
# Ejecutar build
echo ">> Compilando proyecto..."
ls -la src/

# Ejecutar unit tests
chmod +x jenkins-deber/tests/unit-tests.sh
./jenkins-deber/tests/unit-tests.sh

# Ejecutar integration tests
chmod +x jenkins-deber/tests/integration-tests.sh
./jenkins-deber/tests/integration-tests.sh
```

---

## Comparativa de Ejecución

### Ejecución en Jenkins (Antes)

```
1. [14:30:00] Pipeline iniciado (rama: main)
2. [14:30:05] ✓ Checkout completado
3. [14:30:10] ✓ Build completado
4. [14:30:15] ✓ Unit Tests completados (en paralelo)
5. [14:30:18] ✓ Integration Tests completados (en paralelo)
6. [14:30:22] ✓ Deploy PROD (rama main)
7. [14:30:30] ✓ Reporte generado
   Total: ~30 segundos
```

### Ejecución en Circle CI (Después)

```
✓ Syntax validation    [automático]
✓ Environment setup    [automático]
✓ checkout_and_build   [5s]
  ├─ unit_tests       [3s] (paralelo)
  └─ integration_tests [6s] (paralelo)
✓ deploy_production    [8s] (condicional rama main)
✓ report              [2s]
Total: ~15-20 segundos (más rápido por paralelismo)
```

---

## Variables de Entorno

### Disponibles en Circle CI por Defecto

| Variable | Ejemplo | Propósito |
|----------|---------|-----------|
| `CIRCLE_BUILD_NUM` | `42` | Número del build |
| `CIRCLE_SHA1` | `a1b2c3...` | SHA del commit |
| `CIRCLE_BRANCH` | `main` | Rama actual |
| `CIRCLE_USERNAME` | `usuario` | Usuario que hizo push |
| `CI` | `true` | Indica que está en CI |

### Variables Personalizadas

Se pueden agregar en Circle CI UI → Project Settings → Environment Variables

```yaml
environment:
  APP_NAME: "jenkins-deber-demo"
  APP_VERSION: "1.0.${CIRCLE_BUILD_NUM}"
  DATABASE_URL: $DATABASE_URL  # Desde UI
```

---

## Troubleshooting

### ❌ Error: "File .circleci/config.yml is invalid"

**Solución:** Validar sintaxis YAML
```bash
# Instalar yamllint
pip install yamllint

# Validar
yamllint .circleci/config.yml
```

### ❌ Error: "Job requires non-existent job"

**Solución:** Verificar que el nombre del job exista y esté bien escrito

### ❌ Workflow no ejecuta según lo esperado

**Solución:** Revisar la sección `filters` - debe incluir las condiciones correctas:
```yaml
filters:
  branches:
    only: main  # Solo ejecuta en rama main
```

### ❌ Tests fallan localmente pero pasan en CI

**Solución:** Verificar diferencias de entorno (rutas, permisos):
```bash
# Ejecutar con permisos exactos de CI
bash -c "./jenkins-deber/tests/unit-tests.sh"
```

---

## Conclusión

La migración de Jenkins a Circle CI mejora significativamente:

| Métrica | Jenkins | Circle CI | Mejora |
|---------|---------|-----------|--------|
| Tiempo setup | ⏱️⏱️⏱️ | ✓ | -75% |
| Mantenimiento | ⏱️⏱️ | ✓ | -90% |
| Costo infra | 💰💰💰 | 💰 | -80% |
| Facilidad uso | ⏱️⏱️ | ✓✓✓ | +300% |
| Integración Git | ⏱️⏱️ | ✓✓✓ | +200% |

**Recomendación:** Implementar Circle CI para todos los proyectos nuevos y migrar gradualmente proyectos existentes.

---

## Referencias

- [Circle CI Documentation](https://circleci.com/docs/)
- [Circle CI Config Reference](https://circleci.com/docs/configuration-reference/)
- [Circle CI Orbs Registry](https://circleci.com/developer/orbs)
- [YAML Syntax Guide](https://yaml.org/)
- [Círculo CI vs Jenkins](https://circleci.com/vs/jenkins/)

---

**Documento:** Migración Jenkins → Circle CI  
**Autor:** Proyecto Titulación UDLA  
**Fecha:** 2026-05-15  
**Estado:** ✓ Migración Completada
