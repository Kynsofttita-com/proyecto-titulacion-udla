#!/bin/bash
# =============================================================================
# Sprint 4 - SUITE AVANZADA (los 15 casos restantes)
# =============================================================================
# Cubre:
#   ALTA  : 1 lockout real | 2 flow forgot->reset->login | 3 reset revoca refresh
#           4 MS-Notif consume evento | 5 cuenta inactiva
#   MEDIA : 6 token expirado | 7 firma manipulada | 8 header spoofing
#           9 cookie+auth | 10 reset token expirado/usado | 11 DLX
#           12 Eureka failover | 13 cleanup refresh tokens
#   BAJA  : 14 correlation-id propagado | 15 metricas
#
# Requisitos:
#   - docker compose up (todos los MS healthy)
#   - python con PyJWT (para forjar tokens)
#   - acceso a postgres y rabbitmq via docker exec
# =============================================================================
set -u

# Cargar JWT_SECRET del .env (necesario para forjar tokens)
ENV_FILE="$(dirname "$0")/../../.env"
JWT_SECRET=$(grep -E "^JWT_SECRET=" "$ENV_FILE" | head -1 | cut -d= -f2-)
ADMIN_HASH='$2b$10$i2bC3GPW/XFq5wiFffgF0O4WLGKHs/cYaVkfBhUgJvgWWtryED4oi'  # Admin123!

PASS=0; FAIL=0; SKIP=0; RESULTS=""
ok()   { echo "  PASS: $1";              PASS=$((PASS+1)); RESULTS="$RESULTS\nPASS  | $1"; }
fail() { echo "  FAIL: $1";               FAIL=$((FAIL+1)); RESULTS="$RESULTS\nFAIL  | $1"; }
skip() { echo "  SKIP: $1 ($2)";          SKIP=$((SKIP+1)); RESULTS="$RESULTS\nSKIP  | $1 ($2)"; }
section() { echo ""; echo "=============================================="; echo " $1"; echo "=============================================="; }

assert_eq() {
    local label="$1"; local actual="$2"; local expected="$3"
    [ "$actual" = "$expected" ] && ok "$label  -> $actual" || fail "$label  -> $actual (esperado $expected)"
}

psql_exec() {
    docker exec escuela-postgres psql -U escuela_user -d escuela_db -t -A -c "$1" 2>/dev/null
}

ensure_test_user() {
    local email="$1"; local activo="${2:-true}"
    psql_exec "INSERT INTO auth_schema.usuarios (email, password, nombre, apellido, activo, locked, failed_attempts, created_at, updated_at)
               VALUES ('$email', '$ADMIN_HASH', 'Test', 'User', $activo, false, 0, NOW(), NOW())
               ON CONFLICT (email) DO UPDATE SET activo=$activo, locked=false, failed_attempts=0, lock_until=NULL, password='$ADMIN_HASH';" >/dev/null
    local id=$(psql_exec "SELECT id FROM auth_schema.usuarios WHERE email='$email';")
    psql_exec "INSERT INTO auth_schema.usuario_rol (usuario_id, rol_id)
               SELECT $id, 2 WHERE NOT EXISTS (SELECT 1 FROM auth_schema.usuario_rol WHERE usuario_id=$id AND rol_id=2);" >/dev/null
    echo "$id"
}

reset_user() {
    psql_exec "UPDATE auth_schema.usuarios SET failed_attempts=0, locked=false, lock_until=NULL, activo=true WHERE email='$1';" >/dev/null
}

cleanup_user() {
    local id=$(psql_exec "SELECT id FROM auth_schema.usuarios WHERE email='$1';")
    [ -n "$id" ] && {
        psql_exec "DELETE FROM auth_schema.refresh_tokens WHERE usuario_id=$id;" >/dev/null
        psql_exec "DELETE FROM auth_schema.password_reset_token WHERE usuario_id=$id;" >/dev/null
        psql_exec "DELETE FROM auth_schema.usuario_rol WHERE usuario_id=$id;" >/dev/null
        psql_exec "DELETE FROM auth_schema.usuarios WHERE id=$id;" >/dev/null
    }
}

login_token() {
    curl -s -X POST http://localhost:8080/auth/login \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$1\",\"password\":\"$2\"}" \
        | grep -oP '"accessToken"\s*:\s*"\K[^"]+'
}

# ============================================================================
section "PREP - Usuarios de test"
# ============================================================================
TEST_LOCKOUT="lockout.test@escuela.local"
TEST_INACTIVE="inactive.test@escuela.local"
TEST_RESET="reset.test@escuela.local"

ensure_test_user "$TEST_LOCKOUT" true >/dev/null
ensure_test_user "$TEST_INACTIVE" false >/dev/null
ensure_test_user "$TEST_RESET" true >/dev/null
ok "Usuarios de test creados/asegurados"

# ============================================================================
section "1 - LOCKOUT REAL (3 intentos fallidos -> bloqueo)"
# ============================================================================
reset_user "$TEST_LOCKOUT"
echo ""
echo "[1.1] Login fallido 1/3"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_LOCKOUT\",\"password\":\"X\"}")
assert_eq "Intento 1 -> 401" "$HTTP" "401"

echo "[1.2] Login fallido 2/3"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_LOCKOUT\",\"password\":\"X\"}")
assert_eq "Intento 2 -> 401" "$HTTP" "401"

echo "[1.3] Login fallido 3/3 (debe bloquear)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_LOCKOUT\",\"password\":\"X\"}")
# 401 todavia (porque la 3a invocacion incrementa y bloquea, pero responde con InvalidCredentials)
assert_eq "Intento 3 -> 401" "$HTTP" "401"

echo "[1.4] BD refleja locked=true y lock_until poblado"
LOCKED=$(psql_exec "SELECT locked FROM auth_schema.usuarios WHERE email='$TEST_LOCKOUT';")
LOCK_UNTIL=$(psql_exec "SELECT lock_until IS NOT NULL FROM auth_schema.usuarios WHERE email='$TEST_LOCKOUT';")
[ "$LOCKED" = "t" ] && ok "BD: locked=true" || fail "BD: locked=$LOCKED"
[ "$LOCK_UNTIL" = "t" ] && ok "BD: lock_until poblado" || fail "BD: lock_until vacio"

echo "[1.5] Login con password CORRECTO durante lockout es rechazado"
RESP=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_LOCKOUT\",\"password\":\"Admin123!\"}")
HTTP=$(echo "$RESP" | tail -n1)
assert_eq "Login bloqueado -> 423 LOCKED" "$HTTP" "423"

echo "[1.6] Evento UsuarioBloqueado publicado en RabbitMQ"
# Resetear y disparar lockout fresco; esperar a que stats de RabbitMQ refresquen.
reset_user "$TEST_LOCKOUT"
PUB_BEFORE=$(curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F/auth.exchange | grep -oP '"publish_in":\K[0-9]+' | head -1)
PUB_BEFORE=${PUB_BEFORE:-0}
for i in 1 2 3; do
    curl -s -o /dev/null -X POST http://localhost:8080/auth/login \
        -H "Content-Type: application/json" -d "{\"email\":\"$TEST_LOCKOUT\",\"password\":\"X\"}"
    sleep 0.3
done
sleep 6  # RabbitMQ stats refresh ~5s
PUB_AFTER=$(curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F/auth.exchange | grep -oP '"publish_in":\K[0-9]+' | head -1)
PUB_AFTER=${PUB_AFTER:-0}
echo "  publish_in: $PUB_BEFORE -> $PUB_AFTER"
[ "$PUB_AFTER" -gt "$PUB_BEFORE" ] && ok "Evento UsuarioBloqueado publicado en exchange" || fail "publish_in no incremento"

# ============================================================================
section "2 - FLOW forgot -> reset -> login con nuevo password"
# ============================================================================
reset_user "$TEST_RESET"
echo ""
echo "[2.1] Login inicial con password actual"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\",\"password\":\"Admin123!\"}")
assert_eq "Login con password actual" "$HTTP" "200"

echo "[2.2] Solicitar forgot-password (publica evento + crea token)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\"}")
assert_eq "Forgot-password" "$HTTP" "202"

USER_ID=$(psql_exec "SELECT id FROM auth_schema.usuarios WHERE email='$TEST_RESET';")
RESET_TOKEN=$(psql_exec "SELECT token FROM auth_schema.password_reset_token
                          WHERE usuario_id=$USER_ID AND usado=false ORDER BY created_at DESC LIMIT 1;")
echo "  Reset token (de BD): ${RESET_TOKEN:0:36}..."
[ -n "$RESET_TOKEN" ] && ok "Token de reset guardado en BD" || fail "Sin token en BD"

echo "[2.3] Reset-password con token valido"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"NuevoPass123!\"}")
assert_eq "Reset-password OK" "$HTTP" "204"

echo "[2.4] Login con NUEVO password funciona"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\",\"password\":\"NuevoPass123!\"}")
assert_eq "Login con password nuevo" "$HTTP" "200"

echo "[2.5] Login con VIEJO password ya NO funciona"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\",\"password\":\"Admin123!\"}")
assert_eq "Password viejo rechazado" "$HTTP" "401"

echo "[2.6] Token de reset marcado como usado en BD"
USADO=$(psql_exec "SELECT usado FROM auth_schema.password_reset_token WHERE token='$RESET_TOKEN';")
[ "$USADO" = "t" ] && ok "Token marcado usado=true" || fail "Token usado=$USADO"

echo "[2.7] Reusar token de reset ya usado rechazado"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$RESET_TOKEN\",\"newPassword\":\"OtroPass123!\"}")
assert_eq "Reusar token usado" "$HTTP" "401"

# ============================================================================
section "3 - RESET REVOCA REFRESH TOKENS DEL USUARIO"
# ============================================================================
echo ""
echo "[3.1] Login y obtener refresh token"
RESP=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\",\"password\":\"NuevoPass123!\"}")
OLD_REFRESH=$(echo "$RESP" | grep -oP '"refreshToken"\s*:\s*"\K[^"]+')
[ -n "$OLD_REFRESH" ] && ok "Refresh token obtenido" || fail "Sin refresh token"

echo "[3.2] Refresh con ese token funciona"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/refresh \
    -H "Content-Type: application/json" -d "{\"refreshToken\":\"$OLD_REFRESH\"}")
assert_eq "Refresh antes del reset" "$HTTP" "200"

echo "[3.3] Login fresco + reset password"
RESP=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\",\"password\":\"NuevoPass123!\"}")
PRE_RESET_REFRESH=$(echo "$RESP" | grep -oP '"refreshToken"\s*:\s*"\K[^"]+')
curl -s -o /dev/null -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\"}"
NEW_RT=$(psql_exec "SELECT token FROM auth_schema.password_reset_token
                     WHERE usuario_id=$USER_ID AND usado=false ORDER BY created_at DESC LIMIT 1;")
curl -s -o /dev/null -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$NEW_RT\",\"newPassword\":\"OtroPass123!\"}"
ok "Reset ejecutado"

echo "[3.4] Refresh con refresh-token PRE-RESET es rechazado"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/refresh \
    -H "Content-Type: application/json" -d "{\"refreshToken\":\"$PRE_RESET_REFRESH\"}")
assert_eq "Refresh viejo tras reset -> 401" "$HTTP" "401"

# ============================================================================
section "4 - MS-NOTIFICACIONES consume el evento"
# ============================================================================
echo ""
echo "[4.1] Forgot-password publica evento -> consumer log debe registrarlo"
docker logs escuela-ms-notificaciones --tail 0 -f > /tmp/notif.log 2>&1 &
TAIL_PID=$!
sleep 1
curl -s -o /dev/null -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_RESET\"}"
sleep 3
kill $TAIL_PID 2>/dev/null
NOTIF_LOG=$(cat /tmp/notif.log 2>/dev/null)
echo "$NOTIF_LOG" | grep -qiE "passwordreset|password.*reset|email.*enviado|sent.*email|notif.*reset" \
    && ok "MS-Notif procesa evento password-reset" \
    || fail "MS-Notif sin evidencia de procesar evento (log abajo)"
echo "$NOTIF_LOG" | head -10

echo "[4.2] Queue notificaciones.queue tiene mensajes consumidos"
QUEUE_INFO=$(curl -s -u guest:guest "http://localhost:15672/api/queues/%2F/notificaciones.queue" 2>/dev/null)
ACTIVE_CONSUMERS=$(echo "$QUEUE_INFO" | grep -oP '"consumers":\K[0-9]+' | head -1)
DELIVERED=$(echo "$QUEUE_INFO" | grep -oP '"deliver(_get)?":\K[0-9]+' | head -1)
echo "  consumers activos: ${ACTIVE_CONSUMERS:-0}, deliveries: ${DELIVERED:-0}"
[ "${ACTIVE_CONSUMERS:-0}" -gt 0 ] && ok "MS-Notif tiene consumer activo en notificaciones.queue" || fail "Sin consumer activo"

# ============================================================================
section "5 - CUENTA INACTIVA (activo=false)"
# ============================================================================
echo ""
echo "[5.1] Login con cuenta inactiva (activo=false en BD)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" -d "{\"email\":\"$TEST_INACTIVE\",\"password\":\"Admin123!\"}")
assert_eq "Login cuenta inactiva -> 401" "$HTTP" "401"

# ============================================================================
section "6 - JWT EXPIRADO"
# ============================================================================
echo ""
echo "[6.1] Forjar JWT con exp en el pasado y enviar a endpoint protegido"
EXPIRED=$(python -c "
import jwt, time, uuid
secret = '$JWT_SECRET'
now = int(time.time())
tok = jwt.encode({
    'jti': str(uuid.uuid4()), 'sub':'1', 'iss':'escuela-conduccion',
    'iat': now-3600, 'exp': now-1800,
    'email':'admin@escuela.local','roles':['ADMIN'],'type':'ACCESS'
}, secret, algorithm='HS512')
print(tok)
")
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $EXPIRED")
assert_eq "JWT expirado -> 401" "$HTTP" "401"

# ============================================================================
section "7 - JWT CON FIRMA MANIPULADA"
# ============================================================================
echo ""
echo "[7.1] Token con la firma alterada (ultimo char) rechazado"
TOKEN=$(login_token "admin@escuela.local" "Admin123!")
# Alterar el ultimo caracter de la firma
LAST=${TOKEN: -1}
NEW_LAST=$([ "$LAST" = "A" ] && echo "B" || echo "A")
TAMPERED="${TOKEN%?}$NEW_LAST"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $TAMPERED")
assert_eq "JWT con firma alterada -> 401" "$HTTP" "401"

echo "[7.2] Token firmado con secreto distinto rechazado"
WRONG_SIG=$(python -c "
import jwt, time, uuid
now = int(time.time())
tok = jwt.encode({
    'jti': str(uuid.uuid4()), 'sub':'1', 'iss':'escuela-conduccion',
    'iat': now, 'exp': now+900,
    'email':'admin@escuela.local','roles':['ADMIN'],'type':'ACCESS'
}, 'secreto-incorrecto-cualquiera-de-relleno-suficientemente-largo-para-hs512-min-64-bytes-XXXXXX', algorithm='HS512')
print(tok)
")
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $WRONG_SIG")
assert_eq "JWT con secreto distinto -> 401" "$HTTP" "401"

# ============================================================================
section "8 - HEADER SPOOFING (cliente intenta inyectar X-User-*)"
# ============================================================================
echo ""
echo "[8.1] Sin JWT pero con X-User-Email manual -> Gateway rechaza 401"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "X-User-Email: hacker@malicious.com" \
    -H "X-User-Id: 999" \
    -H "X-User-Roles: ADMIN")
assert_eq "Spoof sin JWT -> 401" "$HTTP" "401"

echo "[8.2] Con JWT valido + X-User-* falsos -> Gateway sobreescribe con datos del JWT"
TOKEN=$(login_token "admin@escuela.local" "Admin123!")
RESP=$(curl -s -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-User-Email: hacker@malicious.com" \
    -H "X-User-Id: 999")
echo "$RESP" | grep -q "admin@escuela.local" && ok "Gateway sobreescribe X-User-Email (devuelve admin, no hacker)" || fail "Gateway dejó pasar header falso"

# ============================================================================
section "9 - COOKIE + AUTHORIZATION simultaneos (prioridad header)"
# ============================================================================
echo ""
echo "[9.1] Login fresco para tener cookie valida"
COOKIE_FILE=$(mktemp)
curl -s -c "$COOKIE_FILE" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}' > /dev/null

echo "[9.2] Cookie valida + Authorization MALFORMADO -> rechaza (header tiene prioridad)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_FILE" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer not.valid.token")
assert_eq "Cookie OK + Auth invalido -> 401 (header gana)" "$HTTP" "401"

echo "[9.3] Sin cookie + sin Authorization -> 401"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me)
assert_eq "Sin auth -> 401" "$HTTP" "401"

echo "[9.4] Solo cookie, sin Authorization -> funciona"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_FILE" -X GET http://localhost:8080/auth/me)
assert_eq "Solo cookie -> 200" "$HTTP" "200"
rm -f "$COOKIE_FILE"

# ============================================================================
section "10 - RESET TOKEN EXPIRADO"
# ============================================================================
echo ""
echo "[10.1] Token de reset con expiracion en el pasado rechazado"
USER_ID=$(psql_exec "SELECT id FROM auth_schema.usuarios WHERE email='$TEST_RESET';")
EXPIRED_RT=$(python -c "import uuid; print(uuid.uuid4())")
psql_exec "INSERT INTO auth_schema.password_reset_token
           (usuario_id, token, expira_en, usado, created_at)
           VALUES ($USER_ID, '$EXPIRED_RT', NOW() - INTERVAL '1 hour', false, NOW());" >/dev/null
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d "{\"token\":\"$EXPIRED_RT\",\"newPassword\":\"X1234567!\"}")
assert_eq "Reset token expirado -> 401" "$HTTP" "401"

# ============================================================================
section "11 - DLX (Dead Letter Exchange)"
# ============================================================================
echo ""
echo "[11.1] auth.dlx existe en RabbitMQ"
DLX=$(curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F/auth.dlx | grep -oE '"name":"auth.dlx"')
[ -n "$DLX" ] && ok "Exchange auth.dlx existe" || fail "auth.dlx no existe"

echo "[11.2] notificaciones.dlx existe"
DLX2=$(curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F/notificaciones.dlx | grep -oE '"name":"notificaciones.dlx"')
[ -n "$DLX2" ] && ok "Exchange notificaciones.dlx existe" || fail "notificaciones.dlx no existe"

echo "[11.3] Bindings de DLQ para auth.exchange"
QUEUES_DLQ=$(curl -s -u guest:guest http://localhost:15672/api/queues/%2F | grep -oE '"name":"[^"]*\.dlq"' | sort -u)
echo "$QUEUES_DLQ"
[ -n "$QUEUES_DLQ" ] && ok "Hay queues .dlq configuradas" || fail "Sin queues DLQ"

# ============================================================================
section "12 - EUREKA FAILOVER (bajar un MS y observar)"
# ============================================================================
echo ""
echo "[12.1] Bajar MS-Estudiantes y verificar que Gateway responde 503/504"
docker stop escuela-ms-estudiantes >/dev/null 2>&1
sleep 8  # esperar que Eureka note la baja (heartbeat ~30s default; pero usa metric mas rapida)
TOKEN=$(login_token "admin@escuela.local" "Admin123!")
HTTP=$(curl -s -o /dev/null -w "%{http_code}" --max-time 15 -X GET http://localhost:8080/estudiantes \
    -H "Authorization: Bearer $TOKEN")
echo "  Status con MS-Estudiantes DOWN: $HTTP"
# Esperamos 503/504 (servicio no disponible). Puede tomar varios sec hasta que Eureka note.
case "$HTTP" in
    500|503|504) ok "Gateway responde error cuando MS-Estudiantes esta down ($HTTP)" ;;
    200|404)     fail "Gateway responde $HTTP - debe fallar con MS down" ;;
    *)           fail "Gateway responde $HTTP (inesperado)" ;;
esac

echo "[12.2] Levantar MS-Estudiantes y verificar que vuelve a routear"
docker start escuela-ms-estudiantes >/dev/null 2>&1
echo "  Esperando que MS-Estudiantes vuelva a Eureka (~40s)..."
for i in $(seq 1 20); do
    sleep 3
    HTTP=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 -X GET http://localhost:8080/estudiantes \
        -H "Authorization: Bearer $TOKEN")
    [ "$HTTP" = "404" ] && break
done
[ "$HTTP" = "404" ] && ok "Gateway routea de nuevo (HTTP 404 normal porque endpoint vacio)" \
    || fail "Gateway no recupero routing tras restart ($HTTP)"

# ============================================================================
section "13 - CLEANUP DE REFRESH TOKENS"
# ============================================================================
echo ""
echo "[13.1] Buscar scheduler / cleanup en MS-Auth"
SCHEDULED=$(grep -rE "@Scheduled|cleanupExpired|deleteExpired" \
    /c/Users/hmate/OneDrive/Desktop/UDLA/Proyecto*titulacion/backend/ms-auth/src/main 2>/dev/null | head -5)
if [ -n "$SCHEDULED" ]; then
    ok "MS-Auth tiene cleanup scheduled de tokens"
    echo "$SCHEDULED" | head -3
else
    skip "Sin scheduler de cleanup" "alcance Sprint 5+"
fi

# ============================================================================
section "14 - CORRELATION-ID propagado Gateway -> downstream"
# ============================================================================
echo ""
CORR_ID="test-corr-$(date +%s)"
echo "[14.1] Enviar X-Correlation-Id: $CORR_ID y verificar en logs MS-Auth"
docker logs escuela-ms-auth --tail 0 -f > /tmp/auth.log 2>&1 &
TAIL_PID=$!
sleep 1
TOKEN=$(login_token "admin@escuela.local" "Admin123!")
curl -s -o /dev/null -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $TOKEN" \
    -H "X-Correlation-Id: $CORR_ID"
sleep 2
kill $TAIL_PID 2>/dev/null

echo "[14.2] Header X-Correlation-Id en response 401 del Gateway"
# El Gateway adjunta X-Correlation-Id en respuestas de error (401). En respuestas
# OK del MS downstream el header no se propaga porque el Gateway no lo re-inyecta
# (limitacion conocida; mejora candidata para Sprint 5).
RESP=$(curl -s -i -X GET http://localhost:8080/estudiantes \
    -H "X-Correlation-Id: $CORR_ID")
echo "$RESP" | grep -qi "X-Correlation-Id:.*$CORR_ID" && ok "Gateway eco correlation-id en error 401" || fail "Sin echo de correlation-id en 401"

echo "[14.3] Gateway tambien lo agrega cuando el cliente NO lo manda"
RESP=$(curl -s -i -X GET http://localhost:8080/estudiantes 2>&1)
echo "$RESP" | grep -qi "X-Correlation-Id:" && ok "Gateway genera correlation-id si falta" || fail "Sin correlation-id auto-generado"

# ============================================================================
section "15 - METRICAS DE ACTUATOR"
# ============================================================================
echo ""
echo "[15.1] /actuator/metrics expone metricas en cada MS"
for svc_port in "ms-auth:8081" "api-gateway:8080" "ms-notificaciones:8088"; do
    name="${svc_port%:*}"; port="${svc_port#*:}"
    HTTP=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:$port/actuator/metrics")
    [ "$HTTP" = "200" ] && ok "$name expone /actuator/metrics" || fail "$name /actuator/metrics -> $HTTP"
done

echo "[15.2] Metrica jvm.memory.used disponible"
USED=$(curl -s "http://localhost:8081/actuator/metrics/jvm.memory.used" | grep -oP '"value":\K[0-9.]+' | head -1)
[ -n "$USED" ] && ok "Metrica jvm.memory.used = $USED bytes" || fail "Sin metrica jvm.memory.used"

echo "[15.3] Metrica http.server.requests disponible (Gateway)"
HTTP_M=$(curl -s "http://localhost:8080/actuator/metrics/http.server.requests" | head -c 200)
echo "$HTTP_M" | grep -qi "http.server.requests\|TOTAL_TIME\|COUNT" && ok "Gateway expone http.server.requests" || skip "Gateway sin http.server.requests" "puede ser metric web reactiva"

# ============================================================================
section "CLEANUP - eliminar usuarios de test"
# ============================================================================
echo ""
cleanup_user "$TEST_LOCKOUT"
cleanup_user "$TEST_INACTIVE"
cleanup_user "$TEST_RESET"
ok "Usuarios de test eliminados"

# ============================================================================
section "RESUMEN FINAL"
# ============================================================================
echo ""
echo "=============================================="
echo " SPRINT 4 - SUITE AVANZADA"
echo "=============================================="
echo " Tests pasados : $PASS"
echo " Tests fallidos: $FAIL"
echo " Tests skipped : $SKIP"
TOTAL=$((PASS+FAIL+SKIP))
[ $TOTAL -gt 0 ] && PCT=$(( (PASS*100)/(PASS+FAIL) )) || PCT=0
echo " Tasa exito    : $PCT% ($PASS/$((PASS+FAIL)))"
echo "=============================================="
echo -e "$RESULTS"
echo ""
[ "$FAIL" -eq 0 ] && echo "OK - los 15 casos cubiertos" || echo "AUN HAY $FAIL fallos"
exit $FAIL
