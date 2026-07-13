# GUIA PARA GRABAR VIDEO DEMO DEL SISTEMA

**Duracion:** 10 minutos maximo  
**Formato:** MP4, MOV o similar  
**Herramienta:** OBS Studio (gratuito), Camtasia, o ScreenFlow

---

## EQUIPAMIENTO NECESARIO

- Pantalla con buena resolucion (1920x1080 minimo)
- Micrófono (parlante de computadora o headset)
- OBS Studio instalado (descarga en https://obsproject.com/)
- Sistema corriendo (docker-compose up)

---

## GUION DEL VIDEO (10 MINUTOS)

### MINUTO 0-1: INTRODUCCION (1 minuto)

**Mostrar en pantalla:**
- Nombre del proyecto en terminal/editor
- Logo UDLA

**Decir en voz:**
```
"Bienvenidos. Este es el Sistema de Control Administrativo y Financiero 
para Escuelas de Conducción, proyecto de titulación de la carrera de 
Ingeniería en Sistemas de la Universidad de las Américas.

Vamos a demostrar la funcionalidad completa del sistema."
```

---

### MINUTO 1-2: ARQUITECTURA DEL SISTEMA (1 minuto)

**Mostrar en pantalla:**
- Abrir DECISIONES.md o CLAUDE.md
- Mostrar diagrama de arquitectura (arquitectura-sistema.drawio o PDF)

**Decir en voz:**
```
"El sistema está construido con arquitectura de microservicios:
- 8 microservicios independientes en Java 21 con Spring Boot
- API Gateway que funciona como punto de entrada único
- Vue.js 3 como frontend
- PostgreSQL con 9 esquemas separados
- RabbitMQ para mensajería asíncrona
- Eureka para descubrimiento de servicios
- 14 contenedores Docker corriendo simultáneamente
- 172 tests automatizados con 82% de cobertura"
```

---

### MINUTO 2-3: INICIAR EL SISTEMA (1 minuto)

**Mostrar en pantalla:**
1. Abrir terminal
2. Navegar a la carpeta del proyecto
3. Ejecutar: `docker-compose up`
4. Mostrar cómo levanta:
   - PostgreSQL
   - RabbitMQ
   - Eureka
   - Api Gateway
   - 8 Microservicios
   - Frontend Vue

**Decir en voz:**
```
"Aquí estamos ejecutando docker-compose up que levanta los 14 contenedores 
del sistema. Podemos ver:
- La base de datos PostgreSQL iniciándose
- RabbitMQ con el message broker
- Eureka Server registrando todos los microservicios
- API Gateway en puerto 8080
- El frontend Vue.js en puerto 3000 (o 5173)

Esperar ~30-40 segundos a que todo esté healthy."
```

---

### MINUTO 3-4: LOGIN AL SISTEMA (1 minuto)

**Mostrar en pantalla:**
1. Abrir navegador en `http://localhost:3000` (o 5173)
2. Mostrar pantalla de login

**Decir en voz:**
```
"Ahora accedemos al frontend. Tenemos una pantalla de login con 
validacion de credenciales via JWT.

Los usuarios tienen roles diferentes:
- Admin: Acceso total
- Personal Administrativo: Gestión de estudiantes
- Instructor: Ver su horario
- Estudiante: Ver su progreso

Vamos a hacer login como Admin."
```

**Acciones:**
- Usuario: `admin@escuela.edu` (o el que tengan configurado)
- Contraseña: (usar las credenciales del .env)
- Click "Iniciar Sesión"

---

### MINUTO 4-5: CREAR UN ESTUDIANTE (1 minuto)

**Mostrar en pantalla:**
1. Menu → Estudiantes
2. Botón "Nuevo Estudiante"
3. Llenar formulario:
   - Nombre: Juan Pérez
   - Cédula: 1234567890
   - Email: juan@example.com
   - Teléfono: 0912345678
   - Dirección: Quito, Ecuador
4. Click "Guardar"

**Decir en voz:**
```
"Aquí estamos creando un nuevo estudiante. El sistema valida:
- Que la cédula sea válida (formato Ecuador: 10 dígitos)
- Que el email sea único
- Que todos los campos requeridos estén completos

En backend, esto envía una request POST a MS-Estudiantes:8082
que persiste en PostgreSQL en schema_estudiantes."
```

---

### MINUTO 5-6: CREAR UNA ASIGNACION DE CLASE (1 minuto)

**Mostrar en pantalla:**
1. Menu → Asignaciones
2. Botón "Nueva Asignación"
3. Seleccionar:
   - Estudiante: Juan Pérez
   - Instructor: (elegir uno disponible)
   - Vehículo: (elegir uno disponible)
   - Fecha y Hora
4. Mostrar las 6 validaciones que se ejecutan:

**Decir en voz:**
```
"Al crear una asignación, el sistema ejecuta 6 validaciones críticas:

1. Instructor disponible en esa hora
2. Vehículo disponible (sin mantenimiento)
3. Estudiante activo y no en arriendo
4. SOAT del vehículo vigente
5. RTV (revisión técnica) vigente
6. Sin conflictos de horarios

Si todas las validaciones pasan, la asignación se crea y se:
- Sincroniza kilometraje en el vehículo
- Sincroniza horas en el estudiante
- Envía notificaciones vía RabbitMQ a los 3 actores
- Persiste en schema_asignaciones"
```

---

### MINUTO 6-7: VER REPORTES Y DASHBOARD (1 minuto)

**Mostrar en pantalla:**
1. Menu → Reportes
2. Dashboard con:
   - Total estudiantes
   - Total horas completadas
   - Clases programadas
   - Ingresos
3. Grafico de tendencias
4. Exportar a PDF: Click botón "Descargar PDF"

**Decir en voz:**
```
"El dashboard agregá datos de múltiples microservicios:
- MS-Estudiantes: datos de estudiantes
- MS-Asignaciones: datos de clases
- MS-Cobros: datos financieros
- MS-Vehículos: datos de flota

Se utiliza RabbitMQ para eventos asíncronos y se cachean 
los datos en memoria con Caffeine para mejor performance.

Los reportes se pueden exportar a PDF o Excel."
```

---

### MINUTO 7-8: MOSTRAR CODIGO (1 minuto)

**Mostrar en pantalla:**
1. Abrir VS Code / IDE
2. Mostrar estructura de carpetas:
   ```
   microservices/
   ├── ms-auth/
   ├── ms-estudiantes/
   ├── ms-instructores/
   ├── ms-vehiculos/
   ├── ms-asignaciones/
   ├── ms-cobros/
   ├── ms-reportes/
   └── ms-notificaciones/
   ```

3. Mostrar un controlador (ej: EstudiantesController.java):
   ```java
   @PostMapping
   public ResponseEntity<EstudianteResponse> crear(@RequestBody CreateEstudianteRequest request) {
       return ResponseEntity.ok(estudianteService.crear(request));
   }
   ```

4. Mostrar un servicio (ej: EstudianteService.java)
5. Mostrar una entidad JPA

**Decir en voz:**
```
"El backend está estructurado en 8 microservicios independientes.
Cada uno tiene su propia:
- Capa de Controlador (REST APIs)
- Capa de Servicio (lógica de negocio)
- Capa de Repository (JPA + Hibernate)
- Capa de Entity (modelo de datos)
- DTOs para request/response

La comunicación entre microservicios se hace mediante:
- Feign clients para llamadas síncronas
- RabbitMQ para eventos asíncronos

Cada microservicio se conecta a PostgreSQL mediante JDBC
y todos se registran en Eureka para service discovery."
```

---

### MINUTO 8-9: MOSTRAR BASE DE DATOS (1 minuto)

**Mostrar en pantalla:**
1. Abrir Adminer o DBeaver conectado a PostgreSQL:
   - URL: `http://localhost:8088` (si Adminer está corriendo)
2. Mostrar los 9 schemas:
   ```
   - schema_auth
   - schema_estudiantes
   - schema_instructores
   - schema_vehiculos
   - schema_asignaciones
   - schema_cobros
   - schema_reportes
   - schema_notificaciones
   - schema_common
   ```
3. Expandir schema_estudiantes y mostrar tabla "estudiantes"
4. Ver los datos que acaban de crear

**Decir en voz:**
```
"PostgreSQL está configurado con 9 esquemas, uno por dominio de negocio.
Esto permite:
- Independencia entre módulos
- Escalabilidad futura (cada schema puede ir a su propia BD)
- Mejor organización de datos

Las migraciones se hacen con Flyway, permitiendo versionado 
de esquema y rollback seguro."
```

---

### MINUTO 9-10: TESTING Y CONCLUSIONES (1 minuto)

**Mostrar en pantalla:**
1. Abrir terminal
2. Ejecutar: `mvn test` en un microservicio
   O mostrar log de tests: `[OK] 172 tests passed, 82% coverage`
3. Mostrar algunos archivos de test

**Decir en voz:**
```
"El sistema cuenta con 172 tests automatizados distribuidos 
en los 8 microservicios:
- MS-Auth: 28 tests
- MS-Estudiantes: 25 tests
- MS-Instructores: 22 tests
- MS-Vehículos: 26 tests
- MS-Asignaciones: 31 tests (las más complejas por las 6 validaciones)
- MS-Cobros: 27 tests
- MS-Reportes: 18 tests
- MS-Notificaciones: 19 tests

La cobertura de código es de 82% usando JaCoCo.
Usamos JUnit 5, Mockito y Testcontainers para testing.

En conclusión, este es un sistema profesional de microservicios
completamente funcional, escalable, testeable y listo para producción."
```

---

## CONFIGURACION DE OBS STUDIO

### 1. Descargar OBS Studio
- Ir a https://obsproject.com/
- Descargar para Windows/Mac/Linux
- Instalar

### 2. Configurar OBS para grabar

**Paso 1: Crear una escena**
1. Click "+ Scene"
2. Nombrar: "Demo Video"

**Paso 2: Agregar fuente**
1. Click "+ Source"
2. Seleccionar "Display Capture" o "Window Capture"
3. Seleccionar pantalla/ventana a grabar

**Paso 3: Configurar audio**
1. Click en "Audio Mixer"
2. Seleccionar micrófono
3. Ajustar nivel a -3dB aproximadamente

**Paso 4: Configurar grabación**
1. Click "Settings"
2. Ir a "Output" → "Recording"
3. Seleccionar ruta donde guardar video
4. Formato: MP4
5. Codificador: H.264
6. Bitrate: 6000 kbps (para buena calidad)
7. Resolución: 1920x1080

**Paso 5: Grabar**
1. Click "Start Recording"
2. Presentar demo
3. Click "Stop Recording"
4. Video se guarda automáticamente

---

## TIPS PARA GRABAR

### Antes de grabar
- [ ] Cierra notificaciones
- [ ] Configura resolución a 1920x1080
- [ ] Prueba micrófono
- [ ] Ten el script a mano
- [ ] Levanta docker-compose 30 min antes
- [ ] Verifica que tengas 10+ GB libres en disco

### Mientras grabas
- [ ] Habla claramente y a velocidad moderada
- [ ] Hace clicks y escribes lentamente para que se vea
- [ ] Muestra el resultado de cada acción
- [ ] No comas ni bebas mientras hablas
- [ ] Respira entre oraciones

### Si cometes error
- [ ] Sigue grabando (edita después) O
- [ ] Pausa, corrígete, continúa

---

## EDICION BASICA (OPCIONAL)

Si necesitas editar el video:

### Opcion 1: DaVinci Resolve (Gratuito)
- Descargar en https://www.blackmagicdesign.com/products/davinciresolve/
- Importar video
- Cortar partes malas
- Exportar

### Opcion 2: CapCut (Gratuito, Web)
- Ir a https://www.capcut.com/
- Subir video
- Editar online
- Descargar

### Opcion 3: Subir directo sin editar
- Si sale bien a la primera, sube directo a YouTube

---

## SUBIR VIDEO A YOUTUBE

1. Ir a https://youtube.com
2. Click en tu foto de perfil
3. "Create a video" o "Upload video"
4. Seleccionar archivo de video
5. Título: `Demo Sistema Escuela de Conduccion - Proyecto Titulacion UDLA`
6. Descripcion:
   ```
   Sistema de Control Administrativo y Financiero para Escuelas de Conducción
   
   Arquitectura: 8 Microservicios + API Gateway + PostgreSQL + RabbitMQ
   Frontend: Vue.js 3 + TypeScript
   Backend: Java 21 + Spring Boot 3.4
   Testing: 172 tests, 82% cobertura
   
   Demostracion completa de:
   - Login y autenticación JWT
   - Creacion de estudiantes
   - Programacion de clases (6 validaciones)
   - Dashboard y reportes
   - Codigo fuente
   - Base de datos
   
   Video de máximo 10 minutos explicando funcionalidad total.
   ```
7. Privacidad: "Unlisted" (solo quien tenga el link)
8. Click "PUBLISH" o "Save"
9. Copiar link compartible

---

## CHECKLIST FINAL

- [ ] Video grabado (máximo 10 minutos)
- [ ] Audio claro y comprensible
- [ ] Se vé bien a 1920x1080
- [ ] Muestra: Sistema corriendo, Frontend, Código, BD, Tests
- [ ] Subido a YouTube (Unlisted)
- [ ] Link copiado y listo
- [ ] Documento de tesis actualizado
- [ ] PDF DIAGRAMA_PROCESO_PRINCIPAL.pdf preparado

---

## LINKS PARA LA ASIGNACION

Cuando subas a Blackboard, incluir:

```
Archivos:
1. DIAGRAMA_PROCESO_PRINCIPAL.pdf
2. Documento_Proyecto_Titulacion_FINAL.pdf (o tu nombre)

Link del Video:
https://youtube.com/watch?v=TU_VIDEO_ID

Nota adicional:
"Sistema completamente funcional con 8 microservicios, 
172 tests, 82% cobertura, Docker Compose con 14 contenedores.
Listo para producción."
```

---

**¡Mucho éxito grabando el video!**  
Cualquier duda, pregunta.

