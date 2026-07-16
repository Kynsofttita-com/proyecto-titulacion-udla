# SESSION 2: SPRINT 10 REPORTES + DEPLOY A VPS

**Fecha prevista:** 2026-07-16 (tarde) / 2026-07-17 (mañana)  
**Duración estimada:** 4-5 horas  
**Objetivo:** v1.0.0-ALPHA funcional en VPS  

---

## 📋 PLAN DETALLADO SESSION 2

### FASE 1️⃣: T10.3 + T10.4 - Datos Reales en Reportes (2-3h)

#### **T10.3.1: Reporte Estudiantes Activos** (~30 min)

**Estado actual:**
```java
public ReporteOperativoResponse generarReporteEstudiantesActivos(
    CreateReporteOperativoRequest request) {
    // Devuelve datos VACÍOS
    return new ReporteOperativoResponse(
        "estudiantes_activos",
        Collections.emptyList(),
        0L, 0);
}
```

**Implementación requerida:**
```java
public ReporteOperativoResponse generarReporteEstudiantesActivos(
    CreateReporteOperativoRequest request) {
    
    // 1. Llamar MS-Estudiantes con filtros
    JsonNode estudiantesNode = estudiantesClient.listarEstudiantes(0, 1000);
    List<Map<String, Object>> estudiantes = mapearEstudiantes(estudiantesNode);
    
    // 2. Filtrar activos (estado = MATRICULADO o CURSANDO)
    List<Map<String, Object>> activos = estudiantes.stream()
        .filter(e -> {
            String estado = (String) e.get("estado");
            return "MATRICULADO".equals(estado) || "CURSANDO".equals(estado);
        })
        .toList();
    
    // 3. Registrar ejecución en cache
    registrarEjecucion("estudiantes_activos", (long) activos.size(), 
        (int) (System.currentTimeMillis() - inicio), "EXITOSO");
    
    return new ReporteOperativoResponse(
        "estudiantes_activos",
        activos,
        (long) activos.size(),
        (int) (System.currentTimeMillis() - inicio)
    );
}
```

**Cambios necesarios:**
- [ ] Actualizar generarReporteEstudiantesActivos() en ReporteService
- [ ] Mantener filtro por fechaInicio/fechaFin si aplica
- [ ] Tests: Verificar mapeo correcto

---

#### **T10.3.2: Reporte Instructores Horas** (~30 min)

**Implementación:**
```java
// 1. Listar todos los instructores
JsonNode instructoresNode = instructoresClient.listarInstructores(0, 100);
List<Map<String, Object>> instructores = mapearInstructores(instructoresNode);

// 2. Para cada instructor, contar horas de asignaciones
for (Map<String, Object> instructor : instructores) {
    Long instructorId = (Long) instructor.get("id");
    
    // Llamar MS-Asignaciones para obtener horas totales
    // asignacionesClient.obtenerHorasPorInstructor(instructorId, 
    //     request.getFechaInicio(), request.getFechaFin())
    
    instructor.put("horas_totales", horas);
    instructor.put("clases_impartidas", numClases);
}

// 3. Retornar con totales agregados
```

**Cambios necesarios:**
- [ ] Verificar que AsignacionesClient tenga método para horas por instructor
- [ ] Si no existe, crear endpoint en MS-Asignaciones
- [ ] Agregar horas_totales a respuesta

---

#### **T10.3.3: Reporte Vehículos SOAT** (~30 min)

**Implementación:**
```java
// 1. Listar vehículos
JsonNode vehiculosNode = vehiculosClient.listarVehiculos(0, 100);
List<Map<String, Object>> vehiculos = mapearVehiculos(vehiculosNode);

// 2. Filtrar por vigencia de SOAT
List<Map<String, Object>> conSoatVigente = vehiculos.stream()
    .filter(v -> {
        LocalDate fechaVencimiento = (LocalDate) v.get("soat_vencimiento");
        return fechaVencimiento.isAfter(LocalDate.now());
    })
    .toList();

// 3. Retornar con estado SOAT
for (Map<String, Object> vehiculo : conSoatVigente) {
    LocalDate vencimiento = (LocalDate) vehiculo.get("soat_vencimiento");
    long diasFaltantes = ChronoUnit.DAYS.between(LocalDate.now(), vencimiento);
    vehiculo.put("dias_soat_vigente", diasFaltantes);
}
```

**Cambios necesarios:**
- [ ] Verificar que vehículos tengan fechas SOAT
- [ ] Agregar campo dias_soat_vigente a respuesta

---

#### **T10.3.4: Reporte Asistencia** (~30 min)

**Implementación:**
```java
// 1. Obtener asignaciones completadas en rango de fechas
JsonNode asignacionesNode = asignacionesClient.listarAsignacionesCompletadas(
    request.getFechaInicio(), request.getFechaFin());

// 2. Contar asistencias por estudiante
Map<Long, Integer> asistenciasPorEstudiante = new HashMap<>();
// ... agrupar y contar

// 3. Formatear respuesta
List<Map<String, Object>> asistencias = asistenciasPorEstudiante
    .entrySet().stream()
    .map(e -> Map.of(
        "estudiante_id", e.getKey(),
        "clases_asistidas", e.getValue()
    ))
    .toList();
```

**Cambios necesarios:**
- [ ] Verificar que AsignacionesClient tenga método listarCompletadas
- [ ] Crear endpoint si no existe

---

#### **T10.4.1: Reporte Morosidad** (~30 min)

**Implementación:**
```java
// 1. Obtener todos los estudiantes con factura_cuotas
// 2. Filtrar aquellos con cuotas vencidas y no pagadas
// 3. Calcular: días de atraso, monto vencido, fecha próxima cuota

public ReporteFinancieroResponse generarReporteMorosidad(
    CreateReporteOperativoRequest request) {
    
    List<Map<String, Object>> morosos = cobrosClient
        .obtenerEstudiantesMorosos(request.getFechaInicio(), 
                                   request.getFechaFin())
        .stream()
        .map(e -> Map.of(
            "estudiante_id", e.get("estudiante_id"),
            "nombre", e.get("nombre"),
            "monto_vencido", e.get("monto_vencido"),
            "dias_atraso", e.get("dias_atraso"),
            "fecha_proxima_cuota", e.get("fecha_proxima_cuota")
        ))
        .toList();
    
    return new ReporteFinancieroResponse(
        "morosidad",
        morosos,
        (long) morosos.size()
    );
}
```

**Cambios necesarios:**
- [ ] Crear endpoint obtenerEstudiantesMorosos() en MS-Cobros
- [ ] Validar cálculo de días de atraso
- [ ] Tests unitarios

---

#### **T10.4.2: Reporte Recibos** (~30 min)

**Implementación:**
```java
// 1. Obtener recibos emitidos en período
// 2. Agrupar por mes/estado
// 3. Calcular totales: cantidad, monto, promedio

List<Map<String, Object>> recibos = cobrosClient
    .obtenerRecibos(request.getFechaInicio(), request.getFechaFin())
    .stream()
    .map(r -> Map.of(
        "numero_recibo", r.get("numero"),
        "estudiante", r.get("estudiante_nombre"),
        "monto", r.get("monto"),
        "fecha_emision", r.get("fecha_emision"),
        "estado", r.get("estado")  // PAGADO, PENDIENTE
    ))
    .toList();
```

**Cambios necesarios:**
- [ ] Verificar que MS-Cobros tenga método obtenerRecibos()
- [ ] Crear si no existe

---

#### **T10.4.3: KPIs Dashboard** (~30 min)

**Implementación:**
```java
public DashboardKPIResponse generarDashboardKPIs() {
    DashboardKPIResponse kpis = new DashboardKPIResponse();
    
    // KPI 1: Estudiantes activos
    kpis.setEstudiantesActivos(
        service.generarReporteEstudiantesActivos(...).getTotalRegistros()
    );
    
    // KPI 2: Ingresos mes actual
    ReporteFinancieroResponse ingresos = 
        service.generarReporteIngresoPeriodo(request);
    kpis.setIngresosDelMes(ingresos.getMontoTotal());
    
    // KPI 3: Cobranza %
    long morosos = service.generarReporteMorosidad(...).getTotalRegistros();
    long estudiantes = kpis.getEstudiantesActivos();
    kpis.setCobranzaPorcentaje((estudiantes - morosos) * 100 / estudiantes);
    
    // KPI 4: Deuda total
    kpis.setDeudaVencida(calcularDeudaVencida());
    
    return kpis;
}
```

**Cambios necesarios:**
- [ ] Implementar calcularDeudaVencida()
- [ ] Validar cálculos en tests
- [ ] Agregar cache con TTL 1h

---

### FASE 2️⃣: T13.7 - Deploy a VPS (2-3h)

#### **OPCIÓN A: Oracle Cloud Free Tier** (Recomendado)

**Paso 1: Crear instancia** (~20 min)
```bash
1. Oracle Cloud Console → Compute → Instances
2. Create Instance:
   - Image: Ubuntu 22.04 Minimal (ARM)
   - Shape: VM.Standard.A1.Flex (4 OCPUs, 24GB RAM - siempre gratis)
   - Network: VCN default
3. Descarga private key (.key)
4. Espera a que arranque (~2-3 min)
```

**Paso 2: Configurar SSH + Docker** (~20 min)
```bash
# Conectar (desde terminal local)
ssh -i tu_clave.key ubuntu@<ip-publica>

# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar Docker + Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar
docker --version
docker-compose --version
```

**Paso 3: Subir proyecto** (~15 min)
```bash
# En VPS
cd /opt
git clone https://github.com/tu-usuario/proyecto-titulacion.git
cd proyecto-titulacion

# Crear .env con variables
cat > .env << EOF
JWT_SECRET=tu_secret_aqui_512bits
DATABASE_URL=jdbc:postgresql://postgresql:5432/proyecto_db
RABBITMQ_HOST=rabbitmq
SPRING_PROFILES_ACTIVE=prod
EOF

# Configurar frontend URL
cat > frontend/.env.production << EOF
VITE_API_URL=https://api.tudominio.com
EOF
```

**Paso 4: Nginx + Let's Encrypt** (~30 min)
```bash
# Instalar Nginx + Certbot
sudo apt install nginx certbot python3-certbot-nginx -y

# Crear archivo de configuración
sudo tee /etc/nginx/sites-available/proyecto << EOF
server {
    server_name api.tudominio.com;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}

server {
    server_name tudominio.com www.tudominio.com;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

# Habilitar sitio
sudo ln -s /etc/nginx/sites-available/proyecto /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# Obtener certificado SSL
sudo certbot certonly --nginx \
    -d tudominio.com \
    -d www.tudominio.com \
    -d api.tudominio.com
```

**Paso 5: Iniciar docker-compose** (~10 min)
```bash
# En /opt/proyecto-titulacion
docker-compose up -d

# Verificar logs
docker-compose logs -f

# Tests básicos
curl https://api.tudominio.com/actuator/health
```

**Total Opción A:** ~95 minutos (~1.5h)

---

#### **OPCIÓN B: DigitalOcean** ($6/mes) - MÁS RÁPIDO

**Resumen:**
```
1. DigitalOcean → Droplets → Create → Ubuntu 22.04 LTS
2. $6/month droplet (1GB RAM, 1 vCPU - suficiente)
3. SSH + Docker (mismo proceso que Oracle)
4. docker-compose up -d
5. Nginx reverse proxy + Certbot

Total: ~60-80 minutos
```

**Ventajas:**
- Más rápido de provisionar
- Documentación excelente
- $6/mes (permanente)

**Desventajas:**
- Costo recurrente
- Menos recursos

---

### VERIFICACIÓN POST-DEPLOY ✓

```bash
# Health checks
curl https://api.tudominio.com/actuator/health
curl https://tudominio.com

# Logs en tiempo real
docker-compose logs -f api-gateway

# Tests de conectividad
curl -X GET https://api.tudominio.com/reportes/kpis \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Verificar todos los servicios
docker-compose ps
# Todos deben estar en "Up"
```

---

## 📋 CHECKLIST SESSION 2

### ANTES DE EMPEZAR
- [ ] Backend compilado localmente: `mvn clean package`
- [ ] Tests pasando: `mvn test`
- [ ] Docker compose local funcionando: `docker-compose up -d`
- [ ] Verificar acceso a repositorio GitHub

### DURANTE REPORTES (T10.3 + T10.4)
- [ ] T10.3.1: generarReporteEstudiantesActivos() con datos reales
- [ ] T10.3.2: generarReporteInstructoresHoras() con horas cruzadas
- [ ] T10.3.3: generarReporteVehiculosSoat() con filtro vigencia
- [ ] T10.3.4: generarReporteAsistencia() con asistencias reales
- [ ] T10.4.1: generarReporteMorosidad() con cuotas vencidas
- [ ] T10.4.2: generarReporteRecibos() con recibos emitidos
- [ ] T10.4.3: DashboardKPIs con cálculos completos
- [ ] Tests: 25+ tests en ms-reportes PASANDO
- [ ] Commit: `Sprint 10 (T10.3 + T10.4): Datos reales en reportes`

### DURANTE DEPLOY (T13.7)
- [ ] Crear instancia VPS (Oracle Cloud o DigitalOcean)
- [ ] Instalar Docker + Docker Compose
- [ ] Clonar repositorio
- [ ] Crear archivo .env con valores de producción
- [ ] Instalar Nginx + Certbot
- [ ] Configurar DNS (apuntar a IP de VPS)
- [ ] Generar certificado SSL
- [ ] Ejecutar docker-compose up -d
- [ ] Verificar que todos los servicios están UP
- [ ] Health checks: Gateway, Frontend, DB
- [ ] Commit: `Sprint 13 (T13.7): Deploy a VPS producción`

### POST-DEPLOY VERIFICATION
- [ ] Frontend accesible: https://tudominio.com
- [ ] API Gateway accesible: https://api.tudominio.com
- [ ] Login funcionando
- [ ] Reportes generando con datos reales
- [ ] Descargar PDF de reporte
- [ ] Ver notificaciones in-app
- [ ] Database con datos persistentes
- [ ] Logs sin errores críticos

---

## 🎯 RESULTADO ESPERADO

Después de Session 2:
- ✅ v1.0.0-ALPHA funcional en VPS
- ✅ Reportes con datos reales
- ✅ Acceso público via https://tudominio.com
- ✅ Certificado SSL vigente
- ✅ 194+ tests PASANDO
- ✅ Listo para demostración

**Commits esperados:** 2
1. Sprint 10 (reportes reales)
2. Sprint 13 (deploy)

**Tiempo total: 4-5 horas**

---

## 📞 REFERENCIAS

- **Docker Compose:** `/Proyecto titulacion/docker-compose.yml`
- **Endpoints:** ReporteController en ms-reportes
- **Feign Clients:** `EstudiantesClient`, `CobrosClient`, `AsignacionesClient`, etc.
- **Tests:** `backend/ms-reportes/src/test/java/...`

---

## ⏭️ SIGUIENTE SESIÓN (Session 3)

Después de completar Session 2:
- E2E tests con Cypress (T13.2)
- OWASP security audit (T13.3)
- Rate limiting Bucket4j (T13.4)
- Video demo + slides (T13.8)
- v1.0.0 FINAL

Tiempo estimado: 12-13 horas
