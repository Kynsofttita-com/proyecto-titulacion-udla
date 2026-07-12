# 🚀 GUÍA RÁPIDA — Iniciar Sesión y Explorar el Sistema

---

## ⚡ ANTES QUE NADA

Verifica que Docker está corriendo:

```bash
docker-compose -f infrastructure/docker/docker-compose.yml ps
```

Deberías ver 14 contenedores en status **"healthy"** o **"Up"**.

Si algo no está UP, ejecuta:
```bash
cd /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto\ titulacion
docker-compose -f infrastructure/docker/docker-compose.yml up -d
```

---

## 🎯 PASO 1: ACCEDER AL FRONTEND (Sistema Principal)

### URL
```
http://localhost:5173
```

### Credenciales
```
Email:     admin@escuela.local
Contraseña: Admin123!
```

### Pasos
1. **Abre tu navegador** (Chrome, Edge, Firefox)
2. **Copia y pega**: `http://localhost:5173`
3. **Verás**: Página de login
4. **Ingresa**:
   - Email: `admin@escuela.local`
   - Contraseña: `Admin123!`
5. **Click**: Botón "Iniciar Sesión"
6. **¡Listo!**: Acceso al dashboard completo

### Qué puedes hacer en el Dashboard
- ✅ Ver estudiantes (vacío, sin datos aún)
- ✅ Gestionar instructores
- ✅ Controlar vehículos
- ✅ Ver reportes financieros
- ✅ Asignar clases
- ✅ Gestionar pagos
- ✅ Ver historial de auditoría
- ✅ Descargar reportes en PDF/Excel

---

## 🔍 PASO 2: EXPLORAR LOS SERVICIOS INTERNOS

### 2.1 EUREKA — Ver Microservicios Registrados

**URL**: `http://localhost:8761`

**Qué hace**: Te muestra los 9 microservicios que están corriendo
```
✅ API-GATEWAY (8080)
✅ MS-AUTH (8081)
✅ MS-ESTUDIANTES (8082)
✅ MS-INSTRUCTORES (8083)
✅ MS-VEHICULOS (8084)
✅ MS-ASIGNACIONES (8085)
✅ MS-COBROS (8086)
✅ MS-REPORTES (8087)
✅ MS-NOTIFICACIONES (8088)
```

**Cómo acceder**: Simplemente abre la URL, no necesita credenciales

---

### 2.2 RABBITMQ — Ver Cola de Mensajes

**URL**: `http://localhost:15672`

**Credenciales**:
```
Usuario: guest
Contraseña: guest
```

**Pasos**:
1. Abre `http://localhost:15672`
2. Click "Login"
3. Usuario: `guest`
4. Contraseña: `guest`
5. Click "Login"

**Qué ves**:
- 32 Queues (colas de mensajes)
- 8 Exchanges (canales)
- Consumers activos
- Mensajes pendientes

**Sections importantes**:
- **Queues**: Ver colas como `auth.queue`, `estudiantes.queue`, etc.
- **Exchanges**: Ver canales como `auth.exchange`, `estudiantes.exchange`, etc.
- **Admin > Users**: Gestión de usuarios

---

### 2.3 MINIO — Almacenamiento de Archivos (S3)

**URL**: `http://localhost:9001`

**Credenciales**:
```
Usuario: minioadmin
Contraseña: minioadmin
```

**Pasos**:
1. Abre `http://localhost:9001`
2. Usuario: `minioadmin`
3. Contraseña: `minioadmin`
4. Click "Login"

**Qué es**:
- Almacenamiento tipo Amazon S3
- Guardas archivos de estudiantes, PDFs, fotos
- Compatible con cualquier aplicación S3

---

### 2.4 ADMINER — Acceder a Base de Datos

**URL**: `http://localhost:8888`

**Credenciales**:
```
Servidor: postgres
Usuario: escuela_user
Contraseña: changeme
Base de datos: escuela_db
```

**Pasos**:
1. Abre `http://localhost:8888`
2. En "Motor de base de datos" selecciona `PostgreSQL`
3. Servidor: `postgres`
4. Usuario: `escuela_user`
5. Contraseña: `changeme`
6. Base de datos: dejar en blanco
7. Click "Login"

**Qué puedes hacer**:
- Ver tablas directamente
- Ejecutar queries SQL
- Ver datos en tiempo real
- Editar registros manualmente
- Hacer backups

---

## 📋 TABLA RÁPIDA DE ACCESOS

| Servicio | URL | Usuario | Contraseña | Descripción |
|----------|-----|---------|-----------|-------------|
| **Frontend** | http://localhost:5173 | admin@escuela.local | Admin123! | Aplicación web principal |
| **Eureka** | http://localhost:8761 | - | - | Ver microservicios |
| **RabbitMQ** | http://localhost:15672 | guest | guest | Mensajería |
| **MinIO** | http://localhost:9001 | minioadmin | minioadmin | Archivos S3 |
| **Adminer** | http://localhost:8888 | escuela_user | changeme | Base de datos |
| **API Gateway** | http://localhost:8080 | - | - | APIs REST |

---

## 🔌 PUERTOS IMPORTANTES

```
5173  - Frontend Vue.js 3
8080  - API Gateway
8081  - MS-Auth
8082  - MS-Estudiantes
8083  - MS-Instructores
8084  - MS-Vehiculos
8085  - MS-Asignaciones
8086  - MS-Cobros
8087  - MS-Reportes
8088  - MS-Notificaciones
8761  - Eureka
9001  - MinIO Console
5432  - PostgreSQL
5672  - RabbitMQ AMQP
15672 - RabbitMQ Management
8888  - Adminer
```

---

## ✅ CHECKLIST DE VALIDACIÓN

Haz esto para verificar que TODO funciona:

### Frontend
- [ ] Abro http://localhost:5173
- [ ] Página de login carga correctamente
- [ ] Ingreso admin@escuela.local / Admin123!
- [ ] Entro al dashboard
- [ ] Navego por el menú
- [ ] Veo todas las secciones
- [ ] Cambio tema (claro/oscuro)
- [ ] Veo que es responsive en mobile (F12)

### Eureka
- [ ] Abro http://localhost:8761
- [ ] Veo 9 servicios listados
- [ ] Todos están en status "UP"
- [ ] Veo instancias activas

### RabbitMQ
- [ ] Abro http://localhost:15672
- [ ] Login con guest/guest funciona
- [ ] Veo sección "Queues"
- [ ] Cuento 32 queues
- [ ] Veo sección "Exchanges"
- [ ] Cuento 8 exchanges
- [ ] Veo "Connections" con conexiones activas

### MinIO
- [ ] Abro http://localhost:9001
- [ ] Login con minioadmin/minioadmin funciona
- [ ] Veo buckets
- [ ] Puedo crear carpetas
- [ ] Puedo subir archivos

### Base de Datos (Adminer)
- [ ] Abro http://localhost:8888
- [ ] Login con credenciales funciona
- [ ] Veo lista de tablas
- [ ] Cuento ~49 tablas
- [ ] Veo múltiples schemas (auth, estudiantes, etc.)
- [ ] Puedo hacer queries SQL

---

## 🐛 PROBLEMAS COMUNES

### "No puedo acceder a http://localhost:5173"
```
Solución: Verifica que el servidor Vite está corriendo
Abre terminal en: frontend/
Ejecuta: npm run dev
```

### "Connection refused en 8080"
```
Solución: Verifica que Docker está arriba
Abre terminal donde está docker-compose.yml
Ejecuta: docker-compose up -d
Espera 2-3 minutos a que inicie
```

### "Login fallido (Email/Contraseña incorrecta)"
```
Verifica:
- Email: admin@escuela.local (exacto)
- Contraseña: Admin123! (con mayúscula)
- Sin espacios extras
- CAPS LOCK apagado
```

### "RabbitMQ login failed"
```
Usuario: guest (minúscula)
Contraseña: guest (minúscula)
No hay espacios
```

### "Adminer no conecta a base de datos"
```
En lugar de "localhost", escribe: postgres
Usuario: escuela_user
Password: changeme
Base de datos: escuela_db
```

---

## 🎓 QUÉ PUEDES PROBAR

### 1. Test de Autenticación
- Intenta login con email incorrecto → debe rechazar
- Intenta login con contraseña incorrecta → debe rechazar
- Intenta login correcto → debe aceptar
- Cierra sesión → debe volver al login

### 2. Test de Base de Datos
- Abre Adminer
- Ejecuta query: `SELECT COUNT(*) FROM auth_schema.usuarios;`
- Debería mostrar 1 usuario (admin)

### 3. Test de RabbitMQ
- Publica un mensaje en exchange
- Verifica que llega a la queue
- Consume el mensaje

### 4. Test de Responsividad
- Abre Frontend
- Presiona F12
- Selecciona tamaño mobile
- Verifica que se adapta correctamente

---

## 📞 CONTACTO Y AYUDA

Si algo no funciona:

1. **Verifica Docker**: `docker-compose ps`
2. **Verifica logs**: `docker-compose logs -f <servicio>`
3. **Reinicia todo**: 
   ```bash
   docker-compose down
   docker-compose up -d
   ```

---

## 🎉 LISTO

Con esta guía ya puedes:
✅ Acceder al sistema completo
✅ Explorar todos los servicios
✅ Validar que todo funciona
✅ Ver la arquitectura en acción

**¡Bienvenido al sistema!** 🚀
