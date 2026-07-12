# 🔐 CREDENCIALES COMPLETAS — Sistema de Gestión Escuelas de Conducción

> ⚠️ **IMPORTANTE**: Estas credenciales son SOLO para desarrollo local. Cambiar en producción.

---

## 🎯 ACCESO RÁPIDO

### Sistema Principal (Frontend)
```
URL:  http://localhost:5173
Email: admin@escuela.local
Pass: Admin123!
```

---

## 📱 FRONTEND — Vue.js 3

### Credencial Admin (Administrador del Sistema)

```
Email:     admin@escuela.local
Contraseña: Admin123!
Rol:       ADMIN

Qué puedes hacer:
  ✅ Ver todos los estudiantes
  ✅ Gestionar instructores
  ✅ Controlar vehículos
  ✅ Ver reportes financieros
  ✅ Asignar clases
  ✅ Gestionar cobros
  ✅ Ver auditoría completa
```

**Cómo acceder:**
1. Abre: `http://localhost:5173`
2. Ingresa email: `admin@escuela.local`
3. Ingresa contraseña: `Admin123!`
4. Click en "Iniciar Sesión"
5. ✅ Acceso al dashboard completo

---

## 🏗️ SERVICIOS INTERNOS

### 1️⃣ EUREKA — Service Discovery

```
URL: http://localhost:8761
Acceso: Público (sin autenticación)

Qué ves:
  ✅ 9 microservicios registrados
  ✅ Status UP/DOWN de cada servicio
  ✅ Instancias disponibles
  ✅ Información de replicas
```

**Cómo acceder:**
1. Abre: `http://localhost:8761`
2. Verás lista de servicios:
   - API-GATEWAY
   - MS-AUTH
   - MS-ESTUDIANTES
   - MS-INSTRUCTORES
   - MS-VEHICULOS
   - MS-ASIGNACIONES
   - MS-COBROS
   - MS-NOTIFICACIONES
   - MS-REPORTES

---

### 2️⃣ RABBITMQ — Message Queue

```
URL: http://localhost:15672
Usuario: guest
Contraseña: guest
Acceso: Público

Qué ves:
  ✅ 32 Queues (8 servicios × 4 queues c/u)
  ✅ 8 Exchanges (topic exchanges)
  ✅ Dead Letter Queues (para reintentos)
  ✅ Mensajes en cola
  ✅ Consumers activos
```

**Cómo acceder:**
1. Abre: `http://localhost:15672`
2. Click en "Login"
3. Usuario: `guest`
4. Contraseña: `guest`
5. Click "Login"

**Secciones útiles:**
- **Queues**: Ver colas de cada microservicio
- **Exchanges**: Ver canales de publicación
- **Admin**: Gestión de usuarios y configuración
- **Connections**: Ver conexiones activas

---

### 3️⃣ MINIO — Object Storage (S3)

```
URL: http://localhost:9001
Usuario: minioadmin
Contraseña: minioadmin
Acceso: Protegido por contraseña

Qué ves:
  ✅ Buckets creados
  ✅ Archivos subidos
  ✅ Gestión de permisos
  ✅ Estadísticas de storage
```

**Cómo acceder:**
1. Abre: `http://localhost:9001`
2. Usuario: `minioadmin`
3. Contraseña: `minioadmin`
4. Click "Login"

**Casos de uso:**
- Almacenamiento de documentos de estudiantes
- PDFs de reportes
- Fotos de carnet
- Archivos de auditoría

---

### 4️⃣ ADMINER — Database UI

```
URL: http://localhost:8888
Servidor: PostgreSQL
Host: postgres
Puerto: 5432
Usuario: escuela_user
Contraseña: changeme
Base de datos: escuela_db
```

**Cómo acceder:**
1. Abre: `http://localhost:8888`
2. En "Motor de base de datos": Selecciona `PostgreSQL`
3. Servidor: `postgres`
4. Usuario: `escuela_user`
5. Contraseña: `changeme`
6. Base de datos: `escuela_db` (dejar en blanco inicialmente)
7. Click "Login"

**Qué puedes hacer:**
- ✅ Ver estructura de tablas
- ✅ Ejecutar queries SQL
- ✅ Ver datos directamente
- ✅ Crear backups
- ✅ Editar registros manualmente

---

## 🔑 CREDENCIALES DE BASE DE DATOS (PostgreSQL)

```
Host:       localhost
Port:       5432
Database:   escuela_db
Username:   escuela_user
Password:   changeme

Schemas:
  ✅ auth_schema
  ✅ estudiantes_schema
  ✅ instructores_schema
  ✅ vehiculos_schema
  ✅ asignaciones_schema
  ✅ cobros_schema
  ✅ notificaciones_schema
  ✅ reportes_schema
  ✅ shared_schema

Total: 49 tablas
```

**Conexión desde tu cliente SQL favorito:**
```bash
psql -h localhost -U escuela_user -d escuela_db
# Contraseña: changeme
```

---

## 🌐 API GATEWAY — Endpoints

### Base URL
```
http://localhost:8080
```

### Autenticación
Todos los endpoints (excepto login) requieren JWT:
```
Authorization: Bearer <access_token>
```

### Endpoints Públicos

```
POST /auth/login
POST /auth/refresh
POST /auth/forgot-password
```

### Endpoints Protegidos (Requieren JWT)

```
GET    /estudiantes
POST   /estudiantes
GET    /estudiantes/{id}
PUT    /estudiantes/{id}

GET    /instructores
POST   /instructores
GET    /instructores/{id}

GET    /vehiculos
POST   /vehiculos
GET    /vehiculos/{id}

GET    /asignaciones
POST   /asignaciones
GET    /asignaciones/{id}

GET    /cobros
POST   /cobros
GET    /reportes

GET    /notificaciones
POST   /notificaciones
```

### Cómo obtener JWT

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@escuela.local",
    "password": "Admin123!"
  }'
```

Respuesta:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "accessTokenExpiresInSeconds": 7200,
  "user": {
    "id": 1,
    "email": "admin@escuela.local",
    "nombre": "Administrador",
    "roles": ["ADMIN"]
  }
}
```

### Usar JWT en requests

```bash
curl http://localhost:8080/estudiantes \
  -H "Authorization: Bearer <accessToken>"
```

---

## 🚀 PUERTOS MAPEADOS

| Servicio | Puerto | Protocolo |
|----------|--------|-----------|
| Frontend Vite | 5173 | HTTP |
| API Gateway | 8080 | HTTP |
| MS-Auth | 8081 | HTTP |
| MS-Estudiantes | 8082 | HTTP |
| MS-Instructores | 8083 | HTTP |
| MS-Vehiculos | 8084 | HTTP |
| MS-Asignaciones | 8085 | HTTP |
| MS-Cobros | 8086 | HTTP |
| MS-Reportes | 8087 | HTTP |
| MS-Notificaciones | 8088 | HTTP |
| Eureka Server | 8761 | HTTP |
| MinIO API | 9000 | HTTP |
| MinIO Console | 9001 | HTTP |
| RabbitMQ AMQP | 5672 | AMQP |
| RabbitMQ Management | 15672 | HTTP |
| PostgreSQL | 5432 | TCP |
| Adminer | 8888 | HTTP |

---

## ✅ LISTA DE VERIFICACIÓN

### Al acceder al Frontend
- [ ] Página de login carga
- [ ] Ingreso credenciales admin
- [ ] Aparece dashboard
- [ ] Navego menu lateral
- [ ] Veo estudiantes (vacío ok)
- [ ] Veo instructores (vacío ok)
- [ ] Veo vehículos (vacío ok)
- [ ] Veo reportes
- [ ] Responsive en mobile (F12)

### Al acceder a Eureka
- [ ] Página Eureka carga
- [ ] Veo 9 servicios listados
- [ ] Todos con status UP
- [ ] Veo información de instancias

### Al acceder a RabbitMQ
- [ ] Página de login carga
- [ ] Ingreso guest/guest
- [ ] Veo sección Queues
- [ ] Veo sección Exchanges
- [ ] Cuento 32 queues
- [ ] Cuento 8 exchanges
- [ ] Veo consumers activos

### Al acceder a MinIO
- [ ] Página de login carga
- [ ] Ingreso minioadmin/minioadmin
- [ ] Veo Buckets
- [ ] Puedo navegar carpetas

### Al acceder a Adminer
- [ ] Página carga
- [ ] Selecciono PostgreSQL
- [ ] Ingreso credenciales
- [ ] Conecto exitosamente
- [ ] Veo lista de tablas
- [ ] Cuento 49 tablas
- [ ] Veo 10 schemas

---

## 🆘 TROUBLESHOOTING

### "Connection refused" en http://localhost:5173
- Verificar que el frontend está corriendo: `npm run dev`
- Verificar puerto no está en uso: `netstat -an | grep 5173`

### "Connection refused" en http://localhost:8080
- Verificar que Docker containers están arriba: `docker-compose ps`
- Verificar que API Gateway está healthy

### "Invalid credentials" al login
- Email debe ser exactamente: `admin@escuela.local`
- Password debe ser exactamente: `Admin123!`
- Verificar CAPS LOCK

### "RabbitMQ login failed"
- Usuario: `guest` (minúscula)
- Contraseña: `guest` (minúscula)
- No espacios extras

### "Cannot connect to PostgreSQL en Adminer"
- Server: `postgres` (nombre del container)
- No usar `localhost` en Adminer
- Usuario: `escuela_user`
- Password: `changeme`

---

## 📞 RESUMEN RÁPIDO

**Para Demostración/Testing:**

1. **Abre Frontend**: http://localhost:5173
   - admin@escuela.local / Admin123!

2. **Abre Eureka**: http://localhost:8761
   - Ver 9 servicios UP

3. **Abre RabbitMQ**: http://localhost:15672
   - guest / guest
   - Ver 32 queues

4. **Abre MinIO**: http://localhost:9001
   - minioadmin / minioadmin

5. **Abre BD**: http://localhost:8888
   - postgres / escuela_user / changeme
   - Ver 49 tablas

---

**Documento generado**: 2026-07-12
**Status**: ✅ TODAS LAS CREDENCIALES ACTIVAS
