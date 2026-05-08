# Sprint 4 — Validación End-to-End

**Fecha:** 2026-05-07
**Branch:** `feature/sprint-4-5-validacion-e2e-cierre`
**Stack:** 14 contenedores Docker (Postgres, RabbitMQ, MinIO, Adminer, Eureka, Gateway, 8 MS)

> Documento que registra el resultado de la validación funcional end-to-end del sistema de autenticación implementado en el Sprint 4 (T4.1 a T4.5). Sirve como evidencia para titulación.

---

## 🎯 Alcance validado

El **Sprint 4** entregó el sistema de autenticación completo:

| Tarea | PR | Aporte |
|---|---|---|
| T4.1 | #9 | Framework JWT en `common-security` (Provider, Properties, Helpers) |
| T4.2 | #10 | MS-Auth con login/refresh/logout/forgot/reset + lockout + eventos |
| T4.3 | #11 | API Gateway con filtro JWT global + headers `X-User-*` + CORS |
| T4.4 | #12 | MS-Notificaciones consume eventos + emails Mailtrap (dry-run) |
| T4.5 | (este) | Limpieza + validación E2E + cierre Sprint |

---

## ⚙️ Setup previo

```bash
# 1. .env con JWT_SECRET (mínimo 64 chars)
JWT_SECRET=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef

# 2. Levantar stack
docker compose --env-file .env -f infrastructure/docker/docker-compose.yml up -d --build

# 3. Esperar 14/14 healthy (~50-60s)
docker compose -f infrastructure/docker/docker-compose.yml ps
```

**Observación importante:** docker compose no lee `.env` de la raíz del proyecto por defecto cuando el archivo compose vive en otra carpeta. Hay que pasarle `--env-file .env` explícitamente.

---

## 📋 Tabla de resultados (13 tests)

| # | Test | Status | Notas |
|---|---|---|---|
| 1 | `mvn clean install` con tests | ✅ | 15 módulos BUILD SUCCESS, 60+ tests verdes |
| 2 | Stack Docker 14/14 healthy | ✅ | 13 healthy + Adminer Up |
| 3 | `GET /actuator/health` (path público) | ✅ | HTTP 200 |
| 4 | `GET /estudiantes/1` sin token | ✅ | HTTP 401 RFC 7807 + correlationId |
| 5 | `POST /auth/login` con admin válido | ✅ | HTTP 200 + `accessToken` + `refreshToken` + cookies HttpOnly |
| 6 | `GET /estudiantes/1` con `Bearer` válido | ✅ | HTTP 404 (endpoint no existe) — el JWT pasó al MS |
| 7 | `POST /auth/refresh` con refresh válido | ✅ | HTTP 200 + tokens nuevos (rotation) |
| 8 | Replay attack: refresh ya usado | ✅ | HTTP 401 "Refresh token ya usado o revocado" |
| 9 | 3 logins fallidos → lockout + evento | ✅ | `failed_attempts=3, locked=true`, evento publicado y consumido |
| 10 | Login con cuenta bloqueada | ✅ | HTTP 423 Locked |
| 11 | `POST /auth/forgot-password` válido | ✅ | HTTP 202, fila en `password_reset_token`, log `RECUPERAR_PASSWORD` `PENDIENTE` |
| 12 | `POST /auth/forgot-password` email inexistente | ✅ | HTTP 202 (anti-enumeration), sin fila ni log |
| 13 | `POST /auth/logout` con refresh | ✅ | HTTP 204, fila `refresh_tokens.revocado=true` |

---

## 🐛 Bugs encontrados durante la validación E2E

### Bug 1: `failed_attempts` no se persistían (rollback)

**Síntoma:** tras 3 logins fallidos, `auth_schema.usuarios.failed_attempts = 0` y `locked = false`.

**Causa raíz:** `AuthService.login` estaba marcado con `@Transactional` (rollback automático en `RuntimeException`). Al lanzar `InvalidCredentialsException` (subclase de RuntimeException), Spring hacía rollback del incremento de `failedAttempts` aunque se había llamado `usuarioRepository.save(usuario)` previamente.

**Fix:** `@Transactional(noRollbackFor = {InvalidCredentialsException.class, AccountLockedException.class})` para que estas excepciones NO disparen rollback. La fila persiste con el contador incrementado.

### Bug 2: `EventPublisher` nunca se registró como bean → eventos no se publicaban

**Síntoma:** los eventos `UsuarioBloqueadoEvent` y `PasswordResetSolicitadoEvent` se generaban en MS-Auth pero el log decía `"EventPublisher no disponible (probablemente perfil test)"` y nunca llegaban a RabbitMQ.

**Causa raíz:** `AuthEventDispatcher` dependía de `ObjectProvider<EventPublisher>`, pero `EventPublisher` nunca fue declarado como `@Bean` en ninguna `@Configuration`. Solo existía como POJO instanciado directamente en `UsuarioEventPublisher` (que se eliminó en T4.5).

**Fix:** refactorizar `AuthEventDispatcher` para inyectar `ObjectProvider<RabbitTemplate>` (que sí existe en runtime real, creado por `AbstractRabbitConfig`) y construir `EventPublisher` lazy + cached al primer uso.

### Bug 3: `JWT_SECRET` no se propagaba a contenedores

**Síntoma:** MS-Auth y Gateway fallaban al iniciar con `Could not resolve placeholder 'JWT_SECRET'`.

**Causa raíz:** el `docker-compose.yml` no incluía `JWT_SECRET: ${JWT_SECRET}` en el `environment` de los servicios. Sin esa entrada, docker compose no propagaba la variable al contenedor aunque estuviera en `.env`.

**Fix:** agregar `JWT_SECRET: ${JWT_SECRET}` al `environment` de `api-gateway` y al anchor `&ms-environment` (todos los MS).

### Bug 4: `docker compose` no encontraba el `.env` de la raíz

**Síntoma:** las variables del `.env` en la raíz del proyecto no se cargaban cuando se ejecutaba `docker compose -f infrastructure/docker/docker-compose.yml up`.

**Causa raíz:** docker compose busca el `.env` por defecto en el directorio del archivo compose (`infrastructure/docker/`), no en cwd.

**Fix:** ejecutar siempre con `--env-file .env` cuando se usa este path:
```bash
docker compose --env-file .env -f infrastructure/docker/docker-compose.yml up -d
```

### Bug 5: Health check de mail bloqueaba MS-Notificaciones

**Síntoma:** `escuela-ms-notificaciones` quedaba `unhealthy` con error `mail.AuthenticationFailedException: failed to connect, no password specified`.

**Causa raíz:** Spring Boot autoconfigura un health indicator para mail cuando `spring-boot-starter-mail` está en classpath. El indicator intenta conectar al SMTP y falla si no hay credenciales (el modo dry-run).

**Fix:** desactivar el health check de mail en `application.yml`:
```yaml
management:
  health:
    mail:
      enabled: false
```

---

## 📸 Evidencias destacadas

### Test 5 — Login exitoso

```bash
$ curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}'

HTTP 200
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIzNTVkYWRhOC...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiIyNGRlYTAzMy0...",
  "accessTokenExpiresInSeconds": 900,
  "refreshTokenExpiresInSeconds": 604800,
  "user": {"id": 1, "email": "admin@escuela.local", "roles": ["ADMIN"]}
}
```

JWT decodificado (access):
```json
{
  "jti": "355dada8-7675-46a7-a969-ab7fa49524c2",
  "sub": "1",
  "iss": "escuela-conduccion",
  "iat": 1778188730,
  "exp": 1778189630,
  "email": "admin@escuela.local",
  "roles": ["ADMIN"],
  "type": "ACCESS"
}
```

### Test 8 — Replay attack detectado

```bash
1er uso: HTTP 200 (rotation OK)
2do uso: HTTP 401
{
  "type": "https://escuela.local/errors/invalid-token",
  "title": "Token invalido",
  "status": 401,
  "detail": "Refresh token ya usado o revocado",
  "instance": "/auth/refresh"
}
```

### Test 9 — Lockout end-to-end

**Logs de MS-Auth (publica evento):**
```
INFO c.e.c.events.publisher.EventPublisher    : Evento publicado:
  exchange=auth.exchange,
  routingKey=auth.usuario.bloqueado,
  eventId=ce1cdcf3-0963-4c49-81f7-c9099c028556,
  source=ms-auth
```

**Logs de MS-Notificaciones (consume evento):**
```
INFO uthEventListener$UsuarioBloqueadoHandler : UsuarioBloqueadoEvent procesado:
  usuarioId=1, lockUntil=2026-05-07T16:40:48Z, eventId=ce1cdcf3-...
DEBUG uthEventListener$UsuarioBloqueadoHandler : Evento ce1cdcf3-... marcado
  como procesado (tipo=auth.usuario.bloqueado)
```

**Estado final BD:**
```sql
SELECT email, failed_attempts, locked, lock_until FROM auth_schema.usuarios;
        email        | failed_attempts | locked |         lock_until
---------------------+-----------------+--------+-------------------------
 admin@escuela.local |               3 | t      | 2026-05-07 21:40:48...

SELECT plantilla_codigo, estado FROM notificaciones_schema.log_envios_email;
 plantilla_codigo  |  estado
-------------------+-----------
 CUENTA_BLOQUEADA  | PENDIENTE
```

> Estado `PENDIENTE` (no `ENVIADO`) porque corremos en **dry-run** (`JAVA_MAIL_ENABLED=false`). Para envío real bastaría con configurar credenciales Mailtrap y poner la variable en `true`.

---

## ✅ Conclusión

El **Sprint 4 cierra con autenticación completa y validada end-to-end**:

1. **Login real** con bcrypt + JWT HS512
2. **Refresh token con rotation** y detección de replay attacks
3. **Account lockout** (3 intentos / 15 min) con publicación de evento
4. **Forgot/reset password** con anti-enumeración + token UUID + evento
5. **API Gateway** validando JWT y propagando headers `X-User-*`
6. **MS-Notificaciones** consumiendo eventos y persistiendo intentos de envío
7. **Idempotencia** en consumers vía `processed_events`
8. **5 bugs encontrados y arreglados** durante la validación (documentados arriba)

El sistema está listo para que el **Sprint 5** comience a implementar los CRUDs reales de los dominios (estudiantes, instructores, vehículos, etc.) usando los headers `X-User-*` para autorización vía `@PreAuthorize`.

---

## 📋 Comandos útiles para reproducir

```bash
# Reset password admin (bcrypt de "Admin123!")
docker exec escuela-postgres psql -U escuela_user -d escuela_db -c \
  "UPDATE auth_schema.usuarios
   SET password = '\$2b\$10\$i2bC3GPW/XFq5wiFffgF0O4WLGKHs/cYaVkfBhUgJvgWWtryED4oi',
       failed_attempts = 0, locked = false, lock_until = null
   WHERE email = 'admin@escuela.local';"

# Verificar evento en RabbitMQ
curl -s -u guest:guest http://localhost:15672/api/queues/%2F/notificaciones.queue | \
  python -c "import sys,json;d=json.loads(sys.stdin.read());print(f'consumers={d[\"consumers\"]}, ready={d[\"messages_ready\"]}')"

# Ver logs de eventos en cadena
docker logs escuela-ms-auth --since 60s | grep "Evento publicado"
docker logs escuela-ms-notificaciones --since 60s | grep "procesado"

# Inspeccionar tablas relevantes
docker exec escuela-postgres psql -U escuela_user -d escuela_db -c \
  "SELECT * FROM auth_schema.refresh_tokens ORDER BY id DESC LIMIT 5;"
docker exec escuela-postgres psql -U escuela_user -d escuela_db -c \
  "SELECT * FROM notificaciones_schema.log_envios_email ORDER BY created_at DESC LIMIT 5;"
docker exec escuela-postgres psql -U escuela_user -d escuela_db -c \
  "SELECT * FROM shared_schema.processed_events ORDER BY processed_at DESC LIMIT 10;"
```

---

## 🔁 Validación post-cierre (2026-05-08) — suites curl reusables

Tras el merge de PR #13, se ejecutaron **dos suites curl exhaustivas** que destaparon 6 bugs adicionales en Sprint 4. Ambas suites quedan en el repo como pruebas de regresión para futuros sprints.

### Suite 1 — `test_sprint4_exhaustivo.sh` (71 tests · 100%)

Cubre los cinco T4.x con asserts de:
- **T4.4 Eureka:** los 9 servicios registrados (API-GATEWAY, MS-AUTH, MS-ESTUDIANTES, MS-INSTRUCTORES, MS-VEHICULOS, MS-ASIGNACIONES, MS-COBROS, MS-REPORTES, MS-NOTIFICACIONES).
- **T4.2 MS-Auth:** login OK/inválido, anti-enumeration, refresh con rotation, refresh ya-usado rechazado, `/auth/me` con/sin token, logout idempotente, forgot-password (existe/no existe), cookies HttpOnly, validación @Valid devolviendo 400.
- **T4.1 JWT:** algoritmo HS512, claims (jti/sub/iss/iat/exp/email/roles/type), access vs refresh (`type=ACCESS|REFRESH`), `iss=escuela-conduccion`, refresh rechazado en endpoint protegido.
- **T4.3 Gateway:** health, ruta protegida sin/con/malformado token, RFC 7807 problem+json + correlationId, CORS preflight con origin permitido/rechazado, autenticación vía Cookie, paths públicos.
- **T4.5 RabbitMQ:** mgmt UI, exchange `auth.exchange` existe, MS-Notif healthy, eventos publicados.

### Suite 2 — `test_sprint4_avanzado.sh` (45 tests · 100% + 1 skip)

Cubre los 15 casos de profundidad que faltaban:

| # | Caso | Asserts |
|---|---|---|
| 1 | **Lockout real** (3 intentos → bloqueo, login correcto rechazado con 423, evento `UsuarioBloqueado` publicado) | 7 |
| 2 | **Flow forgot→reset→login completo** (token UUID en BD, password viejo rechazado, token marcado `usado=true`, reuso rechazado) | 8 |
| 3 | **Reset revoca refresh tokens del usuario** | 4 |
| 4 | **MS-Notif consumer activo** en `notificaciones.queue` y procesa eventos | 2 |
| 5 | **Cuenta inactiva** (`activo=false`) rechaza login con password correcto | 1 |
| 6 | **JWT expirado** rechazado (forjado con PyJWT) | 1 |
| 7 | **Firma manipulada** + **secreto distinto** rechazados | 2 |
| 8 | **Header spoofing** — Gateway sobreescribe `X-User-*` aunque el cliente los mande | 2 |
| 9 | **Cookie + Authorization** simultáneos — header tiene prioridad | 4 |
| 10 | **Reset token expirado** en BD rechazado | 1 |
| 11 | **DLX** — `auth.dlx`, `notificaciones.dlx`, todas las `.dlq` configuradas | 3 |
| 12 | **Eureka failover** — bajar MS → Gateway error 5xx, levantarlo → routea de nuevo | 2 |
| 13 | **Cleanup refresh tokens** | SKIP (no implementado, alcance Sprint 5+) |
| 14 | **Correlation-id** auto-generado + echo en respuestas 401 | 2 |
| 15 | **Métricas Actuator** (`jvm.memory.used`, `http.server.requests`, todos los MS) | 5 |

### Bugs fixeados en esta validación post-cierre (6)

| # | Síntoma | Causa raíz | Fix |
|---|---|---|---|
| 1 | `/auth/refresh` y `/auth/logout` daban 403 | Marcados como protegidos en Gateway pero su credencial es el refresh token, no el access token. Si access expiró, el usuario quedaba sin poder renovar. | Movidos a `escuela.gateway.public-paths` en `application.yml` del Gateway. |
| 2 | `/auth/me` daba 403 incluso con JWT válido | El controller usaba `Authentication authentication`, pero MS-Auth no tiene filtro JWT propio (la validación ocurre en el Gateway). El parámetro siempre llegaba `null`. | `@RequestHeader(value=UserHeaders.USER_EMAIL, required=false) String email` + delegación al servicio. |
| 3 | `LazyInitializationException` al iterar `usuario.getRoles()` en `/auth/me` | `roles` es `FetchType.LAZY` y se accedía fuera de la transacción Hibernate. | Refactor a `getUserInfoByEmail()` con `@Transactional(readOnly=true)` que devuelve DTO `LoginResponse.UserInfo` con roles materializados. |
| 4 | Validación `@Valid` devolvía 403 en vez de 400 | Sin handler para `MethodArgumentNotValidException`, el body queda vacío. Spring Security 6 con `httpBasic`/`formLogin` deshabilitados usa `Http403ForbiddenEntryPoint` por defecto y devuelve 403. | Handlers para `MethodArgumentNotValidException` y `HttpMessageNotReadableException` en `AuthExceptionHandler` que devuelven `ProblemDetail` 400. |
| 5 | `ResetPasswordRequest` con `@NotBlank` sobre tipo `UUID` lanzaba `UnexpectedTypeException` | `@NotBlank` solo valida `String`. Para UUID hay que usar `@NotNull`. | Cambio a `@NotNull UUID token`. |
| 6 | Spring Security warning + matcher mal interpretado | `requestMatchers("POST", "/auth/login")` — Spring Security 6 no tiene overload `(String,String...)`, ambos se tomaban como patterns sin slash inicial. | Reemplazado por `requestMatchers(HttpMethod.POST, "/auth/login")`. |

### Hallazgos no bloqueantes (candidatos para Sprint 5+)

- Gateway responde **HTTP 500** cuando un MS downstream está down. Idealmente sería **503/504** (Service Unavailable) con circuit breaker (Resilience4j).
- Gateway no propaga `X-Correlation-Id` en respuestas 200 OK (sí en 401). El downstream sí lo recibe en el request, pero el cliente final no lo ve de vuelta cuando todo OK.
- Sin scheduler de cleanup de refresh tokens expirados en BD. La tabla `auth_schema.refresh_tokens` crecerá indefinidamente.

### Reproducir las suites

```bash
# Pre-requisitos: stack levantado, .env con JWT_SECRET, Python 3 con PyJWT
docker compose --env-file .env -f infrastructure/docker/docker-compose.yml up -d --build

# Suite exhaustiva
bash infrastructure/docker/test_sprint4_exhaustivo.sh

# Suite avanzada (incluye lockout, JWT forging, failover; toma ~2 min)
bash infrastructure/docker/test_sprint4_avanzado.sh
```

Los scripts crean usuarios de test (`lockout.test@escuela.local`, `inactive.test@escuela.local`, `reset.test@escuela.local`) con BCrypt hash conocido y los limpian al final.

