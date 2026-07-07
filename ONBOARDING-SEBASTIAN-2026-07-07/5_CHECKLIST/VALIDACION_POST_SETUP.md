# ✅ Validación Post-Setup — Checklist de confirmación

**Después de ejecutar los 4 pasos de INICIO_RAPIDO, verifica TODO aquí.**

Marca cada ✓ conforme lo hagas.

---

## 🚀 Validación en 10 puntos

### 1️⃣ Git repository clonado

```bash
git status
```

✓ Debe decir: `On branch main` y `nothing to commit, working tree clean`

**Si falla:** 
```bash
git status | head -5
# Reportá el error exacto
```

---

### 2️⃣ Archivo `.env` existe y está completo

```bash
ls -la .env
```

✓ Debe existir (no error de "No such file")

```bash
grep -c "^[A-Z_]*=" .env
```

✓ Debe retornar: **≥ 8** (mínimo 8 variables)

```bash
grep "CAMBIAR\|<" .env
```

✓ Debe estar **vacío** (sin placeholders `<...>`)

**Si falla:** Pide el `.env` a Hernán de nuevo

---

### 3️⃣ Docker: 14 contenedores "healthy"

```bash
docker compose -f infrastructure/docker/docker-compose.yml ps
```

✓ Debe mostrar 14 contenedores  
✓ Todos deben decir "healthy" (no puede haber "unhealthy" ni "exited")

**Si falla:**
```bash
docker compose -f infrastructure/docker/docker-compose.yml logs | tail -50
# Ver si hay errores
```

**Si algunos dicen "unhealthy":**
- Esperar 30s más (healthchecks tardan)
- Ejecutar `ps` de nuevo

**Si dice "Connection refused":**
- Normal durante startup
- Esperar 60s total desde que hiciste `up -d`

---

### 4️⃣ Frontend levantó en http://localhost:5173

```bash
curl -s http://localhost:5173 | head -20
```

✓ Debe retornar HTML (que comience con `<!DOCTYPE html` o `<html`)

**O simplemente:**
Abrí http://localhost:5173 en navegador. ✓ Debe cargar (ver una página con "Login" o similar)

**Si falla:**
```bash
cd frontend
npm run dev
# Ver si hay errores en la consola
```

---

### 5️⃣ Eureka muestra 9 servicios registrados

Abrí en navegador: **http://localhost:8761**

✓ Debe cargar el dashboard  
✓ Debe mostrar "Applications" con 9 items:
  - api-gateway
  - ms-auth
  - ms-estudiantes
  - ms-instructores
  - ms-vehiculos
  - ms-asignaciones
  - ms-cobros
  - ms-reportes
  - ms-notificaciones

**Si faltan servicios:**
```bash
docker compose logs | grep "Registering"
# Ver cuáles se registraron
```

---

### 6️⃣ API Gateway responde (health check)

```bash
curl -s http://localhost:8080/actuator/health | python -m json.tool
```

✓ Debe retornar JSON con `"status":"UP"`

**Si falla:**
```bash
curl -s http://localhost:8080/actuator/health
# Ver el error exacto
```

---

### 7️⃣ Puedes LOGUEAR correctamente

**En el navegador:**
Abrí http://localhost:5173

Ingresá:
```
Email:    admin@escuela.local
Password: Admin123!
```

✓ Debe entrar al dashboard (ver menú lateral, botón de usuario, etc.)  
✓ NO debe decir "Credenciales inválidas"

**Si dice "Error de conexión":**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@escuela.local","password":"Admin123!"}'
# Ver la respuesta (debe ser JSON con token, no error)
```

---

### 8️⃣ RabbitMQ Management Console accesible

Abrí en navegador: **http://localhost:15672**

Logueá:
```
Usuario: guest
Contraseña: guest
```

✓ Debe entrar al dashboard de RabbitMQ  
✓ Debe mostrar "Queues" (aunque esté vacío es OK)

---

### 9️⃣ PostgreSQL (Adminer) accesible

Abrí en navegador: **http://localhost:8888**

Ingresá:
```
Sistema: PostgreSQL
Servidor: postgres
Usuario: escuela_user
Contraseña: <la de tu .env>
Base de datos: escuela_db
```

✓ Debe entrar a Adminer  
✓ Debe mostrarte las tablas de la BD (estructura, datos, etc.)

**Si falla:**
```bash
docker exec -it postgres psql -U escuela_user -d escuela_db -c "SELECT 1;"
# Debe retornar: 1
```

---

### 🔟 MinIO (S3) accesible

Abrí en navegador: **http://localhost:9001**

Logueá:
```
Usuario: minioadmin
Contraseña: minioadmin123
```

✓ Debe entrar al console de MinIO  
✓ Debe mostrar buckets (aunque esté vacío es OK)

---

## 🎯 RESUMEN — ¿Todos los checks pasaron?

| # | Punto | ✓ OK | ✗ Falla |
|---|-------|------|--------|
| 1 | Git status limpio | [ ] | [ ] |
| 2 | .env existe y completo | [ ] | [ ] |
| 3 | Docker: 14/14 healthy | [ ] | [ ] |
| 4 | Frontend carga (5173) | [ ] | [ ] |
| 5 | Eureka: 9 servicios | [ ] | [ ] |
| 6 | API Gateway health | [ ] | [ ] |
| 7 | Login funciona | [ ] | [ ] |
| 8 | RabbitMQ accesible | [ ] | [ ] |
| 9 | PostgreSQL accesible | [ ] | [ ] |
| 🔟 | MinIO accesible | [ ] | [ ] |

---

## ✅ Si todos pasaron

🎉 **¡EXCELENTE! Sistema está listo.**

Próximo paso:
1. Cierra este checklist
2. Abrí `2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md`
3. Léela completa (2-3 horas)
4. Cuando termines, estás listo para Sprint 12

---

## ❌ Si algo falló

### Opción 1: Revisar troubleshooting

Abrí `2_GUIA_COMPLETA/ONBOARDING_SEBASTIAN.md` → Sección "Troubleshooting rápido"

Busca tu error ahí.

### Opción 2: Reportá a Hernán

Si no encuentras solución:

Reportá **exactamente**:
- Qué punto falló (1-10)
- Error que ves (en consola o navegador)
- Comando que ejecutaste
- Output completo del error

Ej:
```
Punto 7 - Login falló:
Comando: curl -X POST http://localhost:8080/auth/login ...
Error: Connection refused
```

---

## 📞 Dudas

Si el checklist no te queda claro:
- Preguntá a Hernán
- Revisá `LEEME_PRIMERO.md` de vuelta
- Revisá `INICIO_RAPIDO_SEBASTIAN.md` paso-a-paso

---

**¡Ánimo! Si llegaste aquí, estás casi listo.** 🚀
