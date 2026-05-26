# 18. Datos seed iniciales

[← Volver al índice](../schema.md)

> Datos insertados automáticamente al ejecutar las migraciones `V1_5__Seed_Data.sql` y equivalentes por microservicio. Permiten que el sistema sea usable desde el primer arranque sin intervención manual.

---

## `auth_schema`

### Roles

| Código | Descripción |
|--------|-------------|
| `ADMIN` | Administrador del sistema, acceso total |
| `STAFF` | Personal administrativo (CRUD operacional sin configuración) |
| `INSTRUCTOR` | Docente del curso, acceso a sus asignaciones y estudiantes |
| `ESTUDIANTE` | Alumno, acceso a su perfil, clases y saldo |

### Permisos (ejemplos)

- `USUARIOS_READ`, `USUARIOS_WRITE`
- `ESTUDIANTES_READ`, `ESTUDIANTES_WRITE`, `ESTUDIANTES_DELETE`
- `INSTRUCTORES_READ`, `INSTRUCTORES_WRITE`
- `VEHICULOS_READ`, `VEHICULOS_WRITE`
- `ASIGNACIONES_READ`, `ASIGNACIONES_WRITE`
- `COBROS_READ`, `COBROS_WRITE`
- `REPORTES_READ`, `REPORTES_FINANCIEROS_READ`
- `CONFIGURACION_READ`, `CONFIGURACION_WRITE`

### Usuario administrador inicial

- **Email:** `admin@escuela.local`
- **Password:** `Admin123!` (hash bcrypt cost 10, fijado correctamente en V6 — ver `DECISIONES.md §25.4`)
- **Rol:** ADMIN

### `categorias_licencia`

| Código | Descripción |
|--------|-------------|
| A | Motocicletas hasta 200 cc |
| A1 | Motocicletas mayores de 200 cc |
| B | Vehículo particular auto pequeño |
| C | Vehículo particular auto grande / Pick-up |
| C1 | Camionetas hasta 3.5 toneladas |
| D | Buses |
| D1 | Camiones medianos |
| E | Camiones pesados / Tráiler |
| F | Vehículos especiales / Discapacitados |
| PROFESIONAL_C | Categoría C profesional |
| PROFESIONAL_D | Categoría D profesional |
| PROFESIONAL_E | Categoría E profesional |

### `conceptos_facturacion`

- Curso Básico
- Examen
- Repetición de examen
- Material didáctico

### `tipos_curso`

- Curso Básico Auto
- Curso Profesional
- Curso Moto

### `plantillas_email`

- `RECUPERAR_PASSWORD`
- `MATRICULA_CONFIRMADA`
- `RECIBO_PAGO`
- `RECORDATORIO_CLASE`
- `CLASE_REPROGRAMADA`
- `CLASE_CANCELADA`

### `configuracion_escuela` (única fila)

| Campo | Valor |
|-------|-------|
| `nombre` | "Escuela de Conducción Demo" |
| `ruc` | `1791234567001` |
| `duracion_clase_default_min` | 60 |
| `horas_recordatorio_clase` | 24 |
| `dias_alerta_soat` | 30 |
| `max_intentos_fallidos` | 3 (V4) |
| `duracion_bloqueo_minutos` | 15 (V4) |
| `expiracion_token_reset_minutos` | 60 (V4) |

---

## `vehiculos_schema`

### `tipos_combustible` (V2 — precios referenciales Ecuador 2026)

| Código | Nombre | Unidad | Precio |
|--------|--------|--------|--------|
| EXTRA | Gasolina Extra (87 octanos) | GALON | 2.4600 |
| ECOPAIS | Gasolina Ecopaís (87 oct + etanol) | GALON | 2.4600 |
| SUPER | Gasolina Súper (92 octanos) | GALON | 3.8500 |
| DIESEL | Diésel Premium | GALON | 1.8000 |
| ELECTRICO | Energía eléctrica | KWH | 0.1100 |

> El administrador puede actualizar `precio_actual` cuando suba o baje el precio público.

---

## Demo data (opcional con perfil `demo`)

Solo se insertan si se levanta el sistema con el perfil Spring `demo` activo:

- 2 usuarios staff demo
- 3 instructores demo
- 5 vehículos demo
- 10 estudiantes demo
- Facturas y pagos de ejemplo para validar reportería

> En producción, este seed **no se ejecuta** para evitar datos basura en la BD del cliente.
