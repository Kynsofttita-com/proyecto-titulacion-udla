# Circle CI - Quick Start Guide

## 🚀 En 5 Minutos

### 1. Crear Cuenta
```
Ir a https://circleci.com
Sign up con GitHub → Autorizar acceso
```

### 2. Conectar Proyecto
```
Selecciona tu repositorio
Haz clic en "Set Up Project"
Circle CI detectará .circleci/config.yml automáticamente
Haz clic en "Start Building"
```

### 3. ¡Listo!
```
Tu primer pipeline se ejecutará automáticamente
Verás logs en tiempo real en la UI
```

---

## 📊 Ver Resultados

### En Circle CI Dashboard

1. **Todos los builds**: click en tu nombre de usuario → "Builds"
2. **Detalles de un build**: click en el número (#42)
3. **Logs completos**: click en el job para expandir
4. **Timeline visual**: Ver qué corrió en paralelo

### Statuses Posibles

```
✅ SUCCESS  - Todos los jobs pasaron
🔄 RUNNING  - Pipeline ejecutándose
❌ FAILED   - Algún job falló
⏸️ HELD     - Esperando aprobación (si la hay)
```

---

## 🔄 Triggers Automáticos

El pipeline se ejecuta cuando:

```
1. Haces git push a cualquier rama
2. Abres un Pull Request
3. Merges a main/develop
```

No necesitas hacer nada más. Circle CI lo detecta automáticamente.

---

## 🛠️ Comandos Útiles

### Ver pipeline localmente

```bash
# Validar que la sintaxis YAML es correcta
pip install yamllint
yamllint .circleci/config.yml

# Ejecutar tests localmente como lo haría Circle CI
./jenkins-deber/tests/unit-tests.sh
./jenkins-deber/tests/integration-tests.sh
```

### Simular ejecución (sin Circle CI)

```bash
# Crear variable de entorno como lo hace Circle CI
export CIRCLE_BUILD_NUM=1
export CIRCLE_BRANCH=main

# Ejecutar jobs manualmente
echo "=== CHECKOUT & BUILD ==="
ls -la src/

echo "=== UNIT TESTS ==="
./jenkins-deber/tests/unit-tests.sh

echo "=== INTEGRATION TESTS ==="
./jenkins-deber/tests/integration-tests.sh
```

---

## 📈 Entender el Pipeline

### Archivo: `.circleci/config.yml`

```yaml
version: 2.1                    # Versión moderna de Circle CI

orbs:
  node: circleci/node@5         # Librerías reutilizables

jobs:
  checkout_and_build:           # Job 1: Build
    docker:
      - image: cimg/base:current
    steps:
      - checkout
      - run: [comandos]

  unit_tests:                   # Job 2: Tests paralelo
    requires:
      - checkout_and_build
    steps:
      - run: ./jenkins-deber/tests/unit-tests.sh

  deploy_production:            # Job 3: Deploy condicional
    filters:
      branches:
        only: main              # Solo en rama main
    requires:
      - unit_tests
      - integration_tests

workflows:                      # Orquestación
  pipeline_principal:
    jobs:
      - checkout_and_build
      - unit_tests:
          requires: [checkout_and_build]
      - deploy_production: [...]
```

---

## 🔐 Variables de Entorno

### Automáticas (proporcionadas por Circle CI)

```bash
CIRCLE_BUILD_NUM         # Número del build (42)
CIRCLE_BRANCH            # Rama (main, develop)
CIRCLE_SHA1              # Commit SHA (a1b2c3...)
CIRCLE_USERNAME          # Usuario de Git
CI                       # True en Circle CI
```

### Personalizadas

Agregar en Circle CI UI:
```
Project Settings → Environment Variables → Add Variable
```

Ejemplo:
```
DATABASE_URL=postgres://...
API_KEY=secret123
```

Usar en config.yml:
```yaml
environment:
  DATABASE_URL: $DATABASE_URL
```

---

## 🐛 Debugging

### Ver logs en tiempo real

1. Open Circle CI Dashboard
2. Click en "Build #42"
3. Los logs se actualizan en tiempo real
4. Click en cualquier job para expandir

### Rerun un build que falló

1. Haz clic en "Rerun workflow" (botón con flechas)
2. Elige "Rerun all jobs" o "Rerun failed jobs"
3. Vuelve a ver los logs

### SSH en el ambiente Circle CI

Útil para debugging complejo:

1. Haz clic en "Rerun with SSH"
2. Espera el comando SSH en los logs
3. Conecta via SSH y explora
4. Los archivos están en `/home/circleci/project/`

---

## ✅ Validación Rápida

### Tests Status

```bash
# Unit Tests
✅ PASS: src/app.js existe
✅ PASS: Funcion 'sumar' encontrada

# Integration Tests
✅ PASS: Estructura del proyecto correcta
✅ PASS: Jenkinsfile encontrado

# Deploy
✅ PASS: Deploy completado

# Report
✅ PASS: Reporte generado
```

---

## 📝 Cambios Respecto a Jenkins

| Aspecto | Jenkins | Circle CI |
|--------|---------|-----------|
| Archivo config | `Jenkinsfile` (Groovy) | `.circleci/config.yml` (YAML) |
| Ubicación | Raíz del proyecto | `.circleci/` |
| Infraestructura | Servidor propio | Cloud SaaS |
| Integración Git | Manual (webhooks) | Automática |
| Sintaxis | Groovy DSL | YAML estructurado |
| Paralelismo | Agentes Jenkins | Docker nativo |
| UI | Clasica | Moderna y responsiva |

---

## 🎯 Próximos Pasos

- [ ] Crear cuenta en Circle CI
- [ ] Conectar repositorio
- [ ] Ver primer pipeline ejecutarse
- [ ] Leer logs completamente
- [ ] Configurar notificaciones (Slack, email)
- [ ] Proteger ramas con "Require status checks"
- [ ] Integrar con otras herramientas (SonarQube, etc.)

---

## 💡 Tips Profesionales

### 1. **Caché para velocidad**

```yaml
steps:
  - restore_cache:
      keys:
        - v1-dependencies-{{ checksum "package.json" }}
  - run: npm install
  - save_cache:
      paths:
        - node_modules/
      key: v1-dependencies-{{ checksum "package.json" }}
```

### 2. **Artifacts para descargar**

```yaml
- store_artifacts:
    path: coverage/
    destination: coverage-report
```

### 3. **Notificaciones en Slack**

Instalar Circle CI app en Slack:
- Settings → Notifications → "Slack"
- Autorizar workspace

### 4. **Status checks en Pull Requests**

Circle CI automáticamente:
- Corre tests en PRs
- Bloquea merge si fallan
- Muestra resultados en GitHub

---

## 🆘 Soporte Rápido

| Problema | Solución |
|----------|----------|
| YAML inválido | `yamllint .circleci/config.yml` |
| Job no ejecuta | Verificar `filters:` y `requires:` |
| Permisos denied | Agregar `chmod +x script.sh` |
| Timeout | Aumentar `timeout` en job |
| Out of memory | Reducir paralelismo o recursos |

---

## 📚 Recursos

- [Circle CI Docs](https://circleci.com/docs/)
- [Config Reference](https://circleci.com/docs/configuration-reference/)
- [Orbs Registry](https://circleci.com/developer/orbs)
- [Ejemplos](https://circleci.com/docs/sample-config/)

---

## Ejemplo Completo de Uso

```bash
# 1. Hacer cambios en código
echo "console.log('hello')" >> src/app.js

# 2. Commit y push
git add .
git commit -m "Add feature"
git push origin main

# 3. Circle CI ejecuta automáticamente
# - checkout_and_build
# - unit_tests (paralelo)
# - integration_tests (paralelo)
# - deploy_production (si main)
# - report

# 4. Ver resultados
# Abrir: https://app.circleci.com/pipelines
# Click en tu pipeline
# Ver logs en tiempo real

# ¡Listo! 🎉
```

---

**¡Bienvenido a Circle CI!** 🚀

Para cualquier duda, la documentación oficial es tu mejor amigo.
