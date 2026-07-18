-- ============================================================================
-- Datos de prueba: 3 instructores + 6 estudiantes con casos variados
-- Password de todos: Admin123!  (mismo hash que el admin)
-- ============================================================================

BEGIN;

-- ------- USUARIOS AUTH (uno por instructor + uno por estudiante) -------
INSERT INTO auth_schema.usuarios (email, password, nombre, apellido, telefono, activo, cedula, created_by)
VALUES
  -- Instructores
  ('roberto.vasquez@escuela.com',  '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Roberto', 'Vasquez Mendoza', '0987112233', true, '1710034065', 'seed'),
  ('andrea.salazar@escuela.com',   '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Andrea',  'Salazar Torres', '0998554477', true, '0926687856', 'seed'),
  ('diego.herrera@escuela.com',    '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Diego',   'Herrera Ochoa', '0976889900', true, '1712345675', 'seed'),
  -- Estudiantes
  ('camila.mora@escuela.com',      '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Camila',    'Mora Vinueza', '0991122334', true, '1723456784', 'seed'),
  ('sebastian.paz@escuela.com',    '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Sebastian', 'Paz Ortega', '0992233445', true, '0925678906', 'seed'),
  ('valeria.chavez@escuela.com',   '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Valeria',   'Chavez Aguirre', '0993344556', true, '1755443320', 'seed'),
  ('mateo.jimenez@escuela.com',    '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Mateo',     'Jimenez Quiroz', '0994455667', true, '1801122332', 'seed'),
  ('nicole.ramos@escuela.com',     '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Nicole',    'Ramos Cordero', '0995566778', true, '0602233447', 'seed'),
  ('luis.torres@escuela.com',      '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq', 'Luis',      'Torres Salgado', '0996677889', true, '1099887760', 'seed');

-- ------- ROLES: asignar INSTRUCTOR (3) y ESTUDIANTE (4) -------
INSERT INTO auth_schema.usuario_rol (usuario_id, rol_id)
SELECT u.id, 3 FROM auth_schema.usuarios u
WHERE u.email IN ('roberto.vasquez@escuela.com','andrea.salazar@escuela.com','diego.herrera@escuela.com');

INSERT INTO auth_schema.usuario_rol (usuario_id, rol_id)
SELECT u.id, 4 FROM auth_schema.usuarios u
WHERE u.email IN (
  'camila.mora@escuela.com','sebastian.paz@escuela.com','valeria.chavez@escuela.com',
  'mateo.jimenez@escuela.com','nicole.ramos@escuela.com','luis.torres@escuela.com'
);

-- ------- INSTRUCTORES -------
-- Caso 1: LICENCIA VENCIDA (rojo)
INSERT INTO instructores_schema.instructores (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento,
  licencia_numero, licencia_categoria, licencia_emision, licencia_caducidad,
  estado, fecha_contratacion, salario_mensual, tipo_contrato, horas_contrato_semanales,
  usuario_id, observaciones, created_by
)
SELECT
  '1710034065','Roberto','Vasquez Mendoza','roberto.vasquez@escuela.com','0987112233',
  'Av. 10 de Agosto N24-15, Quito','1980-03-12',
  '1710034065','B','2021-05-30','2026-05-30',  -- VENCIDA hace ~48 dias
  'ACTIVO','2020-06-01',950.00,'TIEMPO_COMPLETO',40,
  u.id,'Instructor con mas de 5 anios. Licencia PENDIENTE de renovacion.','seed'
FROM auth_schema.usuarios u WHERE u.email='roberto.vasquez@escuela.com';

-- Caso 2: LICENCIA VIGENTE (control)
INSERT INTO instructores_schema.instructores (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento,
  licencia_numero, licencia_categoria, licencia_emision, licencia_caducidad,
  estado, fecha_contratacion, salario_mensual, tipo_contrato, horas_contrato_semanales,
  usuario_id, observaciones, created_by
)
SELECT
  '0926687856','Andrea','Salazar Torres','andrea.salazar@escuela.com','0998554477',
  'Av. 9 de Octubre 1234, Guayaquil','1988-11-22',
  '0926687856','C','2024-08-20','2029-08-20',
  'ACTIVO','2024-01-15',550.00,'MEDIO_TIEMPO',20,
  u.id,'Certificacion categoria profesional. Ingles B2.','seed'
FROM auth_schema.usuarios u WHERE u.email='andrea.salazar@escuela.com';

-- Caso 3: LICENCIA PROXIMA A VENCER (7 dias)
INSERT INTO instructores_schema.instructores (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento,
  licencia_numero, licencia_categoria, licencia_emision, licencia_caducidad,
  estado, fecha_contratacion, tipo_contrato, horas_contrato_semanales, tarifa_hora,
  usuario_id, observaciones, created_by
)
SELECT
  '1712345675','Diego','Herrera Ochoa','diego.herrera@escuela.com','0976889900',
  'Calle Los Cedros 45, Quito','1985-04-08',
  '1712345675','B','2021-07-24','2026-07-24',  -- vence en 7 dias
  'ACTIVO','2022-09-01','POR_HORAS',15,12.50,
  u.id,'Recordar renovacion en agosto.','seed'
FROM auth_schema.usuarios u WHERE u.email='diego.herrera@escuela.com';

-- ------- ESTUDIANTES (6 casos variados) -------
-- Caso 1: PRE_MATRICULADO (recien registrado, sin pagar)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, tipo_curso_id, categoria_licencia_id, situacion_pago,
  usuario_id, observaciones, created_by
)
SELECT
  '1723456784','Camila','Mora Vinueza','camila.mora@escuela.com','0991122334',
  'Av. La Prensa N45-32, Quito','2005-08-14','F',
  'PRE_MATRICULADO', 1, 3, 'PENDIENTE_FACTURACION',
  u.id,'Interesada en curso basico. Aun no realiza pagos.','seed'
FROM auth_schema.usuarios u WHERE u.email='camila.mora@escuela.com';

-- Caso 2: MATRICULADO (pago matricula, aun no empieza)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, fecha_matricula, tipo_curso_id, categoria_licencia_id, situacion_pago,
  usuario_id, observaciones, created_by
)
SELECT
  '0925678906','Sebastian','Paz Ortega','sebastian.paz@escuela.com','0992233445',
  'Cdla. Kennedy Norte, Guayaquil','2003-02-27','M',
  'MATRICULADO','2026-07-10', 1, 3,'PAGADO_TOTAL',
  u.id,'Matriculado y al dia con pagos. Comienza el curso proxima semana.','seed'
FROM auth_schema.usuarios u WHERE u.email='sebastian.paz@escuela.com';

-- Caso 3: CURSANDO (activo, con horas ya cursadas - 720 min = 12h de 40h)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, fecha_matricula, tipo_curso_id, categoria_licencia_id, situacion_pago, minutos_completados,
  usuario_id, observaciones, created_by
)
SELECT
  '1755443320','Valeria','Chavez Aguirre','valeria.chavez@escuela.com','0993344556',
  'Sector El Batan, Quito','2001-05-19','F',
  'CURSANDO','2026-05-20', 1, 3,'PAGADO_TOTAL', 720,
  u.id,'Progreso constante. 12 horas cursadas de 40.','seed'
FROM auth_schema.usuarios u WHERE u.email='valeria.chavez@escuela.com';

-- Caso 4: COMPLETADO (termino el curso - 2400 min = 40h del curso)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, fecha_matricula, tipo_curso_id, categoria_licencia_id, situacion_pago, minutos_completados,
  usuario_id, observaciones, created_by
)
SELECT
  '1801122332','Mateo','Jimenez Quiroz','mateo.jimenez@escuela.com','0994455667',
  'Av. Rio Amazonas, Tena','1999-10-03','M',
  'COMPLETADO','2026-03-01', 1, 3,'PAGADO_TOTAL', 2400,
  u.id,'Curso completado. Pendiente examen ANT.','seed'
FROM auth_schema.usuarios u WHERE u.email='mateo.jimenez@escuela.com';

-- Caso 5: RETIRADO (abandono el curso)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, fecha_matricula, tipo_curso_id, categoria_licencia_id, situacion_pago, minutos_completados,
  usuario_id, observaciones, created_by
)
SELECT
  '0602233447','Nicole','Ramos Cordero','nicole.ramos@escuela.com','0995566778',
  'Av. Napo, Riobamba','2002-07-11','F',
  'RETIRADO','2026-04-15', 1, 3,'PAGO_PARCIAL', 360,
  u.id,'Abandono el curso por motivos personales. Devolucion parcial pendiente.','seed'
FROM auth_schema.usuarios u WHERE u.email='nicole.ramos@escuela.com';

-- Caso 6: CURSANDO con PAGO PARCIAL (atrasado en pagos)
INSERT INTO estudiantes_schema.estudiantes (
  cedula, nombre, apellido, email, telefono, direccion, fecha_nacimiento, genero,
  estado, fecha_matricula, tipo_curso_id, categoria_licencia_id, situacion_pago, minutos_completados,
  usuario_id, observaciones, created_by
)
SELECT
  '1099887760','Luis','Torres Salgado','luis.torres@escuela.com','0996677889',
  'Barrio San Marcos, Cuenca','1997-01-25','M',
  'CURSANDO','2026-06-01', 2, 4,'PAGO_PARCIAL', 1200,
  u.id,'Cursando categoria profesional C. Con saldo pendiente de $200.','seed'
FROM auth_schema.usuarios u WHERE u.email='luis.torres@escuela.com';

COMMIT;

-- Verificacion
SELECT 'INSTRUCTORES' AS tipo, COUNT(*) AS total FROM instructores_schema.instructores WHERE created_by='seed';
SELECT 'ESTUDIANTES' AS tipo, COUNT(*) AS total FROM estudiantes_schema.estudiantes WHERE created_by='seed';
SELECT 'USUARIOS AUTH' AS tipo, COUNT(*) AS total FROM auth_schema.usuarios WHERE created_by='seed';
