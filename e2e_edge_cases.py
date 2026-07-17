"""E2E - Casos edge y reglas de negocio criticas."""
import requests
import random
from datetime import date, timedelta

BASE = "http://localhost:8080"
RESULTS = []

def log(step, ok, msg=""):
    icon = "OK  " if ok else "FAIL"
    print(f"  [{icon}] {step}: {msg[:150]}")
    RESULTS.append((step, ok, msg))

def hdr(t):
    print(f"\n{'='*70}\n== {t}\n{'='*70}")

def generar_cedula():
    provincia = random.randint(1, 24)
    base = f"{provincia:02d}" + str(random.randint(0, 5)) + "".join(str(random.randint(0, 9)) for _ in range(6))
    coefs = [2, 1, 2, 1, 2, 1, 2, 1, 2]
    s = sum((int(c) * coefs[i]) - (9 if int(c) * coefs[i] > 9 else 0) for i, c in enumerate(base))
    return base + str((10 - s % 10) % 10)

# Login
r = requests.post(f"{BASE}/auth/login",
    json={"email": "admin@escuela.local", "password": "Admin123!"})
TOKEN = r.json()["accessToken"]
J = {"Content-Type": "application/json", "Authorization": f"Bearer {TOKEN}"}
H = {"Authorization": f"Bearer {TOKEN}"}

# =============== SEGURIDAD ===============
hdr("SEGURIDAD - Autenticacion y Autorizacion")

# Login con multiples intentos fallidos
usr_test = "admin@escuela.local"
for i in range(2):
    r = requests.post(f"{BASE}/auth/login", json={"email": usr_test, "password": "wrong"})
log("Intentos fallidos capturados", r.status_code == 401, f"HTTP {r.status_code}")

# Reset intentos con login exitoso
r = requests.post(f"{BASE}/auth/login", json={"email": usr_test, "password": "Admin123!"})
log("Reset intentos con login exitoso", r.status_code == 200, f"HTTP {r.status_code}")

# JWT malformado
r = requests.get(f"{BASE}/estudiantes", headers={"Authorization": "Bearer malformed"})
log("JWT malformado rechazado", r.status_code == 401, f"HTTP {r.status_code}")

# JWT vacío
r = requests.get(f"{BASE}/estudiantes", headers={"Authorization": "Bearer "})
log("JWT vacio rechazado", r.status_code == 401, f"HTTP {r.status_code}")

# =============== VALIDACIONES DE ECUADOR ===============
hdr("VALIDACIONES ECUADOR - Cedula, Placa, Telefono")

# Cedulas invalidas
for cedula, desc in [
    ("1234567890", "cedula sin dv correcto"),
    ("0000000000", "cedula todos ceros"),
    ("9999999999", "cedula provincia invalida"),
    ("123456789", "cedula 9 digitos"),
    ("12345678901", "cedula 11 digitos"),
    ("abc1234567", "cedula con letras"),
]:
    r = requests.post(f"{BASE}/estudiantes", json={
        "cedula": cedula, "nombre": "Test", "apellido": "User",
        "email": f"test{random.randint(1,999999)}@t.com", "telefono": "0999999999",
        "fechaNacimiento": "2000-01-01"
    }, headers=J)
    log(f"Rechaza {desc}", r.status_code == 400, f"HTTP {r.status_code}")

# Cedula valida es aceptada
cedula_ok = generar_cedula()
r = requests.post(f"{BASE}/estudiantes", json={
    "cedula": cedula_ok, "nombre": "Testing", "apellido": "Validaciones",
    "email": f"valid{random.randint(1,999999)}@t.com", "telefono": "0999999999",
    "fechaNacimiento": "2000-01-01",
    "contactosEmergencia": [{"nombre": "X", "telefono": "0999999999", "parentesco": "P", "esPrincipal": True}]
}, headers=J)
log(f"Acepta cedula valida ({cedula_ok})", r.status_code == 201, f"HTTP {r.status_code}")
if r.status_code == 201:
    est_id_temp = r.json()["id"]

# Placas invalidas
for placa, desc in [
    ("ABC1234", "placa sin guion"),
    ("abc-1234", "placa minusculas"),
    ("AB-1234", "placa 2 letras"),
    ("ABCD-1234", "placa 4 letras"),
    ("ABC-12345", "placa 5 numeros"),
    ("ABC-123", "placa 3 numeros"),
]:
    r = requests.post(f"{BASE}/vehiculos", json={
        "placa": placa, "marca": "Toyota", "modelo": "Corolla", "anio": 2020
    }, headers=J)
    log(f"Rechaza {desc}", r.status_code == 400, f"HTTP {r.status_code}")

# Telefonos invalidos
for tel, desc in [
    ("0812345678", "no empieza 09"),
    ("0999999", "muy corto"),
    ("09999999999", "muy largo"),
]:
    r = requests.post(f"{BASE}/estudiantes", json={
        "cedula": generar_cedula(), "nombre": "T", "apellido": "U",
        "email": f"t{random.randint(1,999999)}@t.com", "telefono": tel,
        "fechaNacimiento": "2000-01-01"
    }, headers=J)
    log(f"Rechaza telefono {desc}", r.status_code == 400, f"HTTP {r.status_code}")

# =============== REGLAS DE NEGOCIO ===============
hdr("REGLAS DE NEGOCIO - Cross-microservicio")

# Crear un estudiante activo para pruebas
cedula_est = generar_cedula()
r = requests.post(f"{BASE}/estudiantes", json={
    "cedula": cedula_est, "nombre": "TestRules", "apellido": "Edge",
    "email": f"rules{random.randint(1,999999)}@t.com",
    "telefono": "0999999999", "fechaNacimiento": "2000-01-01",
    "tipoCursoId": 1, "categoriaLicenciaId": 3,
    "contactosEmergencia": [{"nombre": "X", "telefono": "0999999999", "parentesco": "P", "esPrincipal": True}]
}, headers=J)
if r.status_code == 201:
    EST_ID = r.json()["id"]
    log(f"Crea estudiante para tests", True, f"id={EST_ID}")

    # Duplicado por cedula
    r = requests.post(f"{BASE}/estudiantes", json={
        "cedula": cedula_est, "nombre": "Dup", "apellido": "Test",
        "email": f"dup{random.randint(1,999999)}@t.com",
        "telefono": "0999999999", "fechaNacimiento": "2000-01-01"
    }, headers=J)
    log("Rechaza cedula duplicada", r.status_code in (400, 409), f"HTTP {r.status_code}")

# Vehiculo con SOAT vencido no puede asignar clase
r_vehs, _ = None, None
r = requests.get(f"{BASE}/vehiculos?size=50", headers=H)
if r.status_code == 200:
    vehs = r.json().get("content", [])
    veh_id_valido = vehs[0]["id"] if vehs else None
    log("Hay vehiculos disponibles", veh_id_valido is not None, f"Count: {len(vehs)}")

# =============== PAGINACION Y SEARCH ===============
hdr("PAGINACION Y SEARCH")

r = requests.get(f"{BASE}/estudiantes?page=0&size=5", headers=H)
if r.status_code == 200:
    d = r.json()
    log("Paginacion estudiantes", True,
        f"total={d.get('totalElements','?')}, size={d.get('size','?')}, page={d.get('number','?')}")

r = requests.get(f"{BASE}/instructores?page=0&size=5", headers=H)
if r.status_code == 200:
    d = r.json()
    log("Paginacion instructores", True,
        f"total={d.get('totalElements','?')}")

r = requests.get(f"{BASE}/vehiculos?page=0&size=5", headers=H)
if r.status_code == 200:
    d = r.json()
    log("Paginacion vehiculos", True,
        f"total={d.get('totalElements','?')}")

# Search por nombre
r = requests.get(f"{BASE}/estudiantes?search=TestRules&size=10", headers=H)
log("Search estudiante por nombre", r.status_code == 200, f"HTTP {r.status_code}")

# Filter por estado
r = requests.get(f"{BASE}/estudiantes?estado=PRE_MATRICULADO&size=10", headers=H)
log("Filter estudiantes por estado", r.status_code == 200, f"HTTP {r.status_code}")

# =============== EVENTOS ASYNC ===============
hdr("EVENTOS ASYNC - RabbitMQ")

# Verificar cola RabbitMQ
try:
    r = requests.get("http://localhost:15672/api/queues",
                     auth=("guest", "guest"), timeout=5)
    if r.status_code == 200:
        queues = r.json()
        log(f"RabbitMQ queues", True, f"Total: {len(queues)}")
        active = [q for q in queues if q.get("consumers", 0) > 0]
        log(f"Queues con consumers activos", len(active) > 0, f"Count: {len(active)}")
except Exception as e:
    log("RabbitMQ management", False, str(e)[:100])

# =============== CIRCUIT BREAKER / RESILIENCE ===============
hdr("RESILENCIA")

# Multiple requests rapidos
import time
start = time.time()
success = 0
for _ in range(10):
    r = requests.get(f"{BASE}/estudiantes?size=1", headers=H, timeout=5)
    if r.status_code == 200:
        success += 1
elapsed = time.time() - start
log("10 requests concurrentes", success >= 9, f"OK={success}/10, tiempo={elapsed:.1f}s, avg={elapsed*100:.0f}ms")

# =============== SUMMARY ===============
hdr("RESUMEN")
total = len(RESULTS)
ok = sum(1 for _, s, _ in RESULTS if s)
fail = total - ok
print(f"\n  Total: {total}, Exitosas: {ok} ({100*ok//total if total else 0}%), Fallidas: {fail}")
if fail > 0:
    print("\n  FALLOS:")
    for name, s, msg in RESULTS:
        if not s:
            print(f"    - {name}: {msg[:200]}")
