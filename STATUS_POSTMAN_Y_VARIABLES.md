# 📊 Status Postman Sprint 5 + Variables a Guardar

**Fecha**: 12 Mayo 2026  
**Estado**: Autenticación ✅ | CRUD de Estudiantes/Instructores 🔧  

---

## ✅ LO QUE FUNCIONA (Verificado con CURL)

### Autenticación (100% Funcional)
```bash
✅ POST /auth/login           → Status 200 OK
✅ GET /auth/me               → Status 200 OK  
✅ POST /auth/refresh         → Status 200 OK
✅ POST /auth/logout          → Status 204 No Content
```

**Credenciales válidas (verificadas)**:
```json
{
  "email": "admin@escuela.local",
  "password": "Admin123!"
}
```

**Respuesta de Login**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "accessTokenExpiresInSeconds": 900,
  "refreshTokenExpiresInSeconds": 604800,
  "user": {
    "id": 1,
    "email": "admin@escuela.local",
    "nombre": "Administrador",
    "apellido": "del Sistema",
    "roles": ["ADMIN"]
  }
}
```

---

## 🔧 LO QUE NO FUNCIONA (Requiere Fix)

### CRUD de Estudiantes, Instructores, Vehículos
```bash
❌ GET /estudiantes           → Status 404 Not Found
❌ POST /estudiantes          → Status 404 Not Found
❌ GET /instructores          → Status 404 Not Found
❌ POST /instructores         → Status 404 Not Found
❌ GET /vehiculos             → Status 404 Not Found
```

**Causa**: Los microservicios están registrados en Eureka (UP) pero sus controladores no están inicializados.

**Evidencia**:
```bash
# Los servicios están corriendo:
$ curl http://localhost:8761/eureka/apps
→ MS-ESTUDIANTES: UP
→ MS-INSTRUCTORES: UP
→ MS-VEHICULOS: UP

# Pero los endpoints no existen:
$ curl http://localhost:8082/estudiantes
→ 404 Not Found (Endpoint no existe)
```

---

## 📋 VARIABLES QUE DEBES GUARDAR EN POSTMAN

### Opción 1: En la Colección (Recomendado para inicio)

1. En Postman, abre la colección `POSTMAN_Sprint5_Funcional.json`
2. Haz click en el nombre → Pestaña "Variables"
3. Estos valores ya están ahí (heredados de Sprint 3-4):

```
baseUrl              = http://localhost:8080
adminEmail           = admin@escuela.local
adminPassword        = Admin123!
accessToken          = (vacío - se auto-llena tras login)
refreshToken         = (vacío - se auto-llena tras login)
```

4. Puedes AGREGAR estos para futuros usos:

```
estudianteId         = 1
instructorId         = 1
cedula               = 1234567890
email                = estudiante@escuela.local
nombreCompleto       = Juan Pérez García
numeroTelefono       = +593912345678
```

### Opción 2: Crear Environment (Más profesional)

1. **Environments** (lado izquierdo) → **Create New**
2. Nombre: `Sprint5-Local`
3. Agrega estas variables:

```json
{
  "baseUrl": "http://localhost:8080",
  "adminEmail": "admin@escuela.local",
  "adminPassword": "Admin123!",
  "accessToken": "",
  "refreshToken": "",
  "estudianteId": "1",
  "instructorId": "1",
  "cedula": "1234567890",
  "email": "estudiante@escuela.local",
  "nombreCompleto": "Juan Pérez García",
  "numeroTelefono": "+593912345678"
}
```

4. **Save**
5. En Postman, dropdown arriba selecciona este environment

---

## 🎯 Variables Esenciales vs Opcionales

### ✅ ESENCIALES (Necesarias para autenticación)

| Variable | Valor | Por qué |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | Punto de entrada |
| `adminEmail` | `admin@escuela.local` | Credencial login |
| `adminPassword` | `Admin123!` | Credencial login |
| `accessToken` | *(se auto-llena)* | Para autorizar requests |
| `refreshToken` | *(se auto-llena)* | Para renovar token |

### 📝 OPCIONALES (Para cuando funcionen CRUD)

| Variable | Valor | Usar en |
|---|---|---|
| `estudianteId` | `1` | GET /estudiantes/{id} |
| `instructorId` | `1` | GET /instructores/{id} |
| `cedula` | `1234567890` | POST /estudiantes (cambiar) |
| `email` | `estudiante@escuela.local` | POST /estudiantes (cambiar) |
| `nombreCompleto` | `Juan Pérez García` | POST /estudiantes |
| `numeroTelefono` | `+593912345678` | POST /estudiantes |

---

## 🔄 Flujo Actual Funcional

```
1. Abrir Postman
   ↓
2. Importar POSTMAN_Sprint5_Funcional.json
   ↓
3. Ejecutar "Login" (POST /auth/login)
   ✅ Obtiene accessToken y refreshToken
   ↓
4. Ejecutar "GET /auth/me"
   ✅ Verifica que el token es válido
   ↓
5. Ejecutar "Refresh Token"
   ✅ Obtiene nuevo accessToken
   ↓
6. Ejecutar "Logout"
   ✅ Invalida el token

¡ÉXITO! Autenticación completamente funcional
```

---

## ❌ Por qué Falla el POST de Estudiantes

**Diagrama del problema**:

```
POST /estudiantes
    ↓
CURL Error 404 → El controlador no responde
    ↓
Eureka Status → MS-ESTUDIANTES: UP (servicio corriendo)
    ↓
CONCLUSIÓN → Servicio corre, pero controlador no inicializado
             (falta @RestController o @RequestMapping)
```

**Verificación manual con CURL**:

```bash
# ✅ FUNCIONA (Auth)
$ curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@escuela.local","password":"Admin123!"}'

RESULTADO: Status 200 OK + tokens ✅

# ❌ NO FUNCIONA (CRUD)
$ curl -X GET http://localhost:8082/estudiantes \
  -H "Authorization: Bearer $TOKEN"

RESULTADO: Status 404 Not Found ❌
```

---

## 🚀 Qué Hacer Ahora

### Opción A: Usar Solo Autenticación (Ahora mismo)
1. Importa `POSTMAN_Sprint5_Funcional.json`
2. Usa las 4 variables esenciales
3. Prueba Login, Refresh, Logout
4. ✅ Todo funciona

### Opción B: Activar CRUD (Requiere fix backend)
1. Verificar logs de ms-estudiantes
2. Confirmar @RestController en EstudianteController
3. Reiniciar servicios
4. Luego agregar variables opcionales

### Opción C: Para la Presentación del Profesor
1. Mostrar autenticación (✅ funciona)
2. Explicar que CRUD está en desarrollo
3. Demostrar con CURL cómo se prueban los endpoints
4. Mostrar logs de servicios en Eureka

---

## 📊 Tabla Comparativa

| Feature | Status | Postman | CURL |
|---|---|---|---|
| Login | ✅ 100% | Funciona | `curl POST /auth/login` |
| Get Token | ✅ 100% | Funciona | `curl GET /auth/me` |
| Refresh | ✅ 100% | Funciona | `curl POST /auth/refresh` |
| Logout | ✅ 100% | Funciona | `curl POST /auth/logout` |
| GET /estudiantes | ❌ 0% | 404 | `curl GET /estudiantes` |
| POST /estudiantes | ❌ 0% | 404 | `curl POST /estudiantes` |
| GET /instructores | ❌ 0% | 404 | `curl GET /instructores` |
| POST /instructores | ❌ 0% | 404 | `curl POST /instructores` |

---

## 🎯 Checklist: Variables a Guardar

Antes de empezar en Postman:

- [ ] ✅ Importé `POSTMAN_Sprint5_Funcional.json`
- [ ] ✅ Veo la variable `baseUrl` = `http://localhost:8080`
- [ ] ✅ Veo la variable `adminEmail` = `admin@escuela.local`
- [ ] ✅ Veo la variable `adminPassword` = `Admin123!`
- [ ] ✅ Ejecuté "Login" sin errores
- [ ] ✅ Vi que `accessToken` se llenó automáticamente
- [ ] ✅ Ejecuté "GET /auth/me" y obtuve datos del admin
- [ ] ✅ Ejecuté "Refresh Token" sin errores
- [ ] ✅ Ejecuté "Logout" y obtuve 204 No Content

---

## 📝 Comandos CURL para Testing

Si prefieres usar CURL directamente:

```bash
# 1. LOGIN (obtener tokens)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@escuela.local","password":"Admin123!"}'

# SALIDA (copiar accessToken)
# "accessToken":"eyJhbGc..."

# 2. USAR TOKEN EN SIGUIENTE REQUEST
TOKEN="eyJhbGc..."
curl -X GET http://localhost:8080/auth/me \
  -H "Authorization: Bearer $TOKEN"

# 3. VERIFICAR QUE ESTUDIANTES FUNCIONA
curl -X GET http://localhost:8082/estudiantes \
  -H "Authorization: Bearer $TOKEN"

# RESULTADO: 404 Not Found (aún en desarrollo)
```

---

## 💾 Resumen Final: Variables a Guardar

**Para Postman Collections → Variables o Environment**:

```
COPIAR ESTOS VALORES:

baseUrl              = http://localhost:8080
adminEmail           = admin@escuela.local
adminPassword        = Admin123!
accessToken          = (se auto-llena tras login)
refreshToken         = (se auto-llena tras login)

AGREGAR ESTOS (OPCIONAL):
estudianteId         = 1
instructorId         = 1
cedula               = 1234567890
email                = estudiante@escuela.local
nombreCompleto       = Juan Pérez García
numeroTelefono       = +593912345678
```

---

**Archivo**: POSTMAN_Sprint5_Funcional.json  
**Variables**: 5 esenciales + 6 opcionales = 11 total  
**Status**: ✅ Autenticación lista | 🔧 CRUD en desarrollo

¿Necesitas ayuda para configurar variables en Postman?
