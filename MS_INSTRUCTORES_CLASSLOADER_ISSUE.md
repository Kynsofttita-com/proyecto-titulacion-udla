# MS-Instructores/MS-Asignaciones Bytecode Corruption Issue - RESOLVED

**Date**: 2026-07-17
**Status**: ✅ **RESOLVED**
**Impact**: All 15 containers healthy and operational

## Problem Summary

MS-Instructores and MS-Asignaciones microservices failed to start with `ClassNotFoundException` errors like:
- `ClassNotFoundException: Certificacion`
- `ClassNotFoundException: AsignacionMapper`
- `ClassNotFoundException: InstructorMapper`

Notably, these errors reported class names WITHOUT their fully qualified package names.

## Root Cause

**Corrupted bytecode in `.class` files** — The compiled `.class` files contained embedded compilation error messages (a signature of Eclipse Compiler for Java / ECJ producing "stub" classes with `Unresolved compilation problems`).

Root cause of the corruption: **Race condition during parallel Maven compilation** (`mvn -T 4` or standard multi-module build from root). The MapStruct annotation processor tried to compile the mapper implementations before the source classes they depend on were fully resolved by the compiler.

The corrupted `.class` files contained bytecode that would throw `java.lang.Error` at runtime with the "Unresolved compilation problems" message when introspected by Spring's ClassLoader.

## How to Diagnose

Check any `.class` file with:
```bash
javap -v path/to/File.class 2>&1 | grep -c "Unresolved compilation problems"
```

If output is > 0, the bytecode is corrupted.

## The Fix

**Build modules sequentially**, one at a time, from each module's own directory:

```bash
# Clean everything
rm -rf ~/.m2/repository/com/escuela/
find backend -type d -name "target" -exec rm -rf {} +

# 1. Install parent POM
cd backend && mvn install -N -DskipTests

# 2. Install shared modules
for mod in common-events common-exceptions common-security common-jpa common-validation; do
  (cd shared/$mod && mvn install -DskipTests)
done

# 3. Install service modules ONE AT A TIME
for ms in eureka-server api-gateway ms-auth ms-estudiantes ms-instructores \
          ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
  (cd $ms && mvn install -DskipTests)
done
```

**Do NOT use:**
- `mvn -T 4 install` (parallel compilation) — causes race conditions
- `mvn install` from backend root — MapStruct sometimes corrupts bytecode

## Verification

All bytecode clean:
```bash
for ms in ms-*; do
  count=$(find $ms/target/classes -name "*.class" -exec javap -v {} \; 2>&1 | \
          grep -c "Unresolved compilation problems")
  echo "$ms: $count errors"
done
```

Expected output: all zeros.

## System Status (After Fix)

```
✅ proyecto-postgres       (healthy)  port 5432
✅ proyecto-rabbitmq       (healthy)  port 5672
✅ proyecto-eureka         (healthy)  port 8761
✅ proyecto-gateway        (healthy)  port 8080
✅ proyecto-ms-auth        (healthy)  port 8081
✅ proyecto-ms-estudiantes (healthy)  port 8082
✅ proyecto-ms-instructores (healthy) port 8083  ← FIXED
✅ proyecto-ms-vehiculos   (healthy)  port 8084
✅ proyecto-ms-asignaciones (healthy) port 8085  ← FIXED
✅ proyecto-ms-cobros      (healthy)  port 8086
✅ proyecto-ms-reportes    (healthy)  port 8087
✅ proyecto-ms-notificaciones (healthy) port 8088
✅ proyecto-frontend       (healthy)  port 3000
✅ proyecto-adminer                   port 8089
✅ proyecto-jenkins                   port 8090

TOTAL: 15/15 containers running
```

## Functional Validation

- ✅ Login endpoint: JWT tokens generated correctly
- ✅ MS-Estudiantes: GET /estudiantes → HTTP 200
- ✅ MS-Instructores: GET /instructores → HTTP 200 with paginated response
- ✅ MS-Vehiculos: GET /vehiculos → HTTP 200
- ✅ MS-Asignaciones: GET /asignaciones → HTTP 200
- ✅ All 9 services registered in Eureka
- ✅ API Gateway routing correctly

## Key Learnings

1. **javap -v is your friend**: Use it to detect corrupted bytecode
2. **Sequential builds are safer**: MapStruct + Lombok need careful compilation order
3. **Windows paths with spaces** can exacerbate race conditions in Maven
4. **ECJ stub classes** at runtime is a signal of build-time compilation errors that were suppressed

## Prevention

Add to Jenkins/CI pipeline:
```bash
# Verify no corrupted bytecode in JARs
for jar in backend/*/target/*-SNAPSHOT.jar; do
  count=$(unzip -p $jar 'BOOT-INF/classes/**/*.class' 2>/dev/null | \
          javap -v /dev/stdin 2>&1 | grep -c "Unresolved compilation problems")
  if [ $count -gt 0 ]; then
    echo "CORRUPTED: $jar"
    exit 1
  fi
done
```
