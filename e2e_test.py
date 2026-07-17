"""E2E Test completo del sistema de escuela de conduccion."""
import requests
import json
import sys
import time
import random
from datetime import date, timedelta

BASE = "http://localhost:8080"
RESULTS = []
REFRESH_TOKEN = None

def log(step, ok, msg=""):
    icon = "OK  " if ok else "FAIL"
    print(f"  [{icon}] {step}: {msg[:180]}")
    RESULTS.append((step, ok, msg))

def hdr(t):
    print(f"\n{'='*70}\n== {t}\n{'='*70}")

def get_list(url, headers):
    r = requests.get(url, headers=headers)
    if r.status_code != 200:
        return None, r
    data = r.json()
    if isinstance(data, list):
        return data, r
    if isinstance(data, dict) and "content" in data:
        return data["content"], r
    return data, r

def generar_cedula():
    """Genera cedula ecuatoriana valida (persona natural).
    - Provincia: 01-24 (2 dig)
    - 3er digito: 0-5 (persona natural)
    - Digitos 4-9: aleatorios (6 dig)
    - Digito verificador: algoritmo modulo 10 con coefs 2,1,2,1,2,1,2,1,2
    """
    provincia = random.randint(1, 24)
    base = f"{provincia:02d}"
    base += str(random.randint(0, 5))  # 3er digito < 6
    base += "".join(str(random.randint(0, 9)) for _ in range(6))
    # Calcular digito verificador
    coefs = [2, 1, 2, 1, 2, 1, 2, 1, 2]
    s = 0
    for i, c in enumerate(base):
        v = int(c) * coefs[i]
        if v > 9:
            v -= 9
        s += v
    dv = (10 - s % 10) % 10
    return base + str(dv)

# ==================== 1. AUTENTICACION ====================
hdr("PASO 1: Autenticacion")

r = requests.post(f"{BASE}/auth/login",
    json={"email": "admin@escuela.local", "password": "Admin123!"})
if r.status_code != 200:
    log("Login admin", False, f"HTTP {r.status_code}: {r.text[:200]}")
    sys.exit(1)

data = r.json()
TOKEN = data["accessToken"]
REFRESH_TOKEN = data["refreshToken"]
log("Login ADMIN", True, f"JWT valido, expira en {data['accessTokenExpiresInSeconds']}s")
log("User info", True, f"ID={data['user']['id']}, roles={data['user']['roles']}")

H = {"Authorization": f"Bearer {TOKEN}"}
J = {"Content-Type": "application/json", **H}

# Password invalido
r = requests.post(f"{BASE}/auth/login",
    json={"email": "admin@escuela.local", "password": "wrong"})
log("Login password invalido (SEC)", r.status_code == 401, f"HTTP {r.status_code}")

# Refresh con body correcto
r = requests.post(f"{BASE}/auth/refresh",
    json={"refreshToken": REFRESH_TOKEN}, headers=J)
log("POST /auth/refresh", r.status_code == 200, f"HTTP {r.status_code}")

# ==================== 2. CATALOGOS ====================
hdr("PASO 2: Catalogos Iniciales")

cats, r = get_list(f"{BASE}/categorias-licencia?size=50", H)
if cats:
    log("GET /categorias-licencia", True, f"Total: {len(cats)}")
    for c in cats[:3]:
        print(f"        - {c.get('codigo')}: {c.get('descripcion','')[:50]}")
    CAT_B_ID = next((c["id"] for c in cats if c.get("codigo") == "B"), None)
else:
    log("GET /categorias-licencia", False, f"HTTP {r.status_code}")
    CAT_B_ID = None

tcs, r = get_list(f"{BASE}/tipos-curso?size=50", H)
if tcs:
    log("GET /tipos-curso", True, f"Total: {len(tcs)}")
    for c in tcs[:3]:
        print(f"        - ID={c.get('id')}: {c.get('nombre')} | {c.get('duracionTotalHoras')}h")
    TC_ID = next((c["id"] for c in tcs if "Basico Auto" in c.get("nombre", "")), tcs[0]["id"])
else:
    log("GET /tipos-curso", False, f"HTTP {r.status_code}")
    TC_ID = None

concs, r = get_list(f"{BASE}/conceptos-facturacion?size=50", H)
if concs:
    log("GET /conceptos-facturacion", True, f"Total: {len(concs)}")
    CONC_ID = next((c["id"] for c in concs if "Basico" in c.get("nombre", "")), concs[0]["id"])
else:
    log("GET /conceptos-facturacion", False, f"HTTP {r.status_code}")
    CONC_ID = None

# ==================== 3. CREAR INSTRUCTOR ====================
hdr("PASO 3: Crear Instructor + Horario + Certificacion")

CEDULA_INS = generar_cedula()
EMAIL_INS = f"instructor{random.randint(1000,9999)}@escuela.com"

instr_data = {
    "cedula": CEDULA_INS,
    "nombre": "Juan Carlos",
    "apellido": "Perez Gomez",
    "email": EMAIL_INS,
    "telefono": "0991234567",
    "direccion": "Av. Amazonas N-40",
    "fechaNacimiento": "1985-05-15",
    "licenciaNumero": CEDULA_INS,
    "licenciaCategoria": "B",
    "licenciaEmision": "2015-01-15",
    "licenciaCaducidad": "2030-01-15",
    "fechaContratacion": "2023-01-01",
    "salarioMensual": 800.00,
    "tipoContrato": "TIEMPO_COMPLETO",
    "horasContratoSemanales": 40,
    "tarifaHora": 10.00
}

r = requests.post(f"{BASE}/instructores", json=instr_data, headers=J)
if r.status_code in (200, 201):
    instructor = r.json()
    INSTR_ID = instructor["id"]
    log("POST /instructores", True, f"id={INSTR_ID}, cedula={CEDULA_INS}")
else:
    log("POST /instructores", False, f"HTTP {r.status_code}: {r.text[:300]}")
    INSTR_ID = None

if INSTR_ID:
    # Disponibilidad semanal: LUNES (1) a VIERNES (5), 8am-5pm
    for dia_num, dia_nombre in [(1,"LUN"),(2,"MAR"),(3,"MIE"),(4,"JUE"),(5,"VIE")]:
        disp_data = {
            "diaSemana": dia_num,
            "horaInicio": "08:00:00",
            "horaFin": "17:00:00"
        }
        r = requests.post(f"{BASE}/instructores/{INSTR_ID}/disponibilidad-semanal",
            json=disp_data, headers=J)
        log(f"POST disp semanal {dia_nombre}", r.status_code in (200, 201),
            f"HTTP {r.status_code}" + (f": {r.text[:150]}" if r.status_code >= 400 else ""))

    # Certificacion
    cert_data = {
        "tipo": "Curso Vial Nacional",
        "fechaObtencion": "2020-06-15",
        "vigenciaHasta": "2030-06-15",
        "entidadEmisora": "ANT Ecuador"
    }
    r = requests.post(f"{BASE}/instructores/{INSTR_ID}/certificaciones",
        json=cert_data, headers=J)
    log("POST certificacion", r.status_code in (200, 201), f"HTTP {r.status_code}")

    r = requests.get(f"{BASE}/instructores/{INSTR_ID}/certificaciones", headers=H)
    log("GET certificaciones", r.status_code == 200,
        f"HTTP {r.status_code}, total={len(r.json()) if r.status_code == 200 else '?'}")

# ==================== 4. CREAR VEHICULO ====================
hdr("PASO 4: Crear Vehiculo")

combs, r = get_list(f"{BASE}/tipos-combustible", H)
if combs:
    log("GET /tipos-combustible", True, f"Total: {len(combs)}")
    TIPO_COMB_ID = combs[0]["id"]
else:
    log("GET /tipos-combustible", False, f"HTTP {r.status_code}")
    TIPO_COMB_ID = None

rand = random.randint(1000, 9999)
plate_letters = "".join(chr(65 + random.randint(0, 25)) for _ in range(3))
PLACA = f"{plate_letters}-{rand}"

vehiculo_data = {
    "placa": PLACA,
    "marca": "Toyota",
    "modelo": "Corolla",
    "anio": 2020,
    "vin": f"1HGBH41JXMN{random.randint(100000,999999)}"[:17],
    "color": "Blanco",
    "kilometraje": 45000,
    "estado": "ACTIVO",
    "soatVencimiento": (date.today() + timedelta(days=180)).isoformat(),
    "revisionVencimiento": (date.today() + timedelta(days=200)).isoformat(),
    "fechaCompra": "2020-06-15",
    "valorCompra": 18500.00,
    "categoriaLicenciaId": CAT_B_ID,
    "tipoCombustibleId": TIPO_COMB_ID,
    "capacidadPasajeros": 5,
    "numeroChasis": f"CHASIS{random.randint(100000,999999)}",
    "numeroMotor": f"MOTOR{random.randint(100000,999999)}"
}
r = requests.post(f"{BASE}/vehiculos", json=vehiculo_data, headers=J)
if r.status_code in (200, 201):
    v = r.json()
    VEH_ID = v["id"]
    log("POST /vehiculos", True, f"id={VEH_ID}, placa={PLACA}")
else:
    log("POST /vehiculos", False, f"HTTP {r.status_code}: {r.text[:300]}")
    VEH_ID = None

# ==================== 5. CREAR ESTUDIANTE ====================
hdr("PASO 5: Crear Estudiante (con contacto de emergencia)")

CEDULA_EST = generar_cedula()
EMAIL_EST = f"estudiante{random.randint(1000,9999)}@escuela.com"

est_data = {
    "cedula": CEDULA_EST,
    "nombre": "Maria Elena",
    "apellido": "Rodriguez Vega",
    "email": EMAIL_EST,
    "telefono": "0987654321",
    "direccion": "Calle 10 de Agosto",
    "fechaNacimiento": "2000-01-10",
    "genero": "F",
    "tipoCursoId": TC_ID,
    "categoriaLicenciaId": CAT_B_ID,
    "observaciones": "Test E2E",
    "contactosEmergencia": [
        {
            "nombre": "Pedro Rodriguez",
            "telefono": "0999888777",
            "parentesco": "Padre",
            "esPrincipal": True
        }
    ]
}
r = requests.post(f"{BASE}/estudiantes", json=est_data, headers=J)
if r.status_code in (200, 201):
    e = r.json()
    EST_ID = e["id"]
    log("POST /estudiantes", True, f"id={EST_ID}, estado={e.get('estado','?')}, sitPago={e.get('situacionPago','?')}")
else:
    log("POST /estudiantes", False, f"HTTP {r.status_code}: {r.text[:300]}")
    EST_ID = None

if EST_ID:
    r = requests.get(f"{BASE}/estudiantes/{EST_ID}", headers=H)
    log("GET estudiante detail", r.status_code == 200, f"HTTP {r.status_code}")

# ==================== 6. FACTURA ====================
hdr("PASO 6: Crear Factura")

if EST_ID and CONC_ID:
    factura_data = {
        "estudianteId": EST_ID,
        "conceptoFacturacionId": CONC_ID,
        "montoOriginal": 250.00,
        "fechaVencimiento": (date.today() + timedelta(days=30)).isoformat(),
        "tipoPago": "CONTADO",
        "observaciones": "Factura E2E"
    }
    r = requests.post(f"{BASE}/facturas", json=factura_data, headers=J)
    if r.status_code in (200, 201):
        f = r.json()
        FACT_ID = f["id"]
        log("POST /facturas", True, f"id={FACT_ID}, monto=${f.get('montoOriginal','?')}, estado={f.get('estado','?')}")
    else:
        log("POST /facturas", False, f"HTTP {r.status_code}: {r.text[:300]}")
        FACT_ID = None

    # Listar facturas del estudiante
    r = requests.get(f"{BASE}/facturas/estudiante/{EST_ID}", headers=H)
    log("GET facturas por estudiante", r.status_code == 200, f"HTTP {r.status_code}")
else:
    FACT_ID = None

# ==================== 7. PAGO ====================
hdr("PASO 7: Registrar Pago")

if FACT_ID:
    pago_data = {
        "facturaId": FACT_ID,
        "monto": 250.00,
        "metodoPago": "EFECTIVO",
        "referenciaTransaccion": f"REC-{random.randint(1000,9999)}",
        "observaciones": "Pago total"
    }
    r = requests.post(f"{BASE}/pagos", json=pago_data, headers=J)
    if r.status_code in (200, 201):
        p = r.json()
        PAGO_ID = p["id"]
        log("POST /pagos", True, f"id={PAGO_ID}, monto=${p.get('monto','?')}")
    else:
        log("POST /pagos", False, f"HTTP {r.status_code}: {r.text[:300]}")

    # Ver situacion pago
    time.sleep(1)
    r = requests.get(f"{BASE}/facturas/estudiante/{EST_ID}/situacion-pago", headers=H)
    log("GET situacion-pago", r.status_code == 200,
        f"HTTP {r.status_code}: {r.text[:150]}" if r.status_code == 200 else f"HTTP {r.status_code}")

# ==================== 8. TRANSICION ESTADO ====================
hdr("PASO 8: Verificar Transicion Automatica (async)")

if EST_ID:
    time.sleep(4)
    r = requests.get(f"{BASE}/estudiantes/{EST_ID}", headers=H)
    if r.status_code == 200:
        e = r.json()
        log("Estado tras pago", True, f"estado={e.get('estado','?')}, sitPago={e.get('situacionPago','?')}")
        if e.get('estado') in ('MATRICULADO', 'CURSANDO'):
            log("Transicion PRE_MATRICULADO -> MATRICULADO", True, "Evento async OK")
        else:
            log("Transicion estado", False, f"Esperado MATRICULADO, obtenido: {e.get('estado','?')}")

# ==================== 9. ASIGNACION ====================
hdr("PASO 9: Crear Asignacion")

if EST_ID and INSTR_ID and VEH_ID:
    today = date.today()
    days_ahead = (0 - today.weekday()) % 7
    if days_ahead == 0:
        days_ahead = 7
    proximo_lunes = today + timedelta(days=days_ahead)

    asig_data = {
        "estudianteId": EST_ID,
        "instructorId": INSTR_ID,
        "vehiculoId": VEH_ID,
        "fecha": proximo_lunes.isoformat(),
        "horaInicio": "10:00:00",
        "horaFin": "11:00:00",
        "observaciones": "Primera clase practica"
    }
    r = requests.post(f"{BASE}/asignaciones", json=asig_data, headers=J)
    if r.status_code in (200, 201):
        a = r.json()
        ASIG_ID = a["id"]
        log("POST /asignaciones", True, f"id={ASIG_ID}, fecha={proximo_lunes.isoformat()}")
    else:
        log("POST /asignaciones", False, f"HTTP {r.status_code}: {r.text[:300]}")
        ASIG_ID = None
else:
    ASIG_ID = None

# ==================== 10. INICIAR/FINALIZAR CLASE ====================
hdr("PASO 10: Iniciar y Finalizar Clase")

if ASIG_ID:
    iniciar_data = {"kmInicial": 45000}
    r = requests.patch(f"{BASE}/asignaciones/{ASIG_ID}/iniciar", json=iniciar_data, headers=J)
    log("PATCH iniciar", r.status_code in (200, 204),
        f"HTTP {r.status_code}: {r.text[:200] if r.status_code >= 400 else 'OK'}")

    finalizar_data = {
        "kmFinal": 45080,
        "observacionesRecorrido": "Estudiante realizo bien"
    }
    r = requests.patch(f"{BASE}/asignaciones/{ASIG_ID}/finalizar", json=finalizar_data, headers=J)
    log("PATCH finalizar", r.status_code in (200, 204),
        f"HTTP {r.status_code}: {r.text[:200] if r.status_code >= 400 else 'OK'}")

    r = requests.get(f"{BASE}/asignaciones/{ASIG_ID}/recorrido", headers=H)
    log("GET recorrido", r.status_code == 200, f"HTTP {r.status_code}")

# ==================== 11. SINC CROSS-MS ====================
hdr("PASO 11: Sincronizacion Cross-MS")

if VEH_ID:
    time.sleep(3)
    r = requests.get(f"{BASE}/vehiculos/{VEH_ID}", headers=H)
    if r.status_code == 200:
        v = r.json()
        km = v.get('kilometraje', v.get('kilometrajeActual', 0))
        log("KM vehiculo sync", km > 45000, f"KM actual={km}")

if EST_ID:
    r = requests.get(f"{BASE}/estudiantes/{EST_ID}", headers=H)
    if r.status_code == 200:
        e = r.json()
        min_c = e.get('minutosCompletados', 0)
        log("Minutos estudiante sync", min_c > 0, f"minutos={min_c}, estado={e.get('estado','?')}")

# ==================== 12. REPORTES ====================
hdr("PASO 12: Reportes (POST)")

reporte_req = {
    "tipoReporte": "OPERATIVO",
    "desde": (date.today() - timedelta(days=30)).isoformat(),
    "hasta": date.today().isoformat(),
    "filtros": {}
}

for endpoint in ["/reportes/estudiantes-activos", "/reportes/instructores-horas",
                 "/reportes/vehiculos-soat", "/reportes/ingresos-periodo"]:
    r = requests.post(f"{BASE}{endpoint}", json=reporte_req, headers=J)
    log(f"POST {endpoint}", r.status_code in (200, 201),
        f"HTTP {r.status_code}" + (f": {r.text[:150]}" if r.status_code >= 400 else ""))

# KPIs (GET)
r = requests.get(f"{BASE}/reportes/kpis", headers=H)
log("GET /reportes/kpis", r.status_code == 200, f"HTTP {r.status_code}")

# ==================== 13. NOTIFICACIONES ====================
hdr("PASO 13: Notificaciones")

r = requests.get(f"{BASE}/notificaciones?usuarioId=1", headers=H)
log("GET /notificaciones", r.status_code == 200, f"HTTP {r.status_code}")

# ==================== 14. SEGURIDAD Y VALIDACION ====================
hdr("PASO 14: Seguridad y Validaciones")

# Sin token
r = requests.get(f"{BASE}/estudiantes")
log("SEC: sin token", r.status_code == 401, f"HTTP {r.status_code}")

# Token invalido
r = requests.get(f"{BASE}/estudiantes",
    headers={"Authorization": "Bearer invalid.token"})
log("SEC: token invalido", r.status_code == 401, f"HTTP {r.status_code}")

# Cedula muy corta
r = requests.post(f"{BASE}/estudiantes", json={
    "cedula": "12345", "nombre": "Test", "apellido": "User",
    "email": "test@test.com", "telefono": "0999999999",
    "fechaNacimiento": "2000-01-01"
}, headers=J)
log("VAL: cedula corta", r.status_code == 400, f"HTTP {r.status_code}")

# Email invalido
r = requests.post(f"{BASE}/estudiantes", json={
    "cedula": "1234567890", "nombre": "Test", "apellido": "User",
    "email": "not-email", "telefono": "0999999999",
    "fechaNacimiento": "2000-01-01"
}, headers=J)
log("VAL: email invalido", r.status_code == 400, f"HTTP {r.status_code}")

# Placa invalida
r = requests.post(f"{BASE}/vehiculos", json={
    "placa": "INVALID", "marca": "Toyota", "modelo": "Corolla", "anio": 2020
}, headers=J)
log("VAL: placa invalida", r.status_code == 400, f"HTTP {r.status_code}")

# Duplicado (cedula)
if CEDULA_EST:
    r = requests.post(f"{BASE}/estudiantes", json={
        "cedula": CEDULA_EST, "nombre": "Duplicado", "apellido": "Test",
        "email": f"dup{random.randint(1,999)}@t.com", "telefono": "0999999999",
        "fechaNacimiento": "2000-01-01"
    }, headers=J)
    log("VAL: cedula duplicada", r.status_code in (409, 400), f"HTTP {r.status_code}")

# ==================== RESUMEN ====================
hdr("RESUMEN FINAL")
total = len(RESULTS)
ok = sum(1 for _, s, _ in RESULTS if s)
fail = total - ok

print(f"\n  Total: {total}, Exitosas: {ok} ({100*ok//total if total else 0}%), Fallidas: {fail}")

if fail > 0:
    print("\n  FALLOS:")
    for name, s, msg in RESULTS:
        if not s:
            print(f"    - {name}: {msg[:200]}")

sys.exit(0 if fail == 0 else 1)
