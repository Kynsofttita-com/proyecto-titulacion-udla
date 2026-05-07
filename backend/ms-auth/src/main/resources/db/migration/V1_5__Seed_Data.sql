-- =============================================================================
-- V1_5__Seed_Data.sql - Datos seed iniciales para MS-Auth
-- =============================================================================
-- Inserta los datos minimos necesarios para que el sistema funcione:
-- - 4 roles (ADMIN, STAFF, INSTRUCTOR, ESTUDIANTE)
-- - Permisos granulares por recurso/accion
-- - Asignacion de permisos a roles
-- - Usuario admin inicial (admin@escuela.local / Admin123!)
-- - Categorias de licencia Ecuador
-- - Conceptos de facturacion estandar
-- - Tipos de curso ofrecidos
-- - Plantillas de email transaccionales
-- - Configuracion de escuela inicial
-- =============================================================================

-- -----------------------------------------------------------------------------
-- ROLES
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.roles (nombre, descripcion, created_by) VALUES
    ('ADMIN',      'Administrador del sistema con acceso total',                       'system'),
    ('STAFF',      'Personal administrativo de la escuela (operaciones diarias)',      'system'),
    ('INSTRUCTOR', 'Instructor de la escuela (ve sus clases y alumnos)',                'system'),
    ('ESTUDIANTE', 'Estudiante matriculado (ve su perfil y progreso)',                  'system');

-- -----------------------------------------------------------------------------
-- PERMISOS (granulares por recurso/accion)
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.permisos (codigo, recurso, accion, descripcion, created_by) VALUES
    -- Usuarios
    ('USUARIOS_READ',                'USUARIOS',         'READ',   'Listar y ver usuarios',                  'system'),
    ('USUARIOS_WRITE',               'USUARIOS',         'WRITE',  'Crear y modificar usuarios',             'system'),
    ('USUARIOS_DELETE',              'USUARIOS',         'DELETE', 'Eliminar usuarios',                      'system'),

    -- Estudiantes
    ('ESTUDIANTES_READ',             'ESTUDIANTES',      'READ',   'Listar y ver estudiantes',               'system'),
    ('ESTUDIANTES_WRITE',            'ESTUDIANTES',      'WRITE',  'Crear y modificar estudiantes',          'system'),
    ('ESTUDIANTES_DELETE',           'ESTUDIANTES',      'DELETE', 'Eliminar estudiantes',                   'system'),

    -- Instructores
    ('INSTRUCTORES_READ',            'INSTRUCTORES',     'READ',   'Listar y ver instructores',              'system'),
    ('INSTRUCTORES_WRITE',           'INSTRUCTORES',     'WRITE',  'Crear y modificar instructores',         'system'),
    ('INSTRUCTORES_DELETE',          'INSTRUCTORES',     'DELETE', 'Eliminar instructores',                  'system'),

    -- Vehiculos
    ('VEHICULOS_READ',               'VEHICULOS',        'READ',   'Listar y ver vehiculos',                 'system'),
    ('VEHICULOS_WRITE',              'VEHICULOS',        'WRITE',  'Crear y modificar vehiculos',            'system'),
    ('VEHICULOS_DELETE',             'VEHICULOS',        'DELETE', 'Eliminar vehiculos',                     'system'),

    -- Asignaciones
    ('ASIGNACIONES_READ',            'ASIGNACIONES',     'READ',   'Ver asignaciones de clases',             'system'),
    ('ASIGNACIONES_WRITE',           'ASIGNACIONES',     'WRITE',  'Crear y modificar asignaciones',         'system'),
    ('ASIGNACIONES_DELETE',          'ASIGNACIONES',     'DELETE', 'Cancelar asignaciones',                  'system'),

    -- Cobros
    ('COBROS_READ',                  'COBROS',           'READ',   'Ver pagos y facturas',                   'system'),
    ('COBROS_WRITE',                 'COBROS',           'WRITE',  'Registrar pagos y crear facturas',       'system'),

    -- Reportes
    ('REPORTES_READ',                'REPORTES',         'READ',   'Generar reportes operativos',            'system'),
    ('REPORTES_FINANCIEROS_READ',    'REPORTES_FIN',     'READ',   'Generar reportes financieros',           'system'),

    -- Configuracion
    ('CONFIGURACION_READ',           'CONFIGURACION',    'READ',   'Ver configuracion del sistema',          'system'),
    ('CONFIGURACION_WRITE',          'CONFIGURACION',    'WRITE',  'Modificar configuracion del sistema',    'system'),

    -- Permisos del estudiante (sobre su propia info)
    ('MI_PERFIL_READ',               'MI_PERFIL',        'READ',   'Ver mi perfil',                          'system'),
    ('MIS_CLASES_READ',              'MIS_CLASES',       'READ',   'Ver mis clases',                         'system'),
    ('MI_SALDO_READ',                'MI_SALDO',         'READ',   'Ver mi saldo y pagos',                   'system');

-- -----------------------------------------------------------------------------
-- ASIGNACION DE PERMISOS A ROLES
-- -----------------------------------------------------------------------------

-- ADMIN: TODOS los permisos
INSERT INTO auth_schema.rol_permiso (rol_id, permiso_id, created_by)
SELECT r.id, p.id, 'system'
FROM auth_schema.roles r, auth_schema.permisos p
WHERE r.nombre = 'ADMIN';

-- STAFF: operaciones diarias (sin gestion de usuarios ni configuracion)
INSERT INTO auth_schema.rol_permiso (rol_id, permiso_id, created_by)
SELECT r.id, p.id, 'system'
FROM auth_schema.roles r, auth_schema.permisos p
WHERE r.nombre = 'STAFF'
  AND p.codigo IN (
      'ESTUDIANTES_READ', 'ESTUDIANTES_WRITE',
      'INSTRUCTORES_READ', 'INSTRUCTORES_WRITE',
      'VEHICULOS_READ', 'VEHICULOS_WRITE',
      'ASIGNACIONES_READ', 'ASIGNACIONES_WRITE', 'ASIGNACIONES_DELETE',
      'COBROS_READ', 'COBROS_WRITE',
      'REPORTES_READ', 'REPORTES_FINANCIEROS_READ',
      'CONFIGURACION_READ'
  );

-- INSTRUCTOR: solo ve su informacion
INSERT INTO auth_schema.rol_permiso (rol_id, permiso_id, created_by)
SELECT r.id, p.id, 'system'
FROM auth_schema.roles r, auth_schema.permisos p
WHERE r.nombre = 'INSTRUCTOR'
  AND p.codigo IN (
      'MI_PERFIL_READ',
      'MIS_CLASES_READ',
      'ASIGNACIONES_READ',
      'ESTUDIANTES_READ'                  -- ver datos de sus alumnos
  );

-- ESTUDIANTE: solo su propio perfil
INSERT INTO auth_schema.rol_permiso (rol_id, permiso_id, created_by)
SELECT r.id, p.id, 'system'
FROM auth_schema.roles r, auth_schema.permisos p
WHERE r.nombre = 'ESTUDIANTE'
  AND p.codigo IN (
      'MI_PERFIL_READ',
      'MIS_CLASES_READ',
      'MI_SALDO_READ'
  );

-- -----------------------------------------------------------------------------
-- USUARIO ADMIN INICIAL
-- -----------------------------------------------------------------------------
-- Email: admin@escuela.local
-- Password: Admin123!
-- BCrypt hash generado con cost=12: $2a$12$LQv3c1yqBwEHFK1ZqI3ZhuB.zR8vQ0h5aPxV3oV9yK7W8B6Eh2bWS
-- (Hash precomputado para evitar dependencia de Java en la migracion)
INSERT INTO auth_schema.usuarios (email, password, nombre, apellido, telefono, activo, created_by) VALUES
    ('admin@escuela.local',
     '$2a$12$LQv3c1yqBwEHFK1ZqI3ZhuB.zR8vQ0h5aPxV3oV9yK7W8B6Eh2bWS',
     'Administrador',
     'del Sistema',
     '0999000000',
     TRUE,
     'system');

-- Asignar rol ADMIN al usuario inicial
INSERT INTO auth_schema.usuario_rol (usuario_id, rol_id, created_by)
SELECT u.id, r.id, 'system'
FROM auth_schema.usuarios u, auth_schema.roles r
WHERE u.email = 'admin@escuela.local' AND r.nombre = 'ADMIN';

-- -----------------------------------------------------------------------------
-- CATEGORIAS DE LICENCIA ECUADOR
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.categorias_licencia (codigo, descripcion, created_by) VALUES
    ('A',              'Motocicletas y similares',                                'system'),
    ('A1',             'Tricimotos, cuadrones',                                   'system'),
    ('B',              'Vehiculos motorizados (autos, jeeps, furgonetas) - particular', 'system'),
    ('C',              'Vehiculos motorizados con capacidad >2000 kg - profesional', 'system'),
    ('C1',             'Camiones pequenos hasta 3500 kg',                          'system'),
    ('D',              'Vehiculos para transporte de pasajeros (taxis, busetas)',  'system'),
    ('D1',             'Vehiculos colectivos hasta 22 pasajeros',                  'system'),
    ('E',              'Vehiculos pesados articulados (trailers)',                 'system'),
    ('F',              'Vehiculos para personas con discapacidad',                 'system'),
    ('PROFESIONAL_C',  'Profesional categoria C',                                  'system'),
    ('PROFESIONAL_D',  'Profesional categoria D',                                  'system'),
    ('PROFESIONAL_E',  'Profesional categoria E',                                  'system');

-- -----------------------------------------------------------------------------
-- CONCEPTOS DE FACTURACION
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.conceptos_facturacion (nombre, monto_base, descripcion, created_by) VALUES
    ('Curso Basico',           250.00, 'Curso basico de conduccion (categoria B)',      'system'),
    ('Curso Profesional',      350.00, 'Curso profesional (categorias C, D, E)',        'system'),
    ('Curso Moto',             180.00, 'Curso de motocicleta (categoria A)',            'system'),
    ('Examen Practico',         30.00, 'Examen practico de manejo',                     'system'),
    ('Repeticion de Examen',    20.00, 'Repeticion de examen reprobado',                'system'),
    ('Material Didactico',      15.00, 'Libro de teoria + material de estudio',         'system'),
    ('Clase Adicional',         12.00, 'Clase practica adicional (60 minutos)',         'system');

-- -----------------------------------------------------------------------------
-- TIPOS DE CURSO
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.tipos_curso (nombre, descripcion, duracion_total_horas, precio_base, categoria_licencia_id, created_by)
SELECT 'Curso Basico Auto',
       'Curso completo para licencia categoria B (vehiculos particulares)',
       40, 250.00,
       cl.id, 'system'
FROM auth_schema.categorias_licencia cl WHERE cl.codigo = 'B';

INSERT INTO auth_schema.tipos_curso (nombre, descripcion, duracion_total_horas, precio_base, categoria_licencia_id, created_by)
SELECT 'Curso Profesional C',
       'Curso para licencia profesional categoria C (vehiculos pesados)',
       60, 350.00,
       cl.id, 'system'
FROM auth_schema.categorias_licencia cl WHERE cl.codigo = 'C';

INSERT INTO auth_schema.tipos_curso (nombre, descripcion, duracion_total_horas, precio_base, categoria_licencia_id, created_by)
SELECT 'Curso Moto',
       'Curso para licencia categoria A (motocicletas)',
       30, 180.00,
       cl.id, 'system'
FROM auth_schema.categorias_licencia cl WHERE cl.codigo = 'A';

-- -----------------------------------------------------------------------------
-- PLANTILLAS DE EMAIL (cuerpo simplificado, se mejorara desde el admin)
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.plantillas_email (codigo, asunto, cuerpo_html, variables, created_by) VALUES
    ('RECUPERAR_PASSWORD',
     'Recupera tu contrasena - Escuela de Conduccion',
     '<h1>Hola {{nombre}}</h1><p>Has solicitado recuperar tu contrasena. Haz click en el siguiente enlace:</p><p><a href="{{enlace}}">Restablecer contrasena</a></p><p>Este enlace expira en 1 hora.</p>',
     '["nombre", "enlace"]'::jsonb,
     'system'),

    ('MATRICULA_CONFIRMADA',
     'Bienvenido - Tu matricula esta confirmada',
     '<h1>Hola {{nombre}}</h1><p>Tu matricula en el curso <strong>{{curso}}</strong> ha sido confirmada.</p><p>Te enviaremos detalles de tus clases pronto.</p>',
     '["nombre", "curso"]'::jsonb,
     'system'),

    ('RECIBO_PAGO',
     'Comprobante de pago - {{numero_factura}}',
     '<h1>Hola {{nombre}}</h1><p>Confirmamos el pago de <strong>${{monto}}</strong> por concepto de <strong>{{concepto}}</strong>.</p><p>Numero de factura: {{numero_factura}}</p><p>Saldo pendiente: ${{saldo}}</p>',
     '["nombre", "monto", "concepto", "numero_factura", "saldo"]'::jsonb,
     'system'),

    ('RECORDATORIO_CLASE',
     'Recordatorio - Tu clase es manana',
     '<h1>Hola {{nombre}}</h1><p>Te recordamos que tienes una clase programada manana <strong>{{fecha_hora}}</strong> con el instructor <strong>{{instructor}}</strong>.</p><p>Vehiculo: {{vehiculo}}</p><p>Por favor, llega 10 minutos antes.</p>',
     '["nombre", "fecha_hora", "instructor", "vehiculo"]'::jsonb,
     'system'),

    ('CLASE_REPROGRAMADA',
     'Tu clase ha sido reprogramada',
     '<h1>Hola {{nombre}}</h1><p>Tu clase del <strong>{{fecha_anterior}}</strong> ha sido reprogramada para el <strong>{{fecha_nueva}}</strong>.</p><p>Motivo: {{motivo}}</p>',
     '["nombre", "fecha_anterior", "fecha_nueva", "motivo"]'::jsonb,
     'system'),

    ('CLASE_CANCELADA',
     'Tu clase ha sido cancelada',
     '<h1>Hola {{nombre}}</h1><p>Lamentamos informarte que tu clase del <strong>{{fecha_hora}}</strong> ha sido cancelada.</p><p>Motivo: {{motivo}}</p><p>Por favor, contactanos para reprogramar.</p>',
     '["nombre", "fecha_hora", "motivo"]'::jsonb,
     'system');

-- -----------------------------------------------------------------------------
-- CONFIGURACION INICIAL DE LA ESCUELA (placeholder editable desde admin)
-- -----------------------------------------------------------------------------
INSERT INTO auth_schema.configuracion_escuela (
    nombre, ruc, direccion, telefono, email,
    color_primario, color_secundario,
    duracion_clase_default_min, horario_apertura, horario_cierre,
    horas_recordatorio_clase, dias_alerta_soat,
    created_by
) VALUES (
    'Escuela de Conduccion Demo',
    '1791234567001',
    'Av. Principal 123, Quito - Ecuador',
    '022345678',
    'info@escuela.local',
    '#1F4E78',
    '#4472C4',
    60,
    '08:00:00',
    '18:00:00',
    24,
    30,
    'system'
);
