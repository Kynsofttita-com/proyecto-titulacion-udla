# 📋 Guía de Variables de Entorno - Postman Sprint 5

**Archivo**: `POSTMAN_Sprint5_Collection_v2.json`  
**Fecha**: 12 Mayo 2026  
**Compatibilidad**: Sprint 3-4 + Sprint 5

---

## 🎯 Variables Heredadas de Sprint 3-4

Estas variables ya existen en la colección anterior y se reutilizan:

| Variable | Valor Defecto | Tipo | Descripción |
|---|---|---|---|
| `baseUrl` | `http://localhost:8080` | string | URL base del API Gateway |
| `adminEmail` | `admin@escuela.local` | string | Email del admin para login |
| `adminPassword` | `Admin123!` | string | Password del admin |
| `accessToken` | `` (vacío) | string | JWT token obtenido tras login (se auto-llena) |
| `refreshToken` | `` (vacío) | string | Refresh token para renovar JWT (se auto-llena) |

**Nota**: `accessToken` y `refreshToken` se **llenan automáticamente** después de ejecutar el endpoint de login. No necesitas ingresar valores manualmente.

---

## ✨ Variables Nuevas para Sprint 5

Estas son **nuevas variables que DEBES AGREGAR** para probar MS-Estudiantes y MS-Instructores:

### 1. **Identificadores (IDs)**

| Variable | Valor Defecto | Tipo | Descripción | Dónde se usa |
|---|---|---|---|---|
| `estudianteId` | `1` | string | ID del estudiante para operaciones GET, PUT, DELETE | GET /estudiantes/{id}, PUT, DELETE |
| `instructorId` | `1` | string | ID del instructor para operaciones GET, PUT, DELETE | GET /instructores/{id}, PUT, DELETE |

**Importante**: Después de crear un estudiante o instructor, el ID se guarda automáticamente en estas variables, así no necesitas cambiarlo manualmente.

---

### 2. **Datos de Estudiante**

| Variable | Valor Defecto | Tipo | Descripción | Validación |
|---|---|---|---|---|
| `cedula` | `1234567890` | string | Cédula del estudiante | Debe ser único en BD |
| `email` | `estudiante@escuela.local` | string | Email del estudiante | Debe ser único en BD |
| `nombreCompleto` | `Juan Pérez García` | string | Nombre completo | Requerido |
| `numeroTelefono` | `+593912345678` | string | Teléfono de contacto | Formato: +593... |

**Cambios recomendados antes de usar**:
- `cedula`: Cambiar a un número único (ej: 1234567891, 1234567892, etc.)
- `email`: Cambiar a un email único (ej: estudiante1@escuela.local, estudiante2@escuela.local)
- `nombreCompleto`: Personalizar con nombres reales
- `numeroTelefono`: Usar un número válido de Ecuador

---

### 3. **Datos de Instructor**

| Variable | Valor Defecto | Tipo | Descripción | Validación |
|---|---|---|---|---|
| `cedulaInstructor` | `9876543210` | string | Cédula del instructor | Debe ser único en BD |
| `emailInstructor` | `instructor@escuela.local` | string | Email del instructor | Debe ser único en BD |
| `licencia` | `1A123456` | string | Número de licencia de conducir | Debe ser único en BD |
| `nombreInstructor` | `Carlos Rodríguez López` | string | Nombre completo del instructor | Requerido |

**Cambios recomendados antes de usar**:
- `cedulaInstructor`: Cambiar a un número único diferente de `cedula`
- `emailInstructor`: Cambiar a un email único diferente de `email`
- `licencia`: Cambiar a un número de licencia único (ej: 1A123457, 1A123458)
- `nombreInstructor`: Personalizar con nombres reales

---

## 🔧 Cómo Configurar las Variables

### Opción 1: En Postman UI (Más fácil)

1. **Importar colección**:
   - Abre Postman
   - File → Import → Selecciona `POSTMAN_Sprint5_Collection_v2.json`

2. **Editar variables**:
   - En Postman, haz click en el nombre de la colección
   - Pestaña "Variables"
   - Edita los valores por defecto en la columna "Initial value"
   - Ejemplo de cambios:
     ```
     cedula:          1234567891
     email:           estudiante1@escuela.local
     cedulaInstructor: 9876543211
     emailInstructor: instructor1@escuela.local
     licencia:        1A123457
     ```

3. **Guardar**:
   - Click en "Save"

### Opción 2: Crear un Environment (Recomendado)

1. **Crear nuevo environment**:
   - Postman → Environments → Create New
   - Nombre: `Sprint5-Local`
   - Variables:

   ```json
   {
     "baseUrl": "http://localhost:8080",
     "adminEmail": "admin@escuela.local",
     "adminPassword": "Admin123!",
     "accessToken": "",
     "refreshToken": "",
     "estudianteId": "1",
     "instructorId": "1",
     "cedula": "1234567891",
     "email": "estudiante1@escuela.local",
     "nombreCompleto": "Juan Pérez García",
     "cedulaInstructor": "9876543211",
     "emailInstructor": "instructor1@escuela.local",
     "licencia": "1A123457",
     "nombreInstructor": "Carlos Rodríguez López",
     "numeroTelefono": "+593912345678"
   }
   ```

2. **Seleccionar environment**:
   - En la parte superior derecha de Postman
   - Dropdown que dice "No Environment"
   - Selecciona "Sprint5-Local"

3. **Cambiar valores según necesites**:
   - Editar environment
   - Cambiar cédulas, emails, etc. para hacerlas únicas

---

## 🚀 Flujo de Ejecución Recomendado

### Paso 1: Login (Obtener Tokens)
```
Ejecutar: 1 AUTENTICACIÓN → Login
↓
Se obtiene accessToken y refreshToken (se guardan automáticamente)
```

### Paso 2: Crear Estudiante
```
Ejecutar: 2 MS-ESTUDIANTES CRUD → POST /estudiantes (Crear)
↓
Se crea estudiante con cedula = {{cedula}}
↓
Se guarda su ID en estudianteId automáticamente
```

### Paso 3: Operaciones CRUD Estudiante
```
Ejecutar en orden:
  GET /estudiantes (Listar todos)
  GET /estudiantes/{id} (Detalle - usa {{estudianteId}})
  PUT /estudiantes/{id} (Actualizar)
  DELETE /estudiantes/{id} (Eliminar - soft delete)
```

### Paso 4-6: Repetir con Instructor
```
POST /instructores (Crear)
  ↓ guarda ID en {{instructorId}}
  ↓
GET, PUT, DELETE igual que estudiantes
```

### Paso 7: Probar Error Handling
```
Ejecutar: 4 ERROR HANDLING
  - Cédula duplicada (409)
  - Email duplicado (409)
  - Sin autenticación (401)
  - No encontrado (404)
  - Licencia duplicada (409)
```

---

## 📝 Ejemplos Prácticos

### Ejemplo 1: Cambiar Cédula

**Antes de empezar**:
1. Abre Postman
2. Variables de colección
3. Localiza `cedula`: `1234567890`
4. Cámbialo a: `1234567891`
5. Click "Save"

**Ahora cuando ejecutes POST /estudiantes**:
- Se creará estudiante con cedula = 1234567891 ✅

### Ejemplo 2: Cambiar Email

1. Variables → `email`: `estudiante@escuela.local`
2. Cámbialo a: `juan@escuela.local`
3. Save
4. Ejecutar POST /estudiantes → crea con email = juan@escuela.local ✅

### Ejemplo 3: Usar diferentes estudiantes

Para probar múltiples estudiantes sin conflictos:

| Run | Cambiar | Valor |
|---|---|---|
| 1 | cedula | 1234567891 |
| 1 | email | estudiante1@escuela.local |
| Run | POST /estudiantes | ✅ Creado ID=1 |
| ---|---|---|
| 2 | cedula | 1234567892 |
| 2 | email | estudiante2@escuela.local |
| Run | POST /estudiantes | ✅ Creado ID=2 |

---

## ⚠️ Problemas Comunes y Soluciones

### ❌ "Cédula duplicada (409)"
**Causa**: Ya existe un estudiante con esa cédula
**Solución**: Cambiar `cedula` a un número diferente (ej: 1234567893)

### ❌ "Email duplicado (409)"
**Causa**: Ya existe un estudiante con ese email
**Solución**: Cambiar `email` a uno diferente (ej: nuevo@escuela.local)

### ❌ "401 Unauthorized"
**Causa**: El token JWT expiró o no está configurado
**Solución**: Volver a ejecutar "Login (obtener JWT token)" en la sección AUTENTICACIÓN

### ❌ "404 Not Found"
**Causa**: El estudiante/instructor con ese ID no existe
**Solución**: Primero crear uno (POST) o cambiar el ID a uno existente

### ❌ "Cannot read variable baseUrl"
**Causa**: Variables no se han inicializado en Postman
**Solución**: 
1. Asegurate de que la colección está importada
2. En Postman, tienes seleccionado el Environment correcto
3. O edita manualmente la variable en la colección

---

## 🎯 Checklist - Variables Configuradas

Antes de empezar pruebas, verifica:

- [ ] ✅ `baseUrl` = `http://localhost:8080`
- [ ] ✅ `adminEmail` = `admin@escuela.local`
- [ ] ✅ `adminPassword` = `Admin123!`
- [ ] ✅ `cedula` = cambio a número único (ej: 1234567891)
- [ ] ✅ `email` = cambio a email único (ej: estudiante1@escuela.local)
- [ ] ✅ `nombreCompleto` = personalizado
- [ ] ✅ `cedulaInstructor` = cambio a número único (ej: 9876543211)
- [ ] ✅ `emailInstructor` = cambio a email único (ej: instructor1@escuela.local)
- [ ] ✅ `licencia` = cambio a licencia única (ej: 1A123457)
- [ ] ✅ `nombreInstructor` = personalizado
- [ ] ✅ `numeroTelefono` = teléfono válido

---

## 📚 Variables Adicionales Opcionales

Si quieres agregar más validaciones o campos:

```json
{
  "provincia": "Pichincha",
  "ciudad": "Quito",
  "domicilio": "Avenida Amazonas 2000",
  "especialidad": "Conducción Manual",
  "horasEntrenamiento": "40",
  "calificacion": "A"
}
```

Estos se pueden hardcodear en los request bodies o agregar como variables.

---

## 🔄 Ciclo Completo Recomendado

```
1. Cambiar variables (cédula, email, licencia)
   ↓
2. Ejecutar "Login" → obtiene token
   ↓
3. Ejecutar "POST /estudiantes" → crea estudiante
   ↓
4. Ejecutar "GET /estudiantes/{id}" → verifica creación
   ↓
5. Ejecutar "PUT /estudiantes/{id}" → actualiza
   ↓
6. Ejecutar "DELETE /estudiantes/{id}" → elimina
   ↓
7. Repetir 1-6 con instructor
   ↓
8. Ejecutar "Error Handling" → valida excepciones
   ↓
✅ Sprint 5 validado completamente
```

---

**Versión**: 1.0  
**Actualizado**: 12 Mayo 2026  
**Próxima actualización**: Después de completar MS-Vehículos, Asignaciones, Cobros

¿Tienes dudas? Consulta POSTMAN_Sprint5_Collection_v2.json directamente - tiene descriptions en cada endpoint.
