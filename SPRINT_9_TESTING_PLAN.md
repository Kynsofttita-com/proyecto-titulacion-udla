# Sprint 9 — Plan de Testing Manual

**Estado:** ⏳ Pendiente. Sprint 9 cerró el código pero **NO se hizo testing manual exhaustivo**. Hacer esto antes de crear el PR final / mergear a main.

**Fecha de cierre del código:** 2026-05-24 madrugada.
**Branch:** `feature/sprint-9-1-perfil-usuarios-estados`
**HEAD:** `03e21c2`

---

## 0) Preparación

```bash
cd "C:/Users/hmate/OneDrive/Desktop/UDLA/Proyecto titulacion"

# Stack arriba
docker compose -f infrastructure/docker/docker-compose.yml ps
# Si no, levantarlo:
# docker compose -f infrastructure/docker/docker-compose.yml up -d

# Frontend
cd frontend && npm run dev
# → http://localhost:5173

# Login
# admin@escuela.local / Admin123!
```

**IMPORTANTE:** Ctrl+Shift+R en cada navegación si hay dudas (Vite HMR cachea agresivo).

**Snapshot de datos actuales en BD:**

```
estudiantes:
 1 | Pedro Estudiante | PRE_MATRICULADO | PENDIENTE_MATRICULA  (sin tipo_curso)
 2 | Juan Test        | MATRICULADO     | PAGO_PARCIAL         (curso B $250)
 3 | Pedro Garcia     | CURSANDO        | PAGO_PARCIAL         (curso C $350)
 4 | Hernan Jurado    | MATRICULADO     | SIN_DEUDA            (sin facturas)

facturas:
 FAC-0001 | est=2 | CONTADO  | $280 → $150 pagado, saldo $130
 FAC-0002 | est=2 | CREDITO  | $900 → $300 pagado, saldo $600, 1/3 cuotas
 FAC-0003 | est=3 | CONTADO  | $250 → $100 pagado, saldo $150
```

---

## 1) Bug perfil resuelto

**Pasos:**
1. Login → llegas a Dashboard.
2. Click "Mi perfil" en sidebar.
3. **Sin recargar**, click "Estudiantes" en sidebar → debe cargar la lista.
4. Vuelve a "Mi perfil" → carga.
5. Click "Cobros y Pagos" → carga.

**Esperado:** ninguna pantalla en blanco. Si pasa, FAIL — reportar consola browser.

---

## 2) Form estudiante con Dropdown tipo de curso

**Dónde:** `/estudiantes → Nuevo estudiante`

**Verificar:**
- En la sección "Plan académico" hay un **Dropdown** (NO botones A-F).
- Al abrir el dropdown debe listar:
  - "Curso Basico Auto · Cat. B · 40 h · $250.00"
  - "Curso Profesional C · Cat. C · 60 h · $350.00"
  - "Curso Moto · Cat. A · 30 h · $180.00"
- Selecciona "Curso Basico Auto" → aparece **card verde** abajo: "Curso Basico Auto · Licencia categoría B · 40 horas de instrucción · podrás emitir facturas que sumen hasta $250.00".
- Llena resto del form y guarda → estudiante creado con `tipoCursoId=1`, `categoriaLicenciaId=...`.

**Bug a chequear:** al editar un estudiante existente, ¿el Dropdown precarga el tipo de curso actual? **(no estoy 100% seguro de que funcione)**.

---

## 3) Asignar tipo de curso a Pedro Estudiante #1

Pedro #1 hoy no tiene tipo de curso. Es buen candidato para probar el flujo completo.

**Pasos:**
1. `/estudiantes` → click "editar" sobre Pedro Estudiante.
2. Si el form no precarga el dropdown, selecciónalo manualmente: "Curso Basico Auto · $250".
3. Guarda.
4. Verifica en BD: `docker exec escuela-postgres psql -U escuela_user -d escuela_db -c "SELECT id, tipo_curso_id FROM estudiantes_schema.estudiantes WHERE id=1;"` → debe ser `1`.

---

## 4) Crear factura CONTADO con resumen académico

**Dónde:** `/cobros → Nueva factura`

**Pasos:**
1. Click "Nueva factura".
2. En AutoComplete escribe "Pedro" → selecciona "Pedro Estudiante".
3. **Verificar mini-card abajo del AutoComplete** muestra: Cédula / Estado académico **Pre-matriculado** / Situación pago **Pendiente matrícula** / Email.
4. **Verificar card verde grande del resumen:** Curso Basico Auto / Cat. B / 40 h / Precio $250.
5. Barra de progreso al 0% (Pagado $0 · Saldo $250).
6. Mini-cards: Facturas 0 / Facturado $0 / Por facturar $250.
7. **Verificar que el monto se auto-llenó a $250** (el saldo).
8. Selecciona concepto "Curso Basico" → ya está $250 si quitaste el auto-fill, sino mantiene.
9. Modalidad: **Contado** (default).
10. Fecha vencimiento: cualquiera futura.
11. Click "Crear factura".
12. **Verificar tabla:** nueva fila con badge **Contado** y badge **Pagada** en columna Saldo... **NO**, espera, recién creada está sin pagar, debe decir **$250.00 en amarillo** en la columna Saldo.

---

## 5) Pago parcial sobre la factura recién creada

**Pasos:**
1. Click "Registrar pago".
2. Estudiante: "Pedro Estudiante" → carga sus facturas pendientes.
3. Dropdown factura → selecciona la nueva con saldo $250.
4. Monto: **$100**.
5. Método: Efectivo.
6. Click "Registrar pago".
7. **Verificar tabla facturas:** columna Saldo de la factura ahora dice **$150.00** (amarillo).
8. **Verificar estudiante:** ir a `/estudiantes` → Pedro Estudiante debe haber pasado a:
   - **Estado académico: Matriculado** (era Pre-matriculado)
   - **Situación pago: Pago parcial** (era Pendiente matrícula)
   - Matrícula: fecha de hoy

---

## 6) Crear factura CRÉDITO con preview cronograma

**Pasos:**
1. `/cobros → Nueva factura`.
2. Estudiante: "Hernan Jurado" (Matriculado, sin facturas).
3. Si no tiene tipo de curso, edítalo primero asignándole "Curso Profesional C $350".
4. De vuelta en Nueva factura → seleccionar Hernan → card debe mostrar Curso Profesional C, precio $350, saldo $350, monto auto = $350.
5. Concepto: cualquiera.
6. Click tarjeta **Crédito**.
7. Cuotas: **6**, Frecuencia: **Mensual**, 1ra cuota: dentro de 30 días.
8. **Verificar preview cronograma:** debe mostrar 6 filas con fechas espaciadas un mes y montos de $58.33 / $58.33 / ... / $58.35 (residuo en la última).
9. Cambia frecuencia a Quincenal → fechas se recalculan a +15 días.
10. Click "Crear factura".
11. **Verificar tabla:** nueva fila con badge **Crédito**, "0/6 cuotas", saldo $350, ícono lista al final.

---

## 7) Ver cuotas y pagar una

**Pasos:**
1. Click ícono lista de la factura crédito de Hernan.
2. Diálogo "Cronograma · FAC-XXXX" abre con:
   - 3 cards arriba: Total $350 / Pagado $0 / Saldo $350.
   - Tabla de 6 cuotas: todas PENDIENTE.
3. Cierra (Esc).
4. Click "Registrar pago".
5. Estudiante: Hernan.
6. Factura: la nueva crédito.
7. **Verificar mini-card "Próxima cuota":** Cuota 1 · vence YYYY-MM-DD · botón "Usar saldo de la cuota: $58.33".
8. Click ese botón → monto se llena a $58.33.
9. Método: Efectivo. Click "Registrar pago".
10. **Verificar:** vuelve a abrir "Ver cuotas" → cuota 1 PAGADA con fecha de pago. Cuotas 2-6 PENDIENTE.
11. Factura ahora: 1/6 cuotas, saldo $291.67.

---

## 8) Pestañas estudiantes por estado pago

**Dónde:** `/cobros`, sección "Estudiantes por estado de pago"

**Verificar:**
- Pestaña **"Sin pagar"** con contador → lista solo los PENDIENTE_MATRICULA. Botón **"Facturar"** abre el form de Nueva Factura precargado con ese estudiante.
- Pestaña **"Con saldo"** con contador → lista PAGO_PARCIAL/EN_MORA/AL_DIA. Botón **"Cobrar"** abre form de Pago precargado con ese estudiante y sus facturas pendientes.
- Pestaña **"Al día"** con contador → lista PAGADO_TOTAL/SIN_DEUDA. Botón **"Ver"** muestra alert simple.

---

## 9) Columna SALDO en tabla facturas

**Verificar:** cada fila de la tabla "Facturas" tiene una columna nueva "Saldo":
- Si saldo > 0 → badge amarillo con el monto pendiente.
- Si saldo = 0 → texto verde "Pagada" con ícono ✓.

---

## 10) Catálogos en Configuración

**Dónde:** `/configuracion → Catálogos` (sidebar interno)

**Conceptos de facturación:**
1. Debe mostrar 7 filas (o más si creaste en sesión anterior).
2. Click "Nuevo concepto" → diálogo con: nombre, monto, descripción, checkbox activo.
3. Crear "Examen Teórico Repetición" $20 → aparece.
4. Editar uno existente → cambiar monto → guardar.
5. Eliminar uno → confirmación → desaparece.
6. **Crítico:** el nuevo concepto creado debe aparecer en `/cobros → Nueva factura → dropdown concepto`.

**Tipos de curso:**
1. Tabla con 3 cursos seed + categoría (badge) + horas + precio.
2. "Nuevo tipo de curso" → diálogo con: nombre, dropdown categoría (carga desde `/categorias-licencia`), duración, precio, descripción, activo.
3. Crear "Curso Profesional D $400" cat D 80h → aparece.
4. **Crítico:** el nuevo curso debe aparecer en `/estudiantes → Nuevo` → Dropdown tipo de curso.

---

## 11) Sincronización situacion_pago

**Pasos:**
1. En `/cobros`, click botón "Sincronizar" (gris arriba).
2. Espera unos segundos → alert "Sincronización OK · X de N estudiantes actualizado(s)".
3. Si todos los datos están consistentes debe decir **0 actualizados**.
4. Para probar drift, en consola psql:
   ```sql
   UPDATE estudiantes_schema.estudiantes SET situacion_pago='SIN_DEUDA' WHERE id=2;
   ```
5. Click "Sincronizar" → debe decir 1 actualizado y Juan vuelve a PAGO_PARCIAL.

---

## 12) Auto-transición CURSANDO al crear asignación

**Pasos:**
1. Asignar tipo de curso a Hernan si aún no lo tiene.
2. En BD verificar que Hernan está MATRICULADO.
3. `/asignaciones → Nueva asignación`. Selecciona instructor, vehículo, Hernan, fecha futura, horas.
4. Crea.
5. Verificar en BD:
   ```sql
   SELECT id, nombre, estado FROM estudiantes_schema.estudiantes WHERE id=4;
   ```
   Debe estar `CURSANDO`.

---

## Si encuentras bugs

Documenta cada uno en formato:
- **Vista**: dónde
- **Pasos para reproducir**
- **Esperado** vs **Observado**
- **Captura/consola** (F12)

Pásamelo en la próxima sesión.

---

## Después del testing

Una vez todas las pruebas pasen (o se hayan corregido los bugs):

1. Crear el PR desde: https://github.com/Kynsofttita-com/proyecto-titulacion-udla/pull/new/feature/sprint-9-1-perfil-usuarios-estados
2. Título sugerido: **Sprint 9 - Cobros con crédito + estados extendidos estudiante + catálogos**
3. Body: copia el listado de "Funcionalidades implementadas Sprint 9" del `project_state.md` (memoria de Claude).
4. Merge a `main` y arrancar branch para Sprint 10 (Reportes).
