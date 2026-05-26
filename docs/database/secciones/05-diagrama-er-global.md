# 5. Diagrama ER global

[← Volver al índice](../schema.md)

> Vista de conjunto: todas las entidades del sistema y sus relaciones. Para el detalle de atributos de cada entidad, ver el archivo de su schema correspondiente.

---

## Diagrama Mermaid — Vista de núcleo (entidades principales)

> Diagrama simplificado con las 9 entidades centrales del sistema y sus relaciones principales. Para el detalle exhaustivo de tablas y FKs dentro de cada schema, ver el archivo de cada schema individual.

```mermaid
erDiagram
    USUARIOS ||--o{ ESTUDIANTES : "ref"
    USUARIOS ||--o{ INSTRUCTORES : "ref"
    ESTUDIANTES ||--o{ ASIGNACIONES : "asiste"
    INSTRUCTORES ||--o{ ASIGNACIONES : "dicta"
    VEHICULOS ||--o{ ASIGNACIONES : "usa"
    ESTUDIANTES ||--o{ FACTURAS : "recibe"
    FACTURAS ||--o{ PAGOS : "registra"
    FACTURAS ||--o{ FACTURA_CUOTAS : "se_divide"
    USUARIOS ||--o{ NOTIFICACIONES : "recibe"
```

### Diagrama detallado por sub-dominio

Para no sobrecargar un solo diagrama (los renderers Mermaid tienen problemas con +30 nodos), el ER completo está dividido por sub-dominios:

**Sub-dominio Auth + Usuarios:**

```mermaid
erDiagram
    USUARIOS ||--o{ USUARIO_ROL : "tiene"
    ROLES ||--o{ USUARIO_ROL : "asignado"
    ROLES ||--o{ ROL_PERMISO : "tiene"
    PERMISOS ||--o{ ROL_PERMISO : "incluido"
    USUARIOS ||--o{ REFRESH_TOKENS : "emite"
    USUARIOS ||--o{ PASSWORD_RESET_TOKEN : "solicita"
```

**Sub-dominio Estudiantes:**

```mermaid
erDiagram
    ESTUDIANTES ||--o{ DOCUMENTOS : "tiene"
    ESTUDIANTES ||--o{ CONTACTOS_EMERGENCIA : "tiene"
    ESTUDIANTES ||--|| PROGRESO_ACADEMICO : "tiene"
    ESTUDIANTES ||--o{ ASISTENCIA : "registra"
```

**Sub-dominio Instructores:**

```mermaid
erDiagram
    INSTRUCTORES ||--o{ CERTIFICACIONES : "tiene"
    INSTRUCTORES ||--o{ DISPONIBILIDAD : "define"
    INSTRUCTORES ||--o{ HORARIOS_TRABAJO : "tiene"
```

**Sub-dominio Vehículos:**

```mermaid
erDiagram
    TIPOS_COMBUSTIBLE ||--o{ VEHICULOS : "usa"
    VEHICULOS ||--o{ MANTENIMIENTOS : "registra"
    VEHICULOS ||--o{ REGISTROS_COMBUSTIBLE : "consume"
    VEHICULOS ||--o{ INSPECCIONES : "tiene"
    VEHICULOS ||--o{ DOCUMENTOS_VEHICULO : "tiene"
```

**Sub-dominio Asignaciones:**

```mermaid
erDiagram
    ASIGNACIONES ||--o{ CAMBIOS_ASIGNACION : "tracks"
    ASIGNACIONES ||--o{ HISTORIAL_ESTADOS : "logs"
```

**Sub-dominio Cobros:**

```mermaid
erDiagram
    FACTURAS ||--o{ FACTURA_CUOTAS : "tiene"
    FACTURAS ||--o{ PAGOS : "recibe"
    FACTURA_CUOTAS ||--o{ PAGOS : "vincula"
```

**Sub-dominio Notificaciones:**

```mermaid
erDiagram
    USUARIOS ||--|| PREFERENCIAS_NOTIFICACION : "configura"
    USUARIOS ||--o{ NOTIFICACIONES : "recibe"
```

---

## Interpretación

- Las relaciones con label `ref_*` (ej. `ref_usuario_id`, `ref_estudiante_id`) representan **referencias cross-schema** entre microservicios. Estas relaciones NO tienen FK física a nivel BD; la consistencia se gestiona mediante eventos RabbitMQ y/o llamadas Feign.
- El resto de relaciones son **FKs reales** dentro del mismo schema, con `ON DELETE CASCADE` cuando aplica.
- Las cardinalidades usan notación crow's foot:
  - `||--||` uno a uno (ej. estudiante ↔ progreso académico)
  - `||--o{` uno a muchos (ej. estudiante → documentos)

---

## Versión alternativa en DBML

Si el renderer Mermaid local no funciona, está disponible el modelo equivalente en formato DBML para [dbdiagram.io](https://dbdiagram.io):

- Archivo: [`../er-diagram.dbml`](../er-diagram.dbml)
- Uso: copiar contenido del archivo, pegar en dbdiagram.io, exportar a PNG/SVG/PDF.
