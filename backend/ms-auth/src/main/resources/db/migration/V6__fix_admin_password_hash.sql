-- =============================================================================
-- V6: Fix del hash bcrypt del admin inicial
-- =============================================================================
-- El hash que quedo en V1_5__Seed_Data.sql para el usuario admin@escuela.local
-- NO coincidia con la contrasenia documentada "Admin123!" (probablemente fue
-- generado con una semilla erronea o copiado de otro proyecto). En ambientes
-- locales el problema no se notaba porque alguien actualizo el hash a mano,
-- pero en cualquier ambiente nuevo (CI, despliegue, otro dev) el login del
-- admin seed fallaba con 401 "Credenciales invalidas".
--
-- Esta migracion actualiza el hash al valor correcto (bcrypt cost 10 de
-- "Admin123!"). Solo afecta al admin seed; si ya existe con otra contrasenia
-- (porque alguien la cambio), la sobreescribe — el usuario debe usar la
-- contrasenia documentada o pedir un reset.
--
-- Hash generado con:
--   python3 -c "import bcrypt;
--   print(bcrypt.hashpw(b'Admin123!', bcrypt.gensalt(10)).decode())"
-- =============================================================================

UPDATE auth_schema.usuarios
   SET password = '$2b$10$YoYyQCf7wfYyUQMu8ImLzeCTeE7CUyACyS37w98DCKGFybuBl/6xq',
       updated_at = NOW(),
       updated_by = 'system'
 WHERE email = 'admin@escuela.local';
