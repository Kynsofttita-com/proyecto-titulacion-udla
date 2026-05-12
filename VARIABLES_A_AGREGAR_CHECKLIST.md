# ✅ Variables a Agregar - Checklist para Sprint 5

## 📋 Resumen Ejecutivo

**Total de variables**: 15  
**Heredadas de Sprint 3-4**: 5  
**NUEVAS a agregar**: 10  
**Archivos Postman**: 
- Original: `Sprint_3_4_Auth_Notificaciones.postman_collection.json`
- Nueva versión: `POSTMAN_Sprint5_Collection_v2.json` ✅ (ya tiene todo)

---

## ✨ Variables NUEVAS que Debes Agregar

### 📋 Copiar exactamente estos nombres y valores:

```json
{
  "variables": [
    {
      "key": "estudianteId",
      "value": "1",
      "type": "string",
      "description": "ID del estudiante (se auto-actualiza al crear)"
    },
    {
      "key": "instructorId",
      "value": "1",
      "type": "string",
      "description": "ID del instructor (se auto-actualiza al crear)"
    },
    {
      "key": "cedula",
      "value": "1234567890",
      "type": "string",
      "description": "Cédula del estudiante (DEBE SER ÚNICA - cambiar antes de usar)"
    },
    {
      "key": "email",
      "value": "estudiante@escuela.local",
      "type": "string",
      "description": "Email del estudiante (DEBE SER ÚNICO - cambiar antes de usar)"
    },
    {
      "key": "nombreCompleto",
      "value": "Juan Pérez García",
      "type": "string",
      "description": "Nombre completo del estudiante (personalizar)"
    },
    {
      "key": "cedulaInstructor",
      "value": "9876543210",
      "type": "string",
      "description": "Cédula del instructor (DEBE SER ÚNICA - cambiar antes de usar)"
    },
    {
      "key": "emailInstructor",
      "value": "instructor@escuela.local",
      "type": "string",
      "description": "Email del instructor (DEBE SER ÚNICO - cambiar antes de usar)"
    },
    {
      "key": "licencia",
      "value": "1A123456",
      "type": "string",
      "description": "Número de licencia (DEBE SER ÚNICA - cambiar antes de usar)"
    },
    {
      "key": "nombreInstructor",
      "value": "Carlos Rodríguez López",
      "type": "string",
      "description": "Nombre completo del instructor (personalizar)"
    },
    {
      "key": "numeroTelefono",
      "value": "+593912345678",
      "type": "string",
      "description": "Teléfono de contacto (formato +593...)"
    }
  ]
}
```

---

## 🎯 Guía Rápida: Dónde Agregar en Postman

### Opción A: En la Colección (Más rápido)

1. Abre Postman
2. **Collections** (lado izquierdo)
3. Haz click derecho en **"Sprint 5 — MS-Estudiantes & MS-Instructores CRUD"**
4. Click en **"Edit"**
5. Pestaña **"Variables"**
6. **Agrega cada fila** con los datos arriba:
   ```
   Key                 | Initial Value
   ─────────────────────────────────────
   estudianteId        | 1
   instructorId        | 1
   cedula              | 1234567890
   email               | estudiante@escuela.local
   nombreCompleto      | Juan Pérez García
   cedulaInstructor    | 9876543210
   emailInstructor     | instructor@escuela.local
   licencia            | 1A123456
   nombreInstructor    | Carlos Rodríguez López
   numeroTelefono      | +593912345678
   ```
7. Click **"Save"**

### Opción B: Crear Environment (Recomendado)

1. **Environments** (lado izquierdo) → **Create New**
2. Nombre: `Sprint5-Local`
3. Agregar variables (ver tabla arriba)
4. Click **"Save"**
5. En la parte superior de Postman, en el dropdown que dice **"No Environment"**, selecciona **"Sprint5-Local"**

### Opción C: Usar Colección v2 (Más fácil)

Ya viene preconfigurada:
1. Importar: `POSTMAN_Sprint5_Collection_v2.json`
2. Solo cambiar valores según tabla abajo
3. ¡Listo! Ya tiene todas las variables

---

## 📊 Tabla de Valores Recomendados (Copia y Pega)

### Para Primer Run

| Variable | Valor Actual | Cambiar a | Notas |
|---|---|---|---|
| cedula | 1234567890 | 1234567891 | Incrementa 1 dígito |
| email | estudiante@escuela.local | estudiante1@escuela.local | Agrega número |
| nombreCompleto | Juan Pérez García | Tu Nombre Aquí | Personalizar |
| cedulaInstructor | 9876543210 | 9876543211 | Incrementa 1 dígito |
| emailInstructor | instructor@escuela.local | instructor1@escuela.local | Agrega número |
| licencia | 1A123456 | 1A123457 | Incrementa número |
| nombreInstructor | Carlos Rodríguez López | Otro Nombre Aquí | Personalizar |

### Para Segundo Run (Probar con diferentes datos)

| Variable | Cambiar a | Notas |
|---|---|---|
| cedula | 1234567892 | Incrementa 2 dígitos |
| email | estudiante2@escuela.local | Agrega número 2 |
| cedulaInstructor | 9876543212 | Incrementa 2 dígitos |
| emailInstructor | instructor2@escuela.local | Agrega número 2 |
| licencia | 1A123458 | Incrementa número |

### Para Tercer Run

| Variable | Cambiar a | Notas |
|---|---|---|
| cedula | 1234567893 | Incrementa 3 dígitos |
| email | estudiante3@escuela.local | Agrega número 3 |
| cedulaInstructor | 9876543213 | Incrementa 3 dígitos |
| emailInstructor | instructor3@escuela.local | Agrega número 3 |
| licencia | 1A123459 | Incrementa número |

---

## ⚠️ Variables que NUNCA cambiar

Estas heredadas de Sprint 3-4 JAMÁS toques:

```json
{
  "baseUrl": "http://localhost:8080",
  "adminEmail": "admin@escuela.local",
  "adminPassword": "Admin123!",
  "accessToken": "",              ← Se llena automáticamente
  "refreshToken": ""              ← Se llena automáticamente
}
```

---

## 🔄 Flujo Automático

| Paso | Ejecuta | Qué sucede |
|---|---|---|
| 1 | POST /auth/login | `accessToken` ← **se llena automáticamente** |
| 2 | POST /estudiantes | `estudianteId` ← **se llena automáticamente** |
| 3 | POST /instructores | `instructorId` ← **se llena automáticamente** |

**Resultado**: Después de estos 3 endpoints, tus variables están 100% actualizadas.

---

## 🚀 Paso a Paso para Principiante

### Si NUNCA usaste Postman:

```
1. Abre Postman
   File → Import → POSTMAN_Sprint5_Collection_v2.json

2. Verifica que la colección se importó
   Deberías ver en el lado izquierdo:
   
   📁 Sprint 5 — MS-Estudiantes & MS-Instructores CRUD
     📂 1 AUTENTICACIÓN
     📂 2 MS-ESTUDIANTES CRUD
     📂 3 MS-INSTRUCTORES CRUD
     📂 4 ERROR HANDLING

3. Haz click en el nombre de la colección
   Pestaña "Variables"
   Verifica que todas las variables están aquí:
   
   ✅ baseUrl
   ✅ adminEmail
   ✅ adminPassword
   ✅ accessToken
   ✅ refreshToken
   ✅ estudianteId
   ✅ instructorId
   ✅ cedula
   ✅ email
   ✅ nombreCompleto
   ✅ cedulaInstructor
   ✅ emailInstructor
   ✅ licencia
   ✅ nombreInstructor
   ✅ numeroTelefono

4. Cambia los valores (ver tabla arriba)
   cedula:          1234567891
   email:           estudiante1@escuela.local
   Etc...

5. Click "Save"

6. Colección → 1 AUTENTICACIÓN → Login
   Click "Send"
   Deberías ver: Status 200 ✅

7. Colección → 2 MS-ESTUDIANTES CRUD → POST /estudiantes
   Click "Send"
   Deberías ver: Status 201 Created ✅

8. ¡Listo! Tu primeiro estudiante fue creado.
   El ID se guardó automáticamente en {{estudianteId}}

9. Continúa con los demás endpoints...
```

---

## ✅ Checklist Completitud

Antes de llevar a presentar:

- [ ] ✅ Importé `POSTMAN_Sprint5_Collection_v2.json`
- [ ] ✅ Veo todas las 15 variables en la colección
- [ ] ✅ Cambié `cedula` a valor único
- [ ] ✅ Cambié `email` a valor único
- [ ] ✅ Cambié `cedulaInstructor` a valor único
- [ ] ✅ Cambié `emailInstructor` a valor único
- [ ] ✅ Cambié `licencia` a valor único
- [ ] ✅ Personalicé nombres (nombreCompleto, nombreInstructor)
- [ ] ✅ Ejecuté Login y obtuve token
- [ ] ✅ Ejecuté POST /estudiantes y funcionó
- [ ] ✅ Ejecuté POST /instructores y funcionó
- [ ] ✅ Ejecuté GET endpoints y vi datos
- [ ] ✅ Ejecuté ERROR HANDLING y vieron excepciones
- [ ] ✅ Tests pasaron en todos los endpoints

---

## 📝 Resumen Final

**¿Qué archivo debo usar?**
→ `POSTMAN_Sprint5_Collection_v2.json` (tiene todo configurado)

**¿Cuántas variables nuevas?**
→ 10 variables nuevas (más las 5 heredadas = 15 total)

**¿Qué tengo que cambiar?**
→ Solo 7 variables (cédulas, emails, licencia) para hacerlas únicas
→ 2 nombres para personalizar
→ El resto quedan igual

**¿Se auto-llenan algunas?**
→ SÍ: `accessToken`, `refreshToken`, `estudianteId`, `instructorId` se llenan automáticamente

**¿Dónd está la guía completa?**
→ `GUIA_VARIABLES_POSTMAN_SPRINT5.md` (20+ ejemplos)

**¿Y la referencia rápida?**
→ `RESUMEN_VARIABLES_SPRINT5.txt` (tabla de copiar-pegar)

---

**Documento**: VARIABLES_A_AGREGAR_CHECKLIST.md  
**Versión**: 1.0  
**Fecha**: 12 Mayo 2026  
**Archivos asociados**: 
- POSTMAN_Sprint5_Collection_v2.json
- GUIA_VARIABLES_POSTMAN_SPRINT5.md
- RESUMEN_VARIABLES_SPRINT5.txt

**Status**: ✅ Listo para usar - Solo importa la colección v2 y comienza
