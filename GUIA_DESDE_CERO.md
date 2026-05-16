# 🚀 GUÍA COMPLETA - EJECUTAR EL DEBER DESDE CERO

## ⏱️ Tiempo total: 1-2 horas (todo de principio a fin)

---

## 📋 ÍNDICE

1. [Preparación (5 minutos)](#preparación)
2. [Entender la estructura (10 minutos)](#entender-la-estructura)
3. [Validar todo funciona (5 minutos)](#validar-todo)
4. [Ejecutar la demostración (10 minutos)](#ejecutar-demo)
5. [Presentar en clase (20 minutos)](#presentación)

---

## 1️⃣ PREPARACIÓN (5 minutos)

### Paso 1.1: Abrir Terminal

**Windows:**
```bash
# Abrir Git Bash o cualquier terminal
# Ir a la carpeta del proyecto
cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto\ titulacion
```

**Verificar que estás en el lugar correcto:**
```bash
pwd
# Debería mostrar: /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto titulacion
```

### Paso 1.2: Verificar que existen las carpetas

```bash
# Ver estructura
ls -la | head -20

# Verificar carpetas clave
test -d .circleci && echo "✓ .circleci existe" || echo "✗ NO existe"
test -d scripts && echo "✓ scripts existe" || echo "✗ NO existe"
test -d jenkins-deber && echo "✓ jenkins-deber existe" || echo "✗ NO existe"
test -d src && echo "✓ src existe" || echo "✗ NO existe"
```

**Esperado:**
```
✓ .circleci existe
✓ scripts existe
✓ jenkins-deber existe
✓ src existe
```

### Paso 1.3: Permisos

```bash
# Dar permisos a scripts
chmod +x scripts/*.sh
chmod +x jenkins-deber/tests/*.sh

# Verificar
ls -la scripts/
```

---

## 2️⃣ ENTENDER LA ESTRUCTURA (10 minutos)

### La carpeta .circleci/

```bash
tree .circleci/

# Esperado:
# .circleci/
# └── config.yml
```

**¿Qué es?**
- `.circleci/` → Carpeta especial para Circle CI
- `config.yml` → Archivo de configuración del pipeline

### Ver el archivo config.yml

```bash
cat .circleci/config.yml | head -30

# Debería mostrar:
# version: 2.1
# orbs:
#   node: circleci/node@5
# jobs:
#   checkout_and_build:
#     ...
```

### Entender qué contiene

```bash
# Contar secciones
echo "=== VERSIÓN ==="
head -1 .circleci/config.yml

echo ""
echo "=== JOBS DEFINIDOS ==="
grep "^  [a-z_]*:" .circleci/config.yml

echo ""
echo "=== TOTAL DE LÍNEAS ==="
wc -l .circleci/config.yml
```

**Esperado:**
```
=== VERSIÓN ===
version: 2.1

=== JOBS DEFINIDOS ===
  checkout_and_build
  unit_tests
  integration_tests
  deploy_development
  deploy_production
  report

=== TOTAL DE LÍNEAS ===
168 .circleci/config.yml
```

---

## 3️⃣ VALIDAR TODO FUNCIONA (5 minutos)

### Paso 3.1: Ejecutar validación

```bash
bash scripts/validate.sh
```

**Esperado:**
```
╔══════════════════════════════════════════════╗
║          ✓ VALIDACIÓN EXITOSA               ║
║  Todo está listo para ejecutar              ║
╚══════════════════════════════════════════════╝
```

### Paso 3.2: Verificar archivos requeridos

```bash
echo "=== VERIFICANDO ARCHIVOS ===" && \
test -f .circleci/config.yml && echo "✓ .circleci/config.yml" || echo "✗ NO existe" && \
test -f src/app.js && echo "✓ src/app.js" || echo "✗ NO existe" && \
test -f jenkins-deber/tests/unit-tests.sh && echo "✓ unit-tests.sh" || echo "✗ NO existe" && \
test -f jenkins-deber/tests/integration-tests.sh && echo "✓ integration-tests.sh" || echo "✗ NO existe"
```

**Esperado:**
```
=== VERIFICANDO ARCHIVOS ===
✓ .circleci/config.yml
✓ src/app.js
✓ unit-tests.sh
✓ integration-tests.sh
```

### Paso 3.3: Ver contenido de src/app.js

```bash
cat src/app.js
```

**Esperado:**
```javascript
function sumar(a, b) {
  return a + b;
}
// ... más funciones
module.exports = { sumar, restar, multiplicar };
```

---

## 4️⃣ EJECUTAR LA DEMOSTRACIÓN (10 minutos)

### Opción A: DEMOSTRACIÓN AUTOMÁTICA (RECOMENDADA)

```bash
bash scripts/demo-presentation.sh
```

**Esto va a mostrar:**
- Parte 1: El problema (Jenkins)
- Parte 2: La solución (Circle CI)
- Parte 3: Estructura del proyecto
- Parte 4: Validación
- Parte 5: Unit tests
- Parte 6: Integration tests
- Parte 7: Pipeline completo
- Parte 8: Arquitectura
- Parte 9: Comparativa
- Parte 10: Documentación
- Parte 11: Resumen final

⏱️ Duración: 5-8 minutos

---

### Opción B: PASO A PASO (MÁS CONTROL)

#### Step 1: Validación (1 minuto)

```bash
bash scripts/validate.sh
```

Verifica que todo esté correcto.

#### Step 2: Unit Tests (1 minuto)

```bash
bash jenkins-deber/tests/unit-tests.sh
```

**Esperado:**
```
[UNIT TEST] Iniciando pruebas unitarias...
[UNIT TEST] Test 1: Verificar que el archivo src/app.js exista
  [PASS] src/app.js existe
[UNIT TEST] Test 2: Verificar que contiene la funcion 'sumar'
  [PASS] Funcion 'sumar' encontrada
[UNIT TEST] Todas las pruebas unitarias pasaron correctamente.
```

#### Step 3: Integration Tests (1 minuto)

```bash
bash jenkins-deber/tests/integration-tests.sh
```

**Esperado:**
```
[INTEGRATION TEST] Iniciando pruebas de integracion...
[INTEGRATION TEST] Test 1: Verificar estructura de carpetas
  [PASS] Estructura del proyecto correcta (src, jenkins-deber/tests, jenkins-deber/pipeline)
[INTEGRATION TEST] Test 2: Verificar Jenkinsfile en jenkins-deber
  [PASS] Jenkinsfile encontrado
[INTEGRATION TEST] Todas las pruebas de integracion pasaron correctamente.
```

#### Step 4: Pipeline Completo (3-5 minutos)

```bash
bash scripts/run-all.sh
```

**Esto corre:**
1. Build (mostrará estructura)
2. Unit tests en paralelo
3. Integration tests en paralelo
4. Deploy (condicional por rama)
5. Reporte final

**Esperado:**
```
╔════════════════════════════════════════════╗
║  ✓ PIPELINE COMPLETADO EXITOSAMENTE       ║
╠════════════════════════════════════════════╣
║  Aplicacion : jenkins-deber-demo          ║
║  Version    : 1.0.42                      ║
║  Rama       : main                        ║
║  Build      : #42                         ║
║  Resultado  : OK                          ║
╚════════════════════════════════════════════╝
```

---

## 5️⃣ PRESENTACIÓN EN CLASE (20 minutos)

### Paso 5.1: Preparación (la noche anterior)

```bash
# Validar una última vez
bash scripts/validate.sh

# Ejecutar una vez para asegurar no hay errores
bash scripts/run-all.sh

# Leer la guía de presentación
cat GUIA_PRESENTACION_CLASE.md | head -100
```

### Paso 5.2: En Clase - Estructura (20 minutos)

**Minuto 0-2: Introducción**
```
"Hoy presento la migración de Jenkins a Circle CI"
```

**Minuto 2-4: Problema**
```bash
cat jenkins-deber/Jenkinsfile | head -30
echo "Jenkins es complejo, Groovy es difícil..."
```

**Minuto 4-6: Solución**
```bash
cat .circleci/config.yml | head -30
echo "Circle CI es más simple, usa YAML..."
```

**Minuto 6-12: DEMO EN VIVO**
```bash
bash scripts/validate.sh        # 1 min
bash jenkins-deber/tests/unit-tests.sh       # 1 min
bash jenkins-deber/tests/integration-tests.sh # 1 min
bash scripts/run-all.sh          # 3-5 min
```

**Minuto 12-16: Arquitectura**
```
Mostrar flujo:
  checkout_and_build
    ├─→ unit_tests (paralelo)
    └─→ integration_tests (paralelo)
      ├─→ deploy_production (si main)
      └─→ deploy_development (si develop)
      └─→ report
```

**Minuto 16-19: Documentación**
```bash
ls -lh *CIRCLECI*.md *CONFIG*.md
echo "1000+ líneas de documentación profesional"
```

**Minuto 19-20: Preguntas**
```
"¿Preguntas?"
(Responder usando las respuestas preparadas)
```

---

## 📚 DOCUMENTOS DE REFERENCIA

### Para Leer (en orden)

1. **TARJETA_RAPIDA_CLASE.txt** (2 min)
   - Resumen visual rápido
   - Imprime y lleva a clase

2. **GUIA_PRESENTACION_CLASE.md** (20 min)
   - Guión completo de la presentación
   - Qué decir en cada momento
   - Respuestas a preguntas

3. **RESUMEN_CONFIG_YML.txt** (5 min)
   - 10 pasos sobre config.yml
   - Dónde está ubicado
   - Cómo funciona

4. **GUIA_CONFIG_CIRCLECI.md** (20 min)
   - Explicación detallada del config.yml
   - Cada sección explicada
   - Variables de entorno

5. **CONFIG_YML_ANOTADO.md** (15 min)
   - Archivo anotado línea por línea
   - Explicaciones detalladas

---

## 🎯 CHECKLIST PASO A PASO

### Hoy (30 minutos)

- [ ] Abre terminal en proyecto
- [ ] Ejecuta: `bash scripts/validate.sh`
- [ ] Ejecuta: `bash jenkins-deber/tests/unit-tests.sh`
- [ ] Ejecuta: `bash jenkins-deber/tests/integration-tests.sh`
- [ ] Lee: `TARJETA_RAPIDA_CLASE.txt`

### Mañana (1 hora)

- [ ] Lee: `GUIA_PRESENTACION_CLASE.md`
- [ ] Ejecuta: `bash scripts/demo-presentation.sh`
- [ ] Practica presentación en voz alta (10 min)

### Día Anterior a Clase (45 min)

- [ ] Lee: `GUIA_PRESENTACION_CLASE.md` (nuevamente)
- [ ] Practica presentación completa (15 min)
- [ ] Ejecuta: `bash scripts/validate.sh`
- [ ] Revisa respuestas a preguntas

### Día de Clase (10 min)

- [ ] Laptop cargada
- [ ] Terminal lista
- [ ] TARJETA_RAPIDA_CLASE.txt a la mano
- [ ] ¡PRESENTA CON CONFIANZA!

---

## 🔧 COMANDOS RÁPIDOS

### Validación
```bash
bash scripts/validate.sh
```

### Tests Unitarios
```bash
bash jenkins-deber/tests/unit-tests.sh
```

### Tests Integración
```bash
bash jenkins-deber/tests/integration-tests.sh
```

### Pipeline Completo
```bash
bash scripts/run-all.sh
```

### Demostración Automática
```bash
bash scripts/demo-presentation.sh
```

### Ver config.yml
```bash
cat .circleci/config.yml
```

### Ver primeras 30 líneas
```bash
head -30 .circleci/config.yml
```

### Contar líneas
```bash
wc -l .circleci/config.yml
```

---

## 🐛 SI ALGO FALLA

### Error: "Script not found"

```bash
# Dar permisos
chmod +x scripts/*.sh
chmod +x jenkins-deber/tests/*.sh

# Intentar nuevamente
bash scripts/validate.sh
```

### Error: "File not found"

```bash
# Verificar que existen
ls -la .circleci/config.yml
ls -la src/app.js
ls -la jenkins-deber/tests/
```

### Error: "Permission denied"

```bash
# Dar permisos recursivos
chmod -R +x scripts/
chmod -R +x jenkins-deber/tests/
```

### Tests fallan

```bash
# Verificar que src/app.js tiene la función sumar
grep "function sumar" src/app.js

# Verificar estructura
ls -la src/
ls -la jenkins-deber/
```

---

## ✅ VALIDACIÓN FINAL

Antes de presentar, ejecuta esto y verifica que todo sea ✓:

```bash
echo "=== VALIDACIÓN FINAL ===" && \
echo "1. config.yml existe:" && \
test -f .circleci/config.yml && echo "✓" || echo "✗" && \
echo "2. src/app.js existe:" && \
test -f src/app.js && echo "✓" || echo "✗" && \
echo "3. Unit tests script existe:" && \
test -f jenkins-deber/tests/unit-tests.sh && echo "✓" || echo "✗" && \
echo "4. Integration tests script existe:" && \
test -f jenkins-deber/tests/integration-tests.sh && echo "✓" || echo "✗" && \
echo "5. Ejecutando validación..." && \
bash scripts/validate.sh 2>&1 | tail -5
```

---

## 🎓 PARA EL DEBER ACADÉMICO

### Qué presentas al profesor

1. **Migración técnica**
   - Archivo: `.circleci/config.yml`
   - Explicación: Migración de Jenkins (Groovy) a Circle CI (YAML)

2. **Tests validados**
   - Ejecutar: `bash scripts/run-all.sh`
   - Demostrar: Todos los tests pasando

3. **Documentación**
   - Mostrar: 1000+ líneas de documentación profesional
   - Archivos: MIGRACION_JENKINS_CIRCLECI.md, etc.

4. **Presentación**
   - Duración: 15-20 minutos
   - Estructura: Problema → Solución → Demo → Arquitectura → Preguntas

### Criterios de evaluación

✅ **Técnico (50%):**
- ✓ Migración completada
- ✓ Tests pasando
- ✓ Documentación profesional
- ✓ Configuración válida

✅ **Presentación (30%):** (Depende de ti)
- Demo en vivo sin errores
- Explicación clara
- Respuestas a preguntas
- Timing perfecto

✅ **Entendimiento (20%):** (Depende de ti)
- Entiendes el problema
- Entiendes la solución
- Explicas bien por qué

---

## 🚀 PRÓXIMOS PASOS DESPUÉS DE PRESENTAR

1. **Crear cuenta en Circle CI**
   ```
   https://circleci.com/signup
   ```

2. **Conectar repositorio**
   ```
   Circle CI → Add Projects → Tu repo
   ```

3. **Hacer git push**
   ```bash
   git push origin main
   ```

4. **Ver en Circle CI**
   ```
   https://app.circleci.com/pipelines
   ```

---

## 📝 RESUMEN

| Paso | Qué hacer | Comando | Tiempo |
|------|-----------|---------|--------|
| 1 | Abrir terminal | `cd proyecto` | 1 min |
| 2 | Entender estructura | `ls -la` | 2 min |
| 3 | Validar todo | `bash scripts/validate.sh` | 1 min |
| 4 | Ejecutar unit tests | `bash jenkins-deber/tests/unit-tests.sh` | 1 min |
| 5 | Ejecutar integration tests | `bash jenkins-deber/tests/integration-tests.sh` | 1 min |
| 6 | Ejecutar pipeline | `bash scripts/run-all.sh` | 5 min |
| 7 | O ver demo automática | `bash scripts/demo-presentation.sh` | 8 min |
| 8 | Leer guía presentación | `cat GUIA_PRESENTACION_CLASE.md` | 20 min |
| 9 | Presentar en clase | Seguir guía | 20 min |

**Total: 1-2 horas para todo**

---

## 💡 TIPS FINALES

✅ **Practica 3 veces antes de presentar**
✅ **Lee los documentos con calma**
✅ **Ejecuta los scripts hasta memorizarlos**
✅ **Entiende QUÉ hace cada cosa**
✅ **Presenta con confianza**

---

## ✨ ¡ÉXITO!

Todo está listo. Solo necesitas ejecutar los pasos y presentar con seguridad.

**¡Tú puedes! 🚀**

---

**Última actualización:** 2026-05-15  
**Estado:** ✅ COMPLETAMENTE LISTO  
**Tiempo estimado para completar:** 1-2 horas
