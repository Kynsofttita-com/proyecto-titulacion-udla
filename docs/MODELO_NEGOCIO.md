# Modelo de Negocio - Sistema de Gestión Escuelas de Conducción

> **Documento:** Diagramas y documentación del modelo conceptual del sistema
> **Versión:** 1.0
> **Última actualización:** 2026-07-12
> **Ver diagramas interactivos:** [diagrama_modelo.html](./diagrama_modelo.html)

---

## Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Entidades Principales](#entidades-principales)
3. [Flujo de Negocio Completo](#flujo-de-negocio-completo)
4. [Arquitectura de Microservicios](#arquitectura-de-microservicios)
5. [Flujos Específicos](#flujos-específicos)
6. [Matriz de Datos por Servicio](#matriz-de-datos-por-servicio)
7. [Diccionario de Entidades](#diccionario-de-entidades)

---

## Visión General

El sistema es un **ERP (Enterprise Resource Planning) especializado** para administrar:
- **Operaciones:** Estudiantes, instructores, vehículos, clases
- **Finanzas:** Facturas, pagos, saldos, morosidad
- **Reportes:** Analítica operativa y financiera
- **Comunicaciones:** Notificaciones por email

**Modelo de negocio:** Single-tenant configurable (1 deploy por escuela)

**Principio arquitectónico:** Cada área tiene un microservicio independiente con su propia base de datos

---

## Entidades Principales

### 1. **ESTUDIANTES**
Persona que se inscribe en la escuela para obtener su licencia de conducir.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| cedula | VARCHAR(10) | Cédula de identidad ecuatoriana (10 dígitos) |
| nombre | VARCHAR | Primer nombre |
| apellido | VARCHAR | Apellidos |
| email | VARCHAR | Email de contacto |
| telefono | VARCHAR(10) | Teléfono móvil (09XXXXXXXX) |
| fecha_matricula | DATE | Fecha de inscripción en la escuela |
| estado | VARCHAR | ACTIVO, INACTIVO, EGRESADO, SUSPENDIDO |
| horas_programadas | INTEGER | Total de horas de clase |
| horas_completadas | INTEGER | Horas completadas hasta el momento |

**Relaciones:**
- Crea múltiples **Asignaciones** (clases)
- Genera múltiples **Facturas** (pagos)
- Realiza múltiples **Pagos** (abonos)

---

### 2. **INSTRUCTORES**
Profesional certificado que imparte las clases de conducción.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| cedula | VARCHAR(10) | Cédula de identidad |
| nombre | VARCHAR | Primer nombre |
| apellido | VARCHAR | Apellidos |
| email | VARCHAR | Email institucional |
| licencia_numero | VARCHAR | Número de licencia de conducir |
| licencia_tipo | VARCHAR | Tipo de licencia (A, B, C, etc.) |
| licencia_fecha_vencimiento | DATE | Fecha de vencimiento de licencia |
| estado | VARCHAR | ACTIVO, INACTIVO, DE_BAJA |
| horas_totales | INTEGER | Horas de enseñanza impartidas |

**Relaciones:**
- Imparte múltiples **Asignaciones** (clases)
- Posee múltiples **Certificaciones** (cursos)

---

### 3. **VEHÍCULOS**
Automóvil de la flota que se utiliza en las clases de conducción.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| placa | VARCHAR(8) | Placa del vehículo (ABC-1234 o AB-1234X) |
| marca | VARCHAR | Marca (Toyota, Hyundai, etc.) |
| modelo | VARCHAR | Modelo |
| año | SMALLINT | Año de fabricación |
| color | VARCHAR | Color |
| tipo_combustible | VARCHAR | GASOLINA, DIESEL, ELÉCTRICO |
| fecha_soat | DATE | Fecha de vencimiento del SOAT |
| fecha_inspeccion_tecnica | DATE | Fecha de vencimiento de inspección técnica |
| kilometraje | INTEGER | Kilómetros actuales |
| estado | VARCHAR | DISPONIBLE, EN_USO, MANTENIMIENTO, FUERA_DE_SERVICIO |

**Relaciones:**
- Se asigna en múltiples **Asignaciones** (clases)
- Requiere múltiples **Mantenimientos**
- Genera múltiples **Alertas de SOAT**

---

### 4. **ASIGNACIONES**
Registro de una clase programada (Estudiante + Instructor + Vehículo + Fecha/Hora).

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| estudiante_id | BIGINT | FK → Estudiante |
| instructor_id | BIGINT | FK → Instructor |
| vehiculo_id | BIGINT | FK → Vehículo |
| fecha_hora | TIMESTAMP | Fecha y hora de inicio de la clase |
| duracion_minutos | SMALLINT | Duración en minutos (60, 90, 120) |
| tipo_clase | VARCHAR | TEORÍA, PRÁCTICA, EXAMEN |
| estado | VARCHAR | PROGRAMADA, EN_CURSO, COMPLETADA, CANCELADA |
| lugar_partida | VARCHAR | Punto de inicio |
| observaciones | TEXT | Notas del instructor |

**Validaciones al crear:**
- Estudiante debe estar ACTIVO
- Instructor debe estar ACTIVO y disponible en esa fecha/hora
- Vehículo debe estar DISPONIBLE y con SOAT vigente
- No puede haber conflicto de horarios (mismo instructor o vehículo en ese tiempo)

---

### 5. **FACTURAS**
Documento contable que registra lo adeudado por un estudiante.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| estudiante_id | BIGINT | FK → Estudiante |
| numero_factura | VARCHAR | Número único secuencial |
| fecha_emision | DATE | Fecha de creación |
| monto_original | NUMERIC(10,2) | Monto total en USD |
| monto_pagado | NUMERIC(10,2) | Total pagado hasta ahora |
| monto_pendiente | NUMERIC(10,2) | Lo que falta pagar (original - pagado) |
| estado | VARCHAR | PENDIENTE, PARCIALMENTE_PAGADA, PAGADA, ANULADA |
| descripcion | TEXT | Concepto: "100 horas de conducción", etc. |

**Lógica:**
- Se auto-genera cuando estudiante completa ciclo de clases
- Una factura puede tener múltiples **Pagos** parciales
- El estado se actualiza automáticamente según los pagos

---

### 6. **PAGOS**
Transacción individual de abono a una factura.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| factura_id | BIGINT | FK → Factura |
| monto | NUMERIC(10,2) | Monto pagado en esta transacción |
| fecha_pago | DATE | Fecha del pago |
| metodo_pago | VARCHAR | EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE |
| numero_referencia | VARCHAR | Número de comprobante/referencia |
| estado | VARCHAR | PROCESADO, ANULADO |

**Características:**
- Append-only (no se puede editar, solo anular)
- Inmediato: cuando se registra, actualiza factura automáticamente
- Trazabilidad completa para auditoría financiera

---

### 7. **MANTENIMIENTOS**
Registro de servicios de mantenimiento o reparación en vehículos.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| vehiculo_id | BIGINT | FK → Vehículo |
| tipo | VARCHAR | PREVENTIVO, CORRECTIVO, INSPECCIÓN |
| descripcion | TEXT | Detalle del trabajo realizado |
| fecha_programada | DATE | Cuándo se debe hacer |
| fecha_realizado | DATE | Cuándo se completó |
| costo | NUMERIC(10,2) | Gasto en USD |
| estado | VARCHAR | PENDIENTE, EN_PROCESO, COMPLETADO, CANCELADO |
| observaciones | TEXT | Notas del técnico |

---

### 8. **CERTIFICACIONES**
Cursos, certificaciones o habilitaciones que posee un instructor.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| instructor_id | BIGINT | FK → Instructor |
| tipo | VARCHAR | CAPACITACIÓN, LICENCIA, HABILITACIÓN, CURSO |
| descripcion | TEXT | Nombre del curso/certificación |
| fecha_obtencion | DATE | Cuándo se obtuvo |
| fecha_vencimiento | DATE | Vencimiento (NULL si no aplica) |
| estado | VARCHAR | VIGENTE, VENCIDA, SUSPENDIDA |

---

### 9. **ALERTAS_SOAT**
Alertas automáticas de vencimiento cercano del SOAT de vehículos.

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | Identificador único |
| vehiculo_id | BIGINT | FK → Vehículo |
| dias_para_vencer | INTEGER | Días restantes (negativo si ya venció) |
| estado | VARCHAR | ALERTA, CRITICA, VENCIDO, RESUELTO |
| fecha_generada | TIMESTAMP | Cuándo se generó la alerta |

---

## Flujo de Negocio Completo

```
┌─────────────────────────────────────────────────────────────────┐
│ FASE 1: INSCRIPCIÓN Y REGISTRO                                  │
└─────────────────────────────────────────────────────────────────┘

1. Estudiante se registra en el sistema
   └─→ MS-Auth: Crea usuario con rol ESTUDIANTE
   └─→ MS-Estudiantes: Crea perfil académico
   └─→ Email: Confirmación de inscripción

2. Escuela aprueba estudiante
   └─→ Estado: ACTIVO
   └─→ Se puede programar clases


┌─────────────────────────────────────────────────────────────────┐
│ FASE 2: PROGRAMACIÓN Y EJECUCIÓN DE CLASES                      │
└─────────────────────────────────────────────────────────────────┘

1. Estudiante / Admin solicita clase
   └─→ MS-Asignaciones: Validar disponibilidad
       ├─→ ¿Estudiante activo? → MS-Estudiantes
       ├─→ ¿Instructor disponible? → MS-Instructores
       └─→ ¿Vehículo disponible y SOAT vigente? → MS-Vehículos

2. Clase se crea si todas validaciones OK
   └─→ Estado: PROGRAMADA
   └─→ Email al estudiante: Confirmación

3. En fecha/hora programada:
   └─→ Instructor toma vehículo
   └─→ Realiza clase con estudiante
   └─→ Registra observaciones

4. Clase finaliza
   └─→ Estado: COMPLETADA
   └─→ Registro de asistencia
   └─→ Email: Confirmación al estudiante


┌─────────────────────────────────────────────────────────────────┐
│ FASE 3: FACTURACIÓN Y COBROS                                    │
└─────────────────────────────────────────────────────────────────┘

1. Cuando estudiante completa ciclo (ej: 100 horas):
   └─→ MS-Cobros: Auto-genera FACTURA
   └─→ Monto = precio por hora × total de horas
   └─→ Estado: PENDIENTE
   └─→ Email: Solicitud de pago

2. Estudiante realiza PAGO:
   └─→ MS-Cobros: Registra transacción
   └─→ Actualiza monto_pagado y monto_pendiente
   └─→ Si monto_pendiente > 0:
       └─→ Estado Factura: PARCIALMENTE_PAGADA
   └─→ Si monto_pendiente = 0:
       └─→ Estado Factura: PAGADA
   └─→ Email: Comprobante de pago

3. Si factura vence sin pago:
   └─→ MS-Cobros: Reporte de morosidad
   └─→ Email: Recordatorio de deuda


┌─────────────────────────────────────────────────────────────────┐
│ FASE 4: EGRESO O SUSPENSIÓN                                     │
└─────────────────────────────────────────────────────────────────┘

1. Si estudiante completó programa:
   └─→ Estado: EGRESADO
   └─→ Emite certificado
   └─→ Email: Confirmación de egreso

2. Si estudiante tiene deuda pendiente:
   └─→ Estado: SUSPENDIDO
   └─→ No se pueden programar más clases
   └─→ Email: Aviso de suspensión

3. Si estudiante se retira voluntariamente:
   └─→ Estado: INACTIVO
```

---

## Arquitectura de Microservicios

### Estructura General

```
┌─────────────────────────────────────────────────────────────────┐
│                          FRONTEND (Vue.js 3)                    │
│                  Interfaz SPA Responsiva                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API GATEWAY (Spring Cloud)                 │
│  ├─ Routing inteligente por endpoint                            │
│  ├─ Validación JWT en cada request                              │
│  ├─ Rate limiting                                               │
│  └─ CORS handling                                               │
└────────────────────┬────────────────────────────────────────────┘
                     │
       ┌─────────────┼─────────────┐
       │             │             │
       ▼             ▼             ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │ MS-Auth  │ │ MS-Estud │ │MS-Instrc │  ... más microservicios
   └──────┬───┘ └─────┬────┘ └────┬─────┘
          │           │           │
          └─────┬─────┴─────┬─────┘
                │           │
                ▼           ▼
          ┌──────────────────────┐
          │   PostgreSQL (1 BD)  │
          │   9 Schemas          │
          │   Schema per MS      │
          └──────────────────────┘

   + Eureka (Service Discovery)
   + RabbitMQ (Mensajería asincrónica)
   + Caffeine (Caching local)
```

### Microservicios y Responsabilidades

| Servicio | Puerto | Responsabilidades |
|----------|--------|-------------------|
| **MS-Auth** | 8081 | JWT, roles, usuarios, auditoría de login |
| **MS-Estudiantes** | 8082 | Perfiles, documentos, progreso académico |
| **MS-Instructores** | 8083 | Perfiles, certificaciones, disponibilidad |
| **MS-Vehículos** | 8085 | Flota, SOAT, mantenimiento, combustible |
| **MS-Asignaciones** | 8084 | Scheduling de clases, tripartite validations |
| **MS-Cobros** | 8086 | Facturas, pagos, saldos, morosidad |
| **MS-Reportes** | 8087 | Analítica operativa, financiera, exportaciones |
| **MS-Notificaciones** | 8088 | Email, plantillas, preferencias de notificación |
| **API Gateway** | 8080 | Enrutamiento y seguridad |

### Patrones de Comunicación

**Sincrónico (REST + Feign):**
- Estudiante solicita clase → Validar disponibilidad en tiempo real
- Generar factura → Consultar nombre de estudiante

**Asincrónico (RabbitMQ):**
- Usuario creado en Auth → Crear notificación in-app
- Clase completada → Publicar evento para generar factura
- Factura creada → Enviar email de cobro

---

## Flujos Específicos

### Flujo 1: Crear Asignación (Clase)

```
Frontend
   │
   ├─→ POST /asignaciones
           ├─ estudiante_id = 123
           ├─ instructor_id = 456
           ├─ vehiculo_id = 789
           └─ fecha_hora = 2026-07-15 14:00
   │
   ▼
API Gateway (valida JWT)
   │
   ▼
MS-Asignaciones Controller
   │
   ├─→ EstudiantesClient.obtener(123)
   │       └─→ ¿Existe? ¿ACTIVO?
   │
   ├─→ InstructoresClient.verificarDisponibilidad(456, fecha_hora)
   │       └─→ ¿Disponible?
   │
   ├─→ VehiculosClient.obtener(789)
   │       └─→ ¿Disponible? ¿SOAT vigente?
   │
   ├─→ [Si todo OK] Crear AsignacionEntity
   │
   ├─→ Publicar evento: "asignacion.creada"
   │       └─→ RabbitMQ
   │
   └─→ Retornar 201 + AsignacionResponse

RabbitMQ Broker
   │
   ├─→ MS-Notificaciones consume "asignacion.creada"
   │       └─→ Genera email
   │       └─→ Envía a estudiante
   │
   └─→ MS-Instructores consume "asignacion.creada"
           └─→ Actualiza disponibilidad horaria
```

### Flujo 2: Registrar Pago

```
Frontend (MS-Cobros Admin)
   │
   ├─→ POST /pagos
           ├─ factura_id = 100
           ├─ monto = 600.00
           ├─ metodo_pago = TRANSFERENCIA
           └─ numero_referencia = TRANS123456
   │
   ▼
API Gateway
   │
   ▼
MS-Cobros Controller
   │
   ├─→ Obtener factura_id=100
   │       └─→ monto_original = 2000.00
   │       └─→ monto_pagado = 1400.00 (de pagos anteriores)
   │       └─→ monto_pendiente = 600.00
   │
   ├─→ Crear PagoEntity
   │
   ├─→ Actualizar Factura:
   │       └─→ monto_pagado = 1400.00 + 600.00 = 2000.00
   │       └─→ monto_pendiente = 0.00
   │       └─→ estado = PAGADA
   │
   ├─→ Publicar evento: "factura.pagada"
   │
   └─→ Retornar 201 + PagoResponse

RabbitMQ
   │
   ├─→ MS-Notificaciones consume "factura.pagada"
   │       └─→ Genera recibo en PDF
   │       └─→ Envía email al estudiante
   │
   └─→ MS-Reportes consume "factura.pagada"
           └─→ Actualiza KPIs de ingresos
```

---

## Matriz de Datos por Servicio

```
┌──────────────┬──────────────────┬────────────────────┐
│ Microservicio│ Tabla Principal  │ Datos Relacionados │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Auth      │ usuarios         │ roles, permisos    │
│              │ refresh_tokens   │ auditoría login    │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Estud     │ estudiantes      │ documentos, estado │
│              │ progreso_acad    │ horas completadas  │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Instrc    │ instructores     │ certificaciones    │
│              │ disponibilidad   │ licencias          │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Vehícul   │ vehiculos        │ SOAT, mantenimiento│
│              │ combustible      │ alertas            │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Asigna    │ asignaciones     │ confirmaciones,    │
│              │                  │ cambios            │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Cobros    │ facturas         │ pagos, saldos      │
│              │ morosidad        │ reconciliación     │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Report    │ ejecucion_rep    │ cache_reporte      │
│              │ (solo lectura)   │ (caching)          │
├──────────────┼──────────────────┼────────────────────┤
│ MS-Notif     │ notificaciones   │ plantillas,        │
│              │ log_envio_email  │ preferencias       │
└──────────────┴──────────────────┴────────────────────┘
```

---

## Diccionario de Entidades

### Estados de Entidades

#### Estudiante
- **ACTIVO:** Inscrito y puede tomar clases
- **INACTIVO:** No paga / solicita suspensión
- **SUSPENDIDO:** Tiene deuda pendiente
- **EGRESADO:** Completó programa
- **BAJA:** Canceló inscripción

#### Instructor
- **ACTIVO:** Disponible para impartir clases
- **INACTIVO:** De vacaciones / temporal
- **DE_BAJA:** Cesado o renunció

#### Vehículo
- **DISPONIBLE:** Puede ser asignado
- **EN_USO:** Actualmente en clase
- **MANTENIMIENTO:** En servicio técnico
- **FUERA_DE_SERVICIO:** Dañado o sin SOAT

#### Asignación
- **PROGRAMADA:** Clase agendada, no iniciada
- **EN_CURSO:** Clase en progreso
- **COMPLETADA:** Finalizada correctamente
- **CANCELADA:** No se realizó

#### Factura
- **PENDIENTE:** Sin pagos
- **PARCIALMENTE_PAGADA:** Algunos pagos recibidos
- **PAGADA:** Saldo = 0
- **ANULADA:** Cancelada/devuelta

---

## Validaciones Clave

### Al crear Asignación:
1. Estudiante existe y estado = ACTIVO
2. Instructor existe y estado = ACTIVO
3. Vehículo existe y estado = DISPONIBLE
4. SOAT del vehículo vence en > 7 días
5. Licencia del instructor vence en > 0 días
6. No hay conflicto de horarios (mismo instructor o vehículo)
7. Fecha/hora está en el futuro

### Al registrar Pago:
1. Factura existe y estado ≠ ANULADA
2. Monto > 0
3. Monto ≤ monto_pendiente
4. Fecha_pago ≤ HOY
5. Actualizar monto_pagado y estado automáticamente

### Al crear Factura:
1. Estudiante existe
2. Monto > 0
3. Generar numero_factura secuencial único
4. Timestamp = NOW()

---

## Notas Importantes

### Single-Tenant:
- 1 deploy = 1 escuela
- Datos completamente aislados por instancia
- Cada escuela tiene su propia BD en su propia VPC

### Consistencia Eventual:
- Los datos entre microservicios no están garantizados ser 100% consistentes en todo momento
- Se usan eventos RabbitMQ para sincronización asincrónica
- Para operaciones críticas, se valida en tiempo real vía Feign

### Auditoría:
- Todas las transacciones financieras quedan en `audit_log`
- Pagos son append-only (no se pueden editar)
- Se registran: usuario, timestamp, acción, antes/después

---

## Referencias

- [Schema completo](./database/schema.md)
- [Decisiones técnicas](../DECISIONES.md)
- [Plan de Sprints](../SPRINTS_PLAN.xlsx)
- [Diagramas Interactivos](./diagrama_modelo.html)
