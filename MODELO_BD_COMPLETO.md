# 📊 Modelo Completo de Base de Datos - Sprint 5

**Proyecto**: Sistema de Escuelas de Conducción  
**BD**: PostgreSQL 15  
**Estrategia**: 1 Base de datos con 9 schemas separados  
**Patrón**: Schema-per-Microservice

---

## 🏗️ Arquitectura de Schemas

```
┌─────────────────────────────────────────────────────────────┐
│                      1 PostgreSQL DB                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  shared_schema (Auditoría + Idempotencia)            │  │
│  │  ├─ audit_log (10M+ registros)                       │  │
│  │  └─ processed_events (deduplicación RabbitMQ)        │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌─────────────────┐ ┌─────────────────┐ ┌──────────────┐  │
│  │ auth_schema     │ │estudiantes_sch. │ │instructores_ │  │
│  │ (Auth)          │ │(MS-Estudiantes) │ │schema        │  │
│  │                 │ │                 │ │(MS-Instruc)  │  │
│  │ • usuarios      │ │ • estudiantes   │ │              │  │
│  │ • roles         │ │ • documentos    │ │ • instructo. │  │
│  │ • permisos      │ │ • contactos     │ │ • certificac │  │
│  │ • token_refresh │ │ • progreso      │ │ • disponib.  │  │
│  │ • categorias    │ │ • asistencia    │ │              │  │
│  └─────────────────┘ └─────────────────┘ └──────────────┘  │
│                                                              │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐    │
│  │ vehiculos_   │ │asignaciones_ │ │cobros_schema    │    │
│  │schema        │ │schema        │ │(MS-Cobros)      │    │
│  │(MS-Vehículos)│ │(MS-Asignac.) │ │                 │    │
│  │              │ │              │ │ • facturas      │    │
│  │ • vehiculos  │ │ • asignac.   │ │ • pagos         │    │
│  │ • documento_ │ │ • cambios    │ │ • reconcilia.   │    │
│  │  vehiculo    │ │ • historial  │ │                 │    │
│  │ • mantenimie │ │              │ │                 │    │
│  │  nto         │ │              │ │                 │    │
│  │ • combustible│ │              │ │                 │    │
│  └──────────────┘ └──────────────┘ └──────────────────┘    │
│                                                              │
│  ┌──────────────────┐ ┌──────────────────────────────────┐  │
│  │notificaciones_   │ │reportes_schema (MS-Reportes)    │  │
│  │schema            │ │                                  │  │
│  │(MS-Notificacion) │ │ • cache_reportes                │  │
│  │                  │ │ • ejecuciones_reporte           │  │
│  │ • notificaciones │ │                                  │  │
│  │ • log_envios     │ │                                  │  │
│  │ • preferencias   │ │                                  │  │
│  └──────────────────┘ └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📋 Tabla de Contenidos por Schema

| Schema | Microservicio | Tablas | Propósito |
|---|---|---|---|
| `shared_schema` | MS-Auth | 2 | Auditoría centralizada + Idempotencia |
| `auth_schema` | MS-Auth | 13 | Autenticación, autorización, roles |
| `estudiantes_schema` | MS-Estudiantes | 5 | Gestión de estudiantes |
| `instructores_schema` | MS-Instructores | 3 | Gestión de instructores |
| `vehiculos_schema` | MS-Vehículos | 3 | Gestión de vehículos y mantenimiento |
| `asignaciones_schema` | MS-Asignaciones | 3 | Clases (tripartitas: alumno+instructor+vehículo) |
| `cobros_schema` | MS-Cobros | 3 | Facturación y pagos |
| `notificaciones_schema` | MS-Notificaciones | 3 | Registro de notificaciones |
| `reportes_schema` | MS-Reportes | 2 | Cache y ejecución de reportes |

**TOTAL**: 9 schemas, 38 tablas, 1 base de datos

---

## 🔐 SCHEMA: shared_schema (Centralizado)

### 1. **audit_log** (Append-Only)
```sql
CREATE TABLE shared_schema.audit_log (
    id              BIGSERIAL PRIMARY KEY,
    microservicio   VARCHAR(50),
    recurso         VARCHAR(100),
    recurso_id      VARCHAR(50),
    accion          VARCHAR(50),      -- CREATE, UPDATE, DELETE, READ, LOGIN, LOGOUT
    usuario_id      BIGINT,
    usuario_email   VARCHAR(255),
    ip              VARCHAR(45),
    user_agent      TEXT,
    datos_anteriores JSONB,           -- Antes del cambio
    datos_nuevos    JSONB,            -- Después del cambio
    correlation_id  VARCHAR(100),
    fecha           TIMESTAMP DEFAULT NOW()
);
```

**Propósito**: Registro inmutable de auditoría para compliance  
**Índices**: microservicio+fecha, usuario+fecha, recurso, correlation_id  
**Estrategia**: Append-only (NUNCA se actualiza)

### 2. **processed_events**
```sql
CREATE TABLE shared_schema.processed_events (
    id                      BIGSERIAL PRIMARY KEY,
    event_id                UUID NOT NULL UNIQUE,
    microservicio_consumidor VARCHAR(50),
    tipo_evento              VARCHAR(100),
    processed_at            TIMESTAMP DEFAULT NOW()
);
```

**Propósito**: Idempotencia para consumidores RabbitMQ  
**Índices**: event_id (único), microservicio_consumidor+fecha

---

## 🔑 SCHEMA: auth_schema (13 tablas)

### 1. **usuarios**
```
┌─ Tabla: auth_schema.usuarios
├─ Campos principales:
│  ├─ id (BIGSERIAL PRIMARY KEY)
│  ├─ email (VARCHAR UNIQUE) ← Credencial login
│  ├─ password (VARCHAR 60) ← BCrypt hash
│  ├─ nombre + apellido (VARCHAR)
│  ├─ telefono (VARCHAR 10)
│  ├─ activo (BOOLEAN)
│  ├─ locked (BOOLEAN) ← Después de 3 fallos
│  ├─ failed_attempts (SMALLINT)
│  ├─ last_login (TIMESTAMP)
│  ├─ lock_until (TIMESTAMP)
│  └─ Auditoría: created_at, updated_at, deleted_at
├─ Constraints:
│  ├─ UNIQUE (email)
│  ├─ CHECK email ~* '^[A-Za-z0-9._%+-]+@...$'
│  └─ CHECK telefono ~ '^09[0-9]{8}$'
└─ Índices: email, (activo, locked)
```

### 2. **roles**
```
┌─ Tabla: auth_schema.roles
├─ Datos predefinidos:
│  ├─ ADMIN (acceso total)
│  ├─ STAFF (personal administrativo)
│  ├─ INSTRUCTOR (docentes)
│  └─ ESTUDIANTE (alumnos)
└─ Relación: usuario → roles (muchos a muchos)
```

### 3. **permisos**
```
┌─ Tabla: auth_schema.permisos
├─ Estructura:
│  ├─ codigo (VARCHAR UNIQUE) ← "estudiantes:create"
│  ├─ recurso (VARCHAR) ← "estudiantes"
│  ├─ accion (VARCHAR) ← "create", "read", "update", "delete"
│  └─ descripcion
└─ Relación: rol → permisos (muchos a muchos)
```

### 4. **refresh_tokens**
```
┌─ Tabla: auth_schema.refresh_tokens
├─ Campos:
│  ├─ id (BIGSERIAL PRIMARY KEY)
│  ├─ usuario_id (BIGINT FK)
│  ├─ token (VARCHAR UNIQUE)
│  ├─ expires_at (TIMESTAMP)
│  ├─ revoked (BOOLEAN)
│  └─ created_at (TIMESTAMP)
└─ Propósito: Tokens de renovación para JWT
```

### 5. **categorias_licencia**
```
┌─ Tabla: auth_schema.categorias_licencia
├─ Datos predefinidos (Ecuador):
│  ├─ A (automóvil)
│  ├─ B (jeep/camioneta)
│  ├─ C (camión)
│  └─ D (autobús)
└─ Configurable por escuela
```

**Nota**: Hay más 8 tablas en auth_schema (tipos_curso, config_escuela, etc.)

---

## 👨‍🎓 SCHEMA: estudiantes_schema (5 tablas)

### 1. **estudiantes**
```sql
CREATE TABLE estudiantes_schema.estudiantes (
    id                    BIGSERIAL PRIMARY KEY,
    cedula                VARCHAR(10) NOT NULL UNIQUE,
    nombre                VARCHAR(100) NOT NULL,
    apellido              VARCHAR(100) NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    telefono              VARCHAR(10),
    direccion             TEXT,
    fecha_nacimiento      DATE NOT NULL,
    genero                CHAR(1), -- M, F, O
    estado                VARCHAR(20) DEFAULT 'PRE_MATRICULADO',
    -- Estados: PRE_MATRICULADO, ACTIVO, COMPLETADO, RETIRADO
    fecha_matricula       DATE,
    tipo_curso_id         BIGINT, -- ref a auth_schema.tipos_curso
    categoria_licencia_id BIGINT, -- ref a auth_schema.categorias_licencia
    usuario_id            BIGINT, -- ref a auth_schema.usuarios (si tiene login)
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP,
    created_by            VARCHAR(50),
    updated_by            VARCHAR(50),
    deleted_at            TIMESTAMP  -- Soft-delete
);
```

**Índices**: cedula, email, estado (WHERE deleted_at IS NULL), apellido+nombre

### 2. **documentos**
```sql
CREATE TABLE estudiantes_schema.documentos (
    id               BIGSERIAL PRIMARY KEY,
    estudiante_id    BIGINT NOT NULL FK,
    tipo             VARCHAR(30), -- CEDULA_FRENTE, CEDULA_REVERSO, FOTO, EXAMEN_MEDICO
    url_archivo      VARCHAR(500) NOT NULL, -- MinIO path
    fecha_subida     TIMESTAMP DEFAULT NOW(),
    mime_type        VARCHAR(100),
    tamano_bytes     BIGINT,
    created_at       TIMESTAMP DEFAULT NOW(),
    deleted_at       TIMESTAMP
);
```

### 3. **contactos_emergencia**
```sql
CREATE TABLE estudiantes_schema.contactos_emergencia (
    id               BIGSERIAL PRIMARY KEY,
    estudiante_id    BIGINT NOT NULL FK,
    nombre           VARCHAR(200) NOT NULL,
    telefono         VARCHAR(15) NOT NULL,
    parentesco       VARCHAR(50),
    es_principal     BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT NOW()
);
```

### 4. **progreso_academico**
```sql
CREATE TABLE estudiantes_schema.progreso_academico (
    id                    BIGSERIAL PRIMARY KEY,
    estudiante_id         BIGINT UNIQUE FK,
    clases_planeadas      SMALLINT DEFAULT 0,
    clases_completadas    SMALLINT DEFAULT 0,
    clases_pendientes     SMALLINT DEFAULT 0,
    clases_canceladas     SMALLINT DEFAULT 0,
    calificacion_promedio NUMERIC(4,2), -- 0-100
    aprobado              BOOLEAN,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP
);
```

### 5. **asistencia**
```sql
CREATE TABLE estudiantes_schema.asistencia (
    id               BIGSERIAL PRIMARY KEY,
    estudiante_id    BIGINT NOT NULL FK,
    asignacion_id    BIGINT NOT NULL, -- ref a asignaciones_schema.asignaciones
    fecha_clase      DATE NOT NULL,
    asistio          BOOLEAN NOT NULL,
    justificacion    TEXT,
    observaciones    TEXT,
    created_at       TIMESTAMP DEFAULT NOW()
);
```

---

## 👨‍🏫 SCHEMA: instructores_schema (3 tablas)

### 1. **instructores**
```sql
CREATE TABLE instructores_schema.instructores (
    id                BIGSERIAL PRIMARY KEY,
    cedula            VARCHAR(10) NOT NULL UNIQUE,
    email             VARCHAR(255) NOT NULL UNIQUE,
    licencia          VARCHAR(20) NOT NULL UNIQUE, -- Número de licencia
    nombre            VARCHAR(100) NOT NULL,
    apellido          VARCHAR(100) NOT NULL,
    telefono          VARCHAR(10),
    direccion         TEXT,
    fecha_nacimiento  DATE,
    genero            CHAR(1),
    estado            VARCHAR(20) DEFAULT 'ACTIVO',
    usuario_id        BIGINT FK, -- ref a auth_schema.usuarios
    años_experiencia  SMALLINT,
    observaciones     TEXT,
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP,
    deleted_at        TIMESTAMP  -- Soft-delete
);
```

### 2. **certificaciones**
```sql
CREATE TABLE instructores_schema.certificaciones (
    id                BIGSERIAL PRIMARY KEY,
    instructor_id     BIGINT NOT NULL FK,
    nombre_certificado VARCHAR(200) NOT NULL,
    fecha_emision     DATE NOT NULL,
    fecha_expiracion  DATE,
    url_documento     VARCHAR(500),
    created_at        TIMESTAMP DEFAULT NOW()
);
```

### 3. **disponibilidad**
```sql
CREATE TABLE instructores_schema.disponibilidad (
    id                BIGSERIAL PRIMARY KEY,
    instructor_id     BIGINT NOT NULL FK,
    dia_semana        SMALLINT, -- 1=lunes, 7=domingo
    hora_inicio       TIME,
    hora_fin          TIME,
    disponible        BOOLEAN DEFAULT TRUE,
    created_at        TIMESTAMP DEFAULT NOW()
);
```

---

## 🚗 SCHEMA: vehiculos_schema (3 tablas)

### 1. **vehiculos**
```sql
CREATE TABLE vehiculos_schema.vehiculos (
    id                    BIGSERIAL PRIMARY KEY,
    placa                 VARCHAR(10) NOT NULL UNIQUE, -- ABC-1234
    marca                 VARCHAR(100) NOT NULL,
    modelo                VARCHAR(100) NOT NULL,
    anio                  SMALLINT, -- Año del vehículo
    vin                   VARCHAR(20) UNIQUE,
    color                 VARCHAR(50),
    tipo_vehiculo         VARCHAR(50), -- AUTOMOVIL, JEEP, CAMIONETA
    numero_asientos       SMALLINT,
    kilometraje           BIGINT,
    estado                VARCHAR(20) DEFAULT 'DISPONIBLE',
    -- Estados: DISPONIBLE, EN_MANTENIMIENTO, FUERA_SERVICIO, RETIRADO
    propietario_id        BIGINT, -- ref a usuario propietario
    fecha_adquisicion     DATE,
    seguro_vigencia       DATE,
    soat_vigencia         DATE,
    revision_tecnica      DATE,
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP,
    deleted_at            TIMESTAMP  -- Soft-delete
);
```

**Índices**: placa, estado (WHERE deleted_at IS NULL)

### 2. **mantenimientos**
```sql
CREATE TABLE vehiculos_schema.mantenimientos (
    id                    BIGSERIAL PRIMARY KEY,
    vehiculo_id           BIGINT NOT NULL FK,
    tipo                  VARCHAR(30), -- PREVENTIVO, CORRECTIVO
    descripcion           VARCHAR(255),
    fecha_inicio          DATE NOT NULL,
    fecha_fin             DATE,
    costo                 NUMERIC(12,2),
    proveedor             VARCHAR(200),
    kilometraje_realizado BIGINT,
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

### 3. **registros_combustible**
```sql
CREATE TABLE vehiculos_schema.registros_combustible (
    id                    BIGSERIAL PRIMARY KEY,
    vehiculo_id           BIGINT NOT NULL FK,
    fecha_carga           DATE NOT NULL,
    litros_cargados       NUMERIC(8,2),
    costo_unitario        NUMERIC(8,2),
    costo_total           NUMERIC(12,2),
    kilometraje           BIGINT,
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

---

## 📅 SCHEMA: asignaciones_schema (3 tablas)

### 1. **asignaciones** (Tripartita: Instructor + Estudiante + Vehículo)
```sql
CREATE TABLE asignaciones_schema.asignaciones (
    id                    BIGSERIAL PRIMARY KEY,
    instructor_id         BIGINT NOT NULL, -- ref a instructores_schema
    estudiante_id         BIGINT NOT NULL, -- ref a estudiantes_schema
    vehiculo_id           BIGINT NOT NULL, -- ref a vehiculos_schema
    fecha_hora            TIMESTAMP NOT NULL,
    duracion_minutos      SMALLINT DEFAULT 60,
    estado                VARCHAR(30) DEFAULT 'CONFIRMADA',
    -- Estados: CONFIRMADA, REPROGRAMADA, CANCELADA, COMPLETADA, NO_PRESENTADO
    razon_cancelacion     VARCHAR(255),
    observaciones         TEXT,
    version               BIGINT DEFAULT 0, -- Optimistic locking
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP,
    deleted_at            TIMESTAMP  -- Soft-delete
);
```

**Propósito**: Modelar las clases tripartitas  
**Índices**: instructor+fecha, estudiante+fecha, vehiculo+fecha

### 2. **cambios_asignacion**
```sql
CREATE TABLE asignaciones_schema.cambios_asignacion (
    id                    BIGSERIAL PRIMARY KEY,
    asignacion_id         BIGINT NOT NULL FK,
    tipo_cambio           VARCHAR(30), -- REPROGRAMACION, CANCELACION, CAMBIO_INSTRUCTOR
    fecha_cambio          TIMESTAMP NOT NULL,
    razon                 VARCHAR(255),
    usuario_id            BIGINT FK, -- quien hizo el cambio
    datos_anteriores      JSONB,
    datos_nuevos          JSONB,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

### 3. **historial_estados**
```sql
CREATE TABLE asignaciones_schema.historial_estados (
    id                    BIGSERIAL PRIMARY KEY,
    asignacion_id         BIGINT NOT NULL FK,
    estado_anterior       VARCHAR(30),
    estado_nuevo          VARCHAR(30),
    fecha_cambio          TIMESTAMP NOT NULL DEFAULT NOW(),
    razon                 VARCHAR(255),
    usuario_id            BIGINT FK
);
```

---

## 💰 SCHEMA: cobros_schema (3 tablas)

### 1. **facturas**
```sql
CREATE TABLE cobros_schema.facturas (
    id                    BIGSERIAL PRIMARY KEY,
    numero_factura        VARCHAR(20) UNIQUE, -- SRI format
    estudiante_id         BIGINT NOT NULL FK,
    concepto              VARCHAR(255),
    monto_bruto           NUMERIC(12,2),
    impuestos             NUMERIC(12,2),
    monto_neto            NUMERIC(12,2),
    descuentos            NUMERIC(12,2),
    estado                VARCHAR(30), -- PENDIENTE, PAGADA, VENCIDA, ANULADA
    fecha_emision         DATE NOT NULL,
    fecha_vencimiento     DATE,
    fecha_pago            DATE,
    forma_pago            VARCHAR(30), -- EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA
    referencia_pago       VARCHAR(100),
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW(),
    updated_at            TIMESTAMP
);
```

### 2. **pagos**
```sql
CREATE TABLE cobros_schema.pagos (
    id                    BIGSERIAL PRIMARY KEY,
    factura_id            BIGINT NOT NULL FK,
    monto                 NUMERIC(12,2),
    fecha_pago            DATE NOT NULL,
    forma_pago            VARCHAR(30),
    referencia            VARCHAR(100),
    banco                 VARCHAR(50),
    comprobante_path      VARCHAR(500), -- Imagen escaneada
    observaciones         TEXT,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

### 3. **reconciliacion**
```sql
CREATE TABLE cobros_schema.reconciliacion (
    id                    BIGSERIAL PRIMARY KEY,
    fecha_reconciliacion  DATE NOT NULL,
    total_facturado       NUMERIC(14,2),
    total_pagado          NUMERIC(14,2),
    total_pendiente       NUMERIC(14,2),
    total_vencido         NUMERIC(14,2),
    discrepancias         JSONB,
    estado                VARCHAR(30), -- PENDIENTE, RECONCILIADO, DISCREPANCIA
    created_at            TIMESTAMP DEFAULT NOW()
);
```

---

## 🔔 SCHEMA: notificaciones_schema (3 tablas)

### 1. **notificaciones**
```sql
CREATE TABLE notificaciones_schema.notificaciones (
    id                    BIGSERIAL PRIMARY KEY,
    usuario_id            BIGINT NOT NULL FK,
    tipo                  VARCHAR(50), -- EMAIL, SMS, WHATSAPP, IN_APP
    asunto                VARCHAR(255),
    cuerpo                TEXT,
    estado                VARCHAR(30), -- PENDIENTE, ENVIADO, FALLIDO, RECHAZADO
    intentos              SMALLINT DEFAULT 0,
    siguiente_intento     TIMESTAMP,
    created_at            TIMESTAMP DEFAULT NOW(),
    enviado_at            TIMESTAMP
);
```

### 2. **log_envios_email**
```sql
CREATE TABLE notificaciones_schema.log_envios_email (
    id                    BIGSERIAL PRIMARY KEY,
    notificacion_id       BIGINT NOT NULL FK,
    email                 VARCHAR(255),
    proveedor             VARCHAR(50), -- Mailtrap, Gmail, AWS SES
    message_id            VARCHAR(255), -- ID del proveedor
    status_code           SMALLINT,
    status_mensaje        TEXT,
    respuesta_json        JSONB,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

### 3. **preferencias_notificacion**
```sql
CREATE TABLE notificaciones_schema.preferencias_notificacion (
    id                    BIGSERIAL PRIMARY KEY,
    usuario_id            BIGINT NOT NULL UNIQUE FK,
    email_habilitado      BOOLEAN DEFAULT TRUE,
    sms_habilitado        BOOLEAN DEFAULT FALSE,
    whatsapp_habilitado   BOOLEAN DEFAULT FALSE,
    horario_inicio        TIME,
    horario_fin           TIME,
    updated_at            TIMESTAMP DEFAULT NOW()
);
```

---

## 📊 SCHEMA: reportes_schema (2 tablas)

### 1. **ejecuciones_reporte**
```sql
CREATE TABLE reportes_schema.ejecuciones_reporte (
    id                    BIGSERIAL PRIMARY KEY,
    nombre_reporte        VARCHAR(100),
    usuario_id            BIGINT NOT NULL FK,
    fecha_inicio          TIMESTAMP NOT NULL,
    fecha_fin             TIMESTAMP,
    formato               VARCHAR(30), -- PDF, EXCEL, CSV, JSON
    estado                VARCHAR(30), -- EJECUTANDO, COMPLETADO, ERROR
    ruta_archivo          VARCHAR(500), -- MinIO path
    tamaño_kb             BIGINT,
    error_mensaje         TEXT,
    parametros_json       JSONB,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

### 2. **cache_reportes**
```sql
CREATE TABLE reportes_schema.cache_reportes (
    id                    BIGSERIAL PRIMARY KEY,
    clave_cache           VARCHAR(255) UNIQUE,
    data_json             JSONB,
    fecha_generacion      TIMESTAMP NOT NULL,
    fecha_expiracion      TIMESTAMP,
    ttl_segundos          BIGINT,
    created_at            TIMESTAMP DEFAULT NOW()
);
```

---

## 🔗 Relaciones Inter-Schema (Sin Foreign Keys)

Debido a la estrategia schema-per-service, **no hay FKs entre schemas**. Las relaciones se manejan por:

1. **Referencias por ID** (almacenadas pero no validadas en BD):
   ```
   estudiantes_schema.estudiantes.usuario_id → auth_schema.usuarios.id
   estudiantes_schema.estudiantes.tipo_curso_id → auth_schema.tipos_curso.id
   instructores_schema.instructores.usuario_id → auth_schema.usuarios.id
   ```

2. **Auditoría en shared_schema**:
   ```
   shared_schema.audit_log registra cambios en TODOS los schemas
   ```

3. **Eventos RabbitMQ** para sincronización:
   ```
   Cuando cambia usuario en auth → evento → actualiza en estudiantes/instructores
   Cuando se crea asignación → evento → notifica a estudiante/instructor
   ```

---

## 📝 Migraciones Flyway

```
backend/
├── ms-auth/src/main/resources/db/migration/
│   ├── V1__Initial_Schema.sql        (shared_schema + auth_schema)
│   ├── V2__refresh_tokens.sql
│   └── V1_5__Seed_Data.sql           (admin@escuela.local / Admin123!)
│
├── ms-estudiantes/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (estudiantes_schema)
│
├── ms-instructores/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (instructores_schema)
│
├── ms-vehiculos/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (vehiculos_schema)
│
├── ms-asignaciones/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (asignaciones_schema)
│
├── ms-cobros/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (cobros_schema)
│
├── ms-notificaciones/src/main/resources/db/migration/
│   └── V1__Initial_Schema.sql        (notificaciones_schema)
│
└── ms-reportes/src/main/resources/db/migration/
    └── V1__Initial_Schema.sql        (reportes_schema)
```

**Estrategia Flyway**:
- Cada microservicio versionado independientemente
- MS-Auth crea también shared_schema
- No hay dependencias entre migraciones
- Cada servicio puede desplegarse sin esperar a otros

---

## 🎯 Características de Diseño

### ✅ Auditoría Centralizada
```
• append_log en shared_schema
• Registra: usuario, ip, cambios antes/después (JSONB)
• Correlation ID para rastrear operaciones distribuidas
```

### ✅ Idempotencia
```
• processed_events previene procesar 2x el mismo evento RabbitMQ
• Válido para rechazos/reintentos de red
```

### ✅ Soft-Delete
```
• deleted_at para preservar auditoría
• Índices usan WHERE deleted_at IS NULL
• Nunca se borra, solo se marca
```

### ✅ Optimistic Locking
```
• asignaciones.version para evitar race conditions
• Al actualizar, verificar version antes de guardar
```

### ✅ Validación en BD (Constraints)
```
• CHECK para formatos (email, teléfono, cédula)
• UNIQUE para campos únicos (cedula, email, placa, licencia)
• Data integrity a nivel BD
```

---

## 📊 Estadísticas

| Métrica | Valor |
|---|---|
| Total de Schemas | 9 |
| Total de Tablas | 38+ |
| Microservicios | 8 |
| Auditoría Centralizada | ✅ Sí |
| Cross-Schema FKs | ❌ No (por diseño) |
| Soft-Delete Pattern | ✅ Sí |
| Optimistic Locking | ✅ Sí (asignaciones) |
| Índices Optimizados | ✅ Sí |

---

## 🚀 Scripts SQL Disponibles

```bash
# Ver todos los schemas
cd backend

# MS-Auth (shared + auth schemas)
cat ms-auth/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Estudiantes
cat ms-estudiantes/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Instructores
cat ms-instructores/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Vehículos
cat ms-vehiculos/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Asignaciones
cat ms-asignaciones/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Cobros
cat ms-cobros/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Notificaciones
cat ms-notificaciones/src/main/resources/db/migration/V1__Initial_Schema.sql

# MS-Reportes
cat ms-reportes/src/main/resources/db/migration/V1__Initial_Schema.sql
```

---

**Documento**: MODELO_BD_COMPLETO.md  
**Versión**: 1.0  
**Fecha**: 12 Mayo 2026  
**Estado**: ✅ Implementado en Sprint 5

---

## 📚 Referencias

- Migraciones Flyway: `backend/*/src/main/resources/db/migration/`
- Entidades JPA: `backend/*/src/main/java/*/entity/`
- Documentación completa: `DOCUMENTACION_PROYECTO_SPRINT_REVIEW.md`
