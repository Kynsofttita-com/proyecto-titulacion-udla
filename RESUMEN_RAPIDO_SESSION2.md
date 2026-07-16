# ⚡ RESUMEN RÁPIDO - SESSION 2 (CHEAT SHEET)

**Objetivo:** De 85% → 100% en ~4-5 horas  
**Deliverable:** v1.0.0-ALPHA en VPS

---

## 🎯 TODO LIST SESSION 2

### PARTE 1: REPORTES CON DATOS REALES (2-3h)

**Archivo a modificar:**
```
backend/ms-reportes/src/main/java/com/escuela/reportes/service/ReporteService.java
```

**Métodos a completar:**

| # | Método | Tarea | Tiempo |
|---|--------|-------|--------|
| 1 | `generarReporteEstudiantesActivos()` | Llamar MS-Estudiantes + filtrar activos | 30m |
| 2 | `generarReporteInstructoresHoras()` | Contar horas de asignaciones | 30m |
| 3 | `generarReporteVehiculosSoat()` | Filtrar SOAT vigentes | 30m |
| 4 | `generarReporteAsistencia()` | Contar asistencias | 30m |
| 5 | `generarReporteMorosidad()` | Obtener morosos (falta en MS-Cobros) | 30m |
| 6 | `generarReporteRecibos()` | Sumar recibos emitidos | 30m |
| 7 | `generarDashboardKPIs()` | Agregar 4 KPIs principales | 30m |

**Verificación:** Tests deben pasar
```bash
mvn test -pl ms-reportes
# Esperado: 25+ tests PASSING
```

---

### PARTE 2: DEPLOY A VPS (2-3h)

**Elegir UNA opción:**

#### Opción A: Oracle Cloud (Recomendado)
```bash
# 1. Crear instancia (20 min)
   Oracle Cloud → Compute → Instances
   Ubuntu 22.04 ARM, 4 OCPUs, 24GB RAM (GRATIS)

# 2. Conectar + Docker (20 min)
   ssh -i key.key ubuntu@IP
   curl -fsSL https://get.docker.com | sudo bash
   sudo docker-compose --version

# 3. Subir proyecto (15 min)
   git clone repo
   cat > .env (JWT_SECRET, DB config)

# 4. Nginx + SSL (30 min)
   sudo apt install nginx certbot
   Crear config /etc/nginx/sites-available/proyecto
   sudo certbot certonly --nginx -d tudominio.com

# 5. Arrancar (10 min)
   docker-compose up -d
   docker-compose ps (verificar UP)
```

**Total: ~95 min (1.5h)**

#### Opción B: DigitalOcean ($6/mes)
```bash
# Más rápido: mismos pasos pero 60-80 min
# Droplet $6/month
```

**Verificación:** Health checks
```bash
curl https://api.tudominio.com/actuator/health
curl https://tudominio.com
```

---

## 📊 ESTADO ESPERADO POST-SESSION2

```
ANTES                                DESPUÉS
──────────────────────────────────────────────────
85% proyecto completado      →    93%+ proyecto completado
0 reportes en producción     →    6 reportes + KPIs en VPS
0 deploy productivo          →    v1.0.0-ALPHA en VPS
0h en producción            →    Sistema running 24/7

Microservicios: 8/8 ✅
Tests: 194/194 ✅
Dominio: https://tudominio.com ✅
API: https://api.tudominio.com ✅
SSL: Let's Encrypt vigente ✅
```

---

## 📁 ARCHIVOS A MODIFICAR

```
backend/
└── ms-reportes/
    └── src/main/java/com/escuela/reportes/service/
        └── ReporteService.java  ← AQUÍ (7 métodos)

Docker:
├── docker-compose.yml           ← Revisar (sin cambios)
└── Dockerfile.spring (cada MS)  ← Revisar (sin cambios)

Deploy:
└── VPS (nuevo, Oracle Cloud o DO)
```

---

## 🔑 PUNTOS CLAVE

### Reportes (T10.3 + T10.4)
1. **NO** crear datos fake → Llamar Feign clients reales
2. **SÍ** validar null en respuestas
3. **SÍ** registrar ejecución en cache (`registrarEjecucion()`)
4. **SÍ** agregar timestamp de generación
5. Tests: 25+ deben pasar

### Deploy
1. **Dominio:** Cambiar `tudominio.com` por el real
2. **DNS:** Apuntar dominio a IP de VPS (antes de Certbot)
3. **JWT_SECRET:** Generar nuevo de 512 bits, guardar seguro
4. **Nginx config:** Revisar health checks (puerto 8080 gateway)
5. **SSL:** Let's Encrypt auto-renew (cron diario)

---

## ⏱️ TIMELINE ESTIMADO

| Tarea | Tiempo | Inicio | Fin |
|-------|--------|--------|-----|
| T10.3.1-4 Reportes operativos | 2h | T+0:00 | T+2:00 |
| T10.4.1-3 Reportes + KPIs | 1h | T+2:00 | T+3:00 |
| Tests ms-reportes | 15m | T+3:00 | T+3:15 |
| Commit reportes | 5m | T+3:15 | T+3:20 |
| **DESCANSO** | 10m | T+3:20 | T+3:30 |
| VPS Setup (Oracle o DO) | 1.5h | T+3:30 | T+5:00 |
| Docker compose up | 10m | T+5:00 | T+5:10 |
| Nginx + SSL | 30m | T+5:10 | T+5:40 |
| Health checks | 10m | T+5:40 | T+5:50 |
| Commit deploy | 5m | T+5:50 | T+5:55 |
| **TOTAL** | **~5.5h** | | |

---

## ✅ ANTES DE EMPEZAR

- [ ] Backend compila: `mvn clean compile` ✓
- [ ] Tests pasan: `mvn test -pl ms-reportes` ✓
- [ ] Docker local funciona: `docker-compose up -d` ✓
- [ ] GitHub remoto actualizado
- [ ] Dominio disponible (registrado o usando ngrok por ahora)
- [ ] Tarjeta para VPS (si no es Oracle Free Tier)

---

## 🚀 COMANDOS RÁPIDOS

```bash
# REPORTES
mvn clean test -pl ms-reportes
mvn package -DskipTests -pl ms-reportes

# DEPLOY LOCAL
docker-compose up -d
docker-compose logs -f api-gateway
docker-compose down

# DEPLOY VPS
ssh -i key.key ubuntu@IP
docker ps
docker-compose up -d
docker-compose logs -f
curl https://api.tudominio.com/actuator/health

# GIT
git add backend/ms-reportes
git commit -m "Sprint 10 (T10.3 + T10.4): Datos reales en reportes"
git push origin main

git add .  # docker-compose changes si aplica
git commit -m "Sprint 13 (T13.7): Deploy a VPS producción"
git push origin main
```

---

## 🎯 RESULTADO FINAL

Después de Session 2, el proyecto estará:
- ✅ **85% → 93%+ completo**
- ✅ **V1.0.0-ALPHA deployed** en VPS pública
- ✅ **Reportes con datos reales** funcionando
- ✅ **194+ tests PASANDO**
- ✅ **HTTPS vigente** con Let's Encrypt
- ✅ **Listo para demostración** en vivo

**Next:** Session 3 (E2E tests + OWASP + demo video) → v1.0.0 FINAL

---

## 📞 SOPORTE RÁPIDO

**Si los reportes devuelven null:**
- Verificar que los Feign clients están inyectados
- Ver logs: `docker-compose logs ms-reportes`
- Validar que MS-Estudiantes, etc. están UP

**Si nginx no encuentra el upstream:**
- Verificar que api-gateway está en puerto 8080
- En VPS: `curl localhost:8080/actuator/health`
- Revisar `/etc/nginx/sites-available/proyecto`

**Si certificado SSL falla:**
- DNS debe estar registrado ANTES de certbot
- Verificar: `nslookup tudominio.com` → resuelve a IP VPS
- Certbot requiere puerto 80 abierto

---

**¡Listo para Session 2? Comienza cuando quieras! 🚀**
