# Getting Started — Sistema de Control Administrativo

Guía rápida para iniciar el sistema completo en tu máquina local.

## 📋 Requisitos

- **Docker & Docker Compose**: [Descargar](https://www.docker.com/products/docker-desktop)
- **Git**: `git --version`
- **RAM disponible**: Mínimo 4GB (recomendado 8GB+)
- **Disco**: 10GB libres

## 🚀 Inicio Rápido (5 minutos)

### 1. Clonar el repositorio

```bash
git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
cd proyecto-titulacion
```

### 2. Ir a la carpeta de infraestructura

```bash
cd infrastructure/docker
```

### 3. Levantar todos los containers

```bash
# Build + start (primera vez, tarda ~5 minutos)
docker-compose up -d --build

# O solo start (si ya están built)
docker-compose up -d
```

### 4. Verificar que todo esté UP

```bash
docker-compose ps

# Debe mostrar 14 containers:
# ✅ projeto-postgres (healthy)
# ✅ proyecto-rabbitmq (healthy)
# ✅ proyecto-eureka (healthy)
# ✅ proyecto-gateway (healthy)
# ✅ proyecto-frontend (healthy)
# ✅ 8x proyecto-ms-* (healthy)
# ✅ proyecto-adminer (up)
# ✅ proyecto-jenkins (up)
```

### 5. Acceder al sistema

| Servicio | URL | Uso |
|----------|-----|-----|
| **Frontend** | http://localhost:3000 | Aplicación principal |
| **API Gateway** | http://localhost:8080/actuator/health | Status API |
| **Eureka** | http://localhost:8761 | Service registry |
| **Adminer** | http://localhost:8089 | Admin de BD (PostgreSQL) |
| **RabbitMQ** | http://localhost:15672 | Admin de mensajería |
| **Jenkins** | http://localhost:8090 | CI/CD (opcional) |

### 6. Login

**Usuario:** `admin@escuela.com`  
**Contraseña:** (Verificar en `.env` o `.env.example`)

---

## 🔧 Comandos útiles

### Ver logs en tiempo real

```bash
# Logs de todos los servicios
docker-compose logs -f

# Logs de un MS específico
docker-compose logs -f ms-auth
docker-compose logs -f ms-estudiantes
```

### Detener todo

```bash
# Parar (preserva datos)
docker-compose down

# Parar y BORRAR datos (CUIDADO)
docker-compose down -v
```

### Reiniciar un servicio

```bash
docker-compose restart ms-auth
docker-compose restart proyecto-frontend
```

### Ver estado de healthchecks

```bash
docker-compose ps --format "table {{.Names}}\t{{.Status}}"
```

---

## 📊 Arquitectura del Sistema

### 14 Containers

**Infraestructura (3):**
- PostgreSQL 15 (BD)
- RabbitMQ 3.12 (Mensajería)
- Adminer (Admin BD)

**Core (3):**
- Eureka Server (Service Discovery)
- API Gateway (Routing)
- Frontend Vue.js 3

**Microservicios (8):**
- MS-Auth (8081)
- MS-Estudiantes (8082)
- MS-Instructores (8083)
- MS-Vehículos (8084)
- MS-Asignaciones (8085)
- MS-Cobros (8086)
- MS-Reportes (8087)
- MS-Notificaciones (8088)

**Herramientas (1):**
- Jenkins CI/CD (8090)

---

## 🐛 Solución de problemas

### Containers no arrancan

```bash
# 1. Ver error
docker-compose logs

# 2. Verificar espacio en disco
docker system df

# 3. Limpiar y reintentar
docker-compose down -v
docker-compose up -d --build
```

### Puerto ya en uso

```bash
# Windows/Mac
lsof -i :8080  # Eureka en 8761, Frontend en 3000, etc.

# Si está en uso, libera el puerto o cambia en docker-compose.yml
```

### Base de datos no se conecta

```bash
# Verificar PostgreSQL
docker-compose logs postgresql

# Reiniciar PostgreSQL
docker-compose restart postgresql
```

### Memoria insuficiente

```bash
# Asignar más RAM a Docker Desktop
# Preferences → Resources → Memory: 8GB
```

---

## 📚 Documentación Adicional

- **[CLAUDE.md](../CLAUDE.md)** — Instrucciones técnicas del proyecto
- **[DECISIONES.md](../DECISIONES.md)** — Decisiones arquitectónicas (32 ADRs)
- **[PLAN_FASES.md](../PLAN_FASES.md)** — Plan de 12 sprints
- **[infrastructure/docker/README.md](../infrastructure/docker/README.md)** — Docker Compose detallado
- **[docs/database/schema.md](../docs/database/schema.md)** — Diseño de BD

---

## 🎯 Flujos principales

### Login

1. Ir a http://localhost:3000
2. Ingresar credenciales
3. Se genera JWT válido por 2 horas
4. Acceso a dashboard según rol

### Crear clase (Asignación tripartita)

1. Dashboard → Programación
2. Seleccionar: Instructor + Estudiante + Vehículo
3. Sistema valida disponibilidad automáticamente
4. Confirmar y guardar

### Registrar pago

1. Dashboard → Cobros
2. Buscar estudiante
3. Registrar pago (parcial o total)
4. Sistema genera factura automáticamente

### Ver reportes

1. Dashboard → Reportes
2. Filtrar por fecha/rango
3. Exportar a PDF o Excel

---

## 🔐 Seguridad

- **JWT**: Válido 2 horas (HttpOnly cookies)
- **RBAC**: 4 roles (Admin, Personal, Instructor, Estudiante)
- **Encriptación**: Passwords con bcrypt
- **Rate limiting**: En API Gateway
- **OWASP**: Compliant (XSS prevention, CSRF, etc.)

---

## 📞 Soporte

**Autores:**
- Raúl Sebastián Cruz Baño
- Hernán Mateo Jurado Moran

**Tutor:** Víctor Javier Gómez Regalado  
**Universidad:** Universidad de las Américas (UDLA)  
**Entrega:** 5 de mayo de 2026

---

## 📄 Licencia

Proyecto académico — Universidad de las Américas (UDLA)
