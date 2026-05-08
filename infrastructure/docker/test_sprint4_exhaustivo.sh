#!/bin/bash
# =============================================================================
# Sprint 4 - SUITE EXHAUSTIVA con curl
# Cubre: T4.1 (JWT), T4.2 (MS-Auth), T4.3 (Gateway), T4.4 (Eureka), T4.5 (Eventos)
# =============================================================================
set -u

PASS=0
FAIL=0
RESULTS=""

ok()   { echo "  PASS: $1"; PASS=$((PASS+1)); RESULTS="$RESULTS\nPASS  | $1"; }
fail() { echo "  FAIL: $1"; FAIL=$((FAIL+1)); RESULTS="$RESULTS\nFAIL  | $1"; }

assert_eq() {
    local label="$1"; local actual="$2"; local expected="$3"
    if [ "$actual" = "$expected" ]; then
        ok "$label  -> HTTP $actual"
    else
        fail "$label  -> HTTP $actual (esperado $expected)"
    fi
}

decode_jwt_payload() {
    local payload=$(echo "$1" | cut -d. -f2)
    local pad=$(( 4 - ${#payload} % 4 ))
    [ $pad -lt 4 ] && payload="${payload}$(printf '=%.0s' $(seq 1 $pad))"
    echo "$payload" | tr '_-' '/+' | base64 -d 2>/dev/null
}

decode_jwt_header() {
    local header=$(echo "$1" | cut -d. -f1)
    local pad=$(( 4 - ${#header} % 4 ))
    [ $pad -lt 4 ] && header="${header}$(printf '=%.0s' $(seq 1 $pad))"
    echo "$header" | tr '_-' '/+' | base64 -d 2>/dev/null
}

section() {
    echo ""
    echo "=============================================="
    echo " $1"
    echo "=============================================="
}

# ============================================================================
section "T4.4 - SERVICE DISCOVERY (Eureka)"
# ============================================================================
echo ""
echo "[T4.4-1] Eureka esta UP (puerto 8761)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8761/actuator/health)
assert_eq "Eureka /actuator/health" "$HTTP" "200"

echo ""
echo "[T4.4-2] Servicios registrados en Eureka (esperados: 9)"
EUREKA_APPS=$(curl -s -H "Accept: application/json" http://localhost:8761/eureka/apps | grep -oE '"name":"[^"]+"' | sort -u)
echo "$EUREKA_APPS"
EXPECTED_SERVICES=("API-GATEWAY" "MS-AUTH" "MS-ESTUDIANTES" "MS-INSTRUCTORES" "MS-VEHICULOS" "MS-ASIGNACIONES" "MS-COBROS" "MS-REPORTES" "MS-NOTIFICACIONES")
for svc in "${EXPECTED_SERVICES[@]}"; do
    if echo "$EUREKA_APPS" | grep -qi "\"$svc\""; then
        ok "Eureka registra $svc"
    else
        fail "Eureka NO registra $svc"
    fi
done

# ============================================================================
section "T4.2 - MS-AUTH ENDPOINTS"
# ============================================================================
echo ""
echo "[T4.2-1] POST /auth/login con credenciales validas"
RESP=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}')
HTTP=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
assert_eq "POST /auth/login" "$HTTP" "200"
ACCESS=$(echo "$BODY" | grep -oP '"accessToken"\s*:\s*"\K[^"]+')
REFRESH=$(echo "$BODY" | grep -oP '"refreshToken"\s*:\s*"\K[^"]+')
ACCESS_EXP=$(echo "$BODY" | grep -oP '"accessTokenExpiresInSeconds"\s*:\s*\K[0-9]+')
REFRESH_EXP=$(echo "$BODY" | grep -oP '"refreshTokenExpiresInSeconds"\s*:\s*\K[0-9]+')
[ -n "$ACCESS" ] && ok "Body incluye accessToken" || fail "Body sin accessToken"
[ -n "$REFRESH" ] && ok "Body incluye refreshToken" || fail "Body sin refreshToken"
[ "$ACCESS_EXP" = "900" ] && ok "Access token expira en 900s (15 min)" || fail "Access exp = $ACCESS_EXP (esperado 900)"
[ "$REFRESH_EXP" = "604800" ] && ok "Refresh token expira en 604800s (7 dias)" || fail "Refresh exp = $REFRESH_EXP"

echo ""
echo "[T4.2-2] Body de login incluye user info"
echo "$BODY" | grep -q '"email":"admin@escuela.local"' && ok "user.email correcto" || fail "user.email"
echo "$BODY" | grep -q '"roles":\["ADMIN"\]' && ok "user.roles=[ADMIN]" || fail "user.roles"
echo "$BODY" | grep -q '"nombre":"Administrador"' && ok "user.nombre" || fail "user.nombre"

echo ""
echo "[T4.2-3] Cookies HttpOnly devueltas en login"
COOKIE_HEADERS=$(curl -s -i -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}' | grep -i "set-cookie")
echo "$COOKIE_HEADERS" | grep -qi "accessToken=" && ok "Set-Cookie accessToken" || fail "No Set-Cookie accessToken"
echo "$COOKIE_HEADERS" | grep -qi "refreshToken=" && ok "Set-Cookie refreshToken" || fail "No Set-Cookie refreshToken"
echo "$COOKIE_HEADERS" | grep -qi "HttpOnly" && ok "Cookies HttpOnly" || fail "Cookies sin HttpOnly"

echo ""
echo "[T4.2-4] Login con password invalido rechaza 401"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"WrongPassword"}')
assert_eq "Login password invalido" "$HTTP" "401"

echo ""
echo "[T4.2-5] Login con email inexistente rechaza 401 (anti-enumeration)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"noexiste@local.com","password":"X"}')
assert_eq "Login email inexistente" "$HTTP" "401"

echo ""
echo "[T4.2-6] POST /auth/refresh con token valido (rotacion)"
RESP=$(curl -s -w "\n%{http_code}" -X POST http://localhost:8080/auth/refresh \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$REFRESH\"}")
HTTP=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
assert_eq "POST /auth/refresh" "$HTTP" "200"
NEW_ACCESS=$(echo "$BODY" | grep -oP '"accessToken"\s*:\s*"\K[^"]+')
NEW_REFRESH=$(echo "$BODY" | grep -oP '"refreshToken"\s*:\s*"\K[^"]+')
[ "$NEW_ACCESS" != "$ACCESS" ] && ok "Access token rotado" || fail "Access token NO rotado"
[ "$NEW_REFRESH" != "$REFRESH" ] && ok "Refresh token rotado" || fail "Refresh token NO rotado"

echo ""
echo "[T4.2-7] Refresh token YA usado rechazado (proteccion contra replay)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/refresh \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$REFRESH\"}")
assert_eq "Refresh ya usado rechazado" "$HTTP" "401"

echo ""
echo "[T4.2-8] GET /auth/me con access token valido"
RESP=$(curl -s -w "\n%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $NEW_ACCESS")
HTTP=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')
assert_eq "GET /auth/me" "$HTTP" "200"
echo "$BODY" | grep -q "admin@escuela.local" && ok "/me devuelve email" || fail "/me sin email"
echo "$BODY" | grep -q '"roles":\["ADMIN"\]' && ok "/me devuelve roles" || fail "/me sin roles"

echo ""
echo "[T4.2-9] POST /auth/logout"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/logout \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$NEW_REFRESH\"}")
assert_eq "POST /auth/logout" "$HTTP" "204"

echo ""
echo "[T4.2-10] Logout idempotente (segundo logout con mismo token)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/logout \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$NEW_REFRESH\"}")
assert_eq "Logout idempotente (token revocado)" "$HTTP" "204"

echo ""
echo "[T4.2-11] POST /auth/forgot-password con email existente (202 anti-enum)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local"}')
assert_eq "Forgot-password email existente" "$HTTP" "202"

echo ""
echo "[T4.2-12] POST /auth/forgot-password con email inexistente (202 anti-enum)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d '{"email":"noexiste@local.com"}')
assert_eq "Forgot-password email inexistente devuelve mismo status" "$HTTP" "202"

echo ""
echo "[T4.2-13] POST /auth/reset-password con token UUID inexistente"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d '{"token":"00000000-0000-0000-0000-000000000000","newPassword":"NuevoPass123!"}')
assert_eq "Reset-password UUID inexistente -> 401" "$HTTP" "401"

echo ""
echo "[T4.2-14] POST /auth/reset-password con UUID malformado"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d '{"token":"NO-ES-UUID","newPassword":"NuevoPass123!"}')
assert_eq "Reset-password UUID malformado -> 400" "$HTTP" "400"

# ============================================================================
section "T4.1 - FRAMEWORK JWT (common-security)"
# ============================================================================
echo ""
echo "[T4.1-1] JWT firmado con HS512"
LOGIN_RESP=$(curl -s -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}')
TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"accessToken"\s*:\s*"\K[^"]+')
REFRESH_T=$(echo "$LOGIN_RESP" | grep -oP '"refreshToken"\s*:\s*"\K[^"]+')

JWT_HEADER=$(decode_jwt_header "$TOKEN")
echo "  Header: $JWT_HEADER"
echo "$JWT_HEADER" | grep -q '"alg":"HS512"' && ok "Algoritmo HS512" || fail "Algoritmo no es HS512"

echo ""
echo "[T4.1-2] JWT contiene claims requeridos (jti, sub, iss, iat, exp, email, roles, type)"
JWT_PAYLOAD=$(decode_jwt_payload "$TOKEN")
echo "  Payload: $JWT_PAYLOAD"
for claim in '"jti"' '"sub"' '"iss"' '"iat"' '"exp"' '"email"' '"roles"' '"type"'; do
    echo "$JWT_PAYLOAD" | grep -q "$claim" && ok "Claim $claim presente" || fail "Claim $claim ausente"
done

echo ""
echo "[T4.1-3] Access token tiene type=ACCESS"
echo "$JWT_PAYLOAD" | grep -q '"type":"ACCESS"' && ok "type=ACCESS" || fail "type no es ACCESS"

echo ""
echo "[T4.1-4] Refresh token tiene type=REFRESH"
REFRESH_PAYLOAD=$(decode_jwt_payload "$REFRESH_T")
echo "$REFRESH_PAYLOAD" | grep -q '"type":"REFRESH"' && ok "type=REFRESH" || fail "type no es REFRESH"

echo ""
echo "[T4.1-5] Issuer = escuela-conduccion"
echo "$JWT_PAYLOAD" | grep -q '"iss":"escuela-conduccion"' && ok "iss=escuela-conduccion" || fail "iss incorrecto"

echo ""
echo "[T4.1-6] Refresh token NO sirve como access token (rechazo en endpoint protegido)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X GET http://localhost:8080/auth/me \
    -H "Authorization: Bearer $REFRESH_T")
assert_eq "Refresh token rechazado en endpoint protegido" "$HTTP" "401"

# ============================================================================
section "T4.3 - API GATEWAY (JWT Filter + Routing + CORS)"
# ============================================================================
echo ""
echo "[T4.3-1] Gateway esta UP (puerto 8080)"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
assert_eq "Gateway /actuator/health" "$HTTP" "200"

echo ""
echo "[T4.3-2] Ruta protegida sin token rechaza 401"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/estudiantes)
assert_eq "GET /estudiantes sin token" "$HTTP" "401"

echo ""
echo "[T4.3-3] Ruta protegida con token MALFORMADO rechaza 401"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/estudiantes \
    -H "Authorization: Bearer not.a.real.token")
assert_eq "Token malformado rechazado" "$HTTP" "401"

echo ""
echo "[T4.3-4] Ruta protegida con token VALIDO pasa al MS downstream"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/estudiantes \
    -H "Authorization: Bearer $TOKEN")
[ "$HTTP" = "404" ] || [ "$HTTP" = "200" ] && ok "Token valido enrutado (HTTP $HTTP)" || fail "Token valido HTTP $HTTP"

echo ""
echo "[T4.3-5] Body de error 401 sigue formato RFC 7807 (problem+json)"
RESP_401=$(curl -s -i http://localhost:8080/estudiantes)
echo "$RESP_401" | grep -qi "Content-Type:.*application/problem+json" && ok "Content-Type problem+json" || fail "Content-Type no es problem+json"
echo "$RESP_401" | grep -q '"status":401' && ok "Body incluye status:401" || fail "Body sin status"
echo "$RESP_401" | grep -q '"correlationId"' && ok "Body incluye correlationId" || fail "Body sin correlationId"
echo "$RESP_401" | grep -qi "X-Correlation-Id:" && ok "Header X-Correlation-Id presente" || fail "Sin X-Correlation-Id"

echo ""
echo "[T4.3-6] CORS preflight con origin permitido"
RESP=$(curl -s -i -X OPTIONS http://localhost:8080/auth/login \
    -H "Origin: http://localhost:5173" \
    -H "Access-Control-Request-Method: POST")
echo "$RESP" | grep -qi "Access-Control-Allow-Origin: http://localhost:5173" && ok "CORS Allow-Origin localhost:5173" || fail "CORS Allow-Origin"
echo "$RESP" | grep -qi "Access-Control-Allow-Credentials: true" && ok "CORS Allow-Credentials true" || fail "CORS Allow-Credentials"
echo "$RESP" | grep -qi "Access-Control-Allow-Methods" && ok "CORS Allow-Methods presente" || fail "CORS Allow-Methods"

echo ""
echo "[T4.3-7] CORS rechaza origin no whitelisted"
RESP=$(curl -s -i -X OPTIONS http://localhost:8080/auth/login \
    -H "Origin: http://malicious.com" \
    -H "Access-Control-Request-Method: POST")
if ! echo "$RESP" | grep -qi "Access-Control-Allow-Origin: http://malicious.com"; then
    ok "CORS rechaza origin malicioso"
else
    fail "CORS aceptó origin malicioso"
fi

echo ""
echo "[T4.3-8] Token enviado via Cookie tambien funciona"
COOKIE_FILE=$(mktemp)
curl -s -c "$COOKIE_FILE" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local","password":"Admin123!"}' > /dev/null
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIE_FILE" http://localhost:8080/auth/me)
assert_eq "GET /auth/me via Cookie" "$HTTP" "200"
rm -f "$COOKIE_FILE"

echo ""
echo "[T4.3-9] Path publico /actuator/health pasa sin token"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
assert_eq "Public path /actuator/health" "$HTTP" "200"

# ============================================================================
section "T4.5 - LOCKOUT + EVENTOS (MS-Notificaciones)"
# ============================================================================
echo ""
echo "[T4.5-1] RabbitMQ esta UP (puerto 5672)"
HTTP=$(curl -s -u guest:guest -o /dev/null -w "%{http_code}" http://localhost:15672/api/overview)
assert_eq "RabbitMQ Management UI" "$HTTP" "200"

echo ""
echo "[T4.5-2] Exchange auth.exchange existe en RabbitMQ"
EXCHANGE=$(curl -s -u guest:guest http://localhost:15672/api/exchanges/%2F/auth.exchange | grep -oE '"name":"auth.exchange"')
[ -n "$EXCHANGE" ] && ok "Exchange auth.exchange existe" || fail "Exchange auth.exchange no existe"

echo ""
echo "[T4.5-3] MS-Notificaciones registrado en Eureka y healthy"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8088/actuator/health)
assert_eq "MS-Notificaciones /actuator/health" "$HTTP" "200"

echo ""
echo "[T4.5-4] Logins fallidos para email inexistente NO bloquean (anti-DoS)"
for i in 1 2 3 4 5; do
    curl -s -o /dev/null -X POST http://localhost:8080/auth/login \
        -H "Content-Type: application/json" \
        -d '{"email":"noexiste@local.com","password":"X"}'
done
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"noexiste@local.com","password":"X"}')
assert_eq "Email inexistente NO se bloquea (sigue 401)" "$HTTP" "401"

echo ""
echo "[T4.5-5] Forgot-password publica evento (counter publish_in en exchange auth.exchange)"
curl -s -o /dev/null -X POST http://localhost:8080/auth/forgot-password \
    -H "Content-Type: application/json" \
    -d '{"email":"admin@escuela.local"}'
sleep 2
PUBLISHED=$(curl -s -u guest:guest "http://localhost:15672/api/exchanges/%2F/auth.exchange" 2>/dev/null | grep -oP '"publish_in":\K[0-9]+' | head -1)
echo "  Total mensajes publicados en exchange auth.exchange: ${PUBLISHED:-0}"
[ "${PUBLISHED:-0}" -gt 0 ] && ok "Eventos publicados en exchange auth.exchange" || fail "No hay eventos publicados"

# ============================================================================
section "T4.2 - VALIDACION DE INPUT (DTOs con @Valid)"
# ============================================================================
echo ""
echo "[T4.2-V1] Login sin email rechaza 400"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"password":"X"}')
assert_eq "Login sin email" "$HTTP" "400"

echo ""
echo "[T4.2-V2] Login con email malformado rechaza 400"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"no-es-email","password":"X"}')
assert_eq "Login email malformado" "$HTTP" "400"

echo ""
echo "[T4.2-V3] Reset-password sin newPassword rechaza 400"
HTTP=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8080/auth/reset-password \
    -H "Content-Type: application/json" \
    -d '{"token":"00000000-0000-0000-0000-000000000000"}')
assert_eq "Reset sin newPassword" "$HTTP" "400"

# ============================================================================
section "RESUMEN FINAL"
# ============================================================================
echo ""
echo "=============================================="
echo " RESULTADO SPRINT 4"
echo "=============================================="
echo " Tests pasados: $PASS"
echo " Tests fallidos: $FAIL"
TOTAL=$((PASS+FAIL))
PCT=0
[ $TOTAL -gt 0 ] && PCT=$((PASS*100/TOTAL))
echo " Tasa exito : $PCT% ($PASS/$TOTAL)"
echo "=============================================="
echo -e "$RESULTS"
echo ""
if [ "$FAIL" -eq 0 ]; then
    echo "SPRINT 4 -> 100% (T4.1 + T4.2 + T4.3 + T4.4 + T4.5 OK)"
    exit 0
else
    echo "SPRINT 4 -> AUN HAY $FAIL fallos"
    exit 1
fi
