# Deber Jenkins - CI/CD Pipeline (Funcional)

---

## Objetivo del deber

Implementar un pipeline de Jenkins en un repositorio privado que cumpla con los siguientes 4 requisitos:

1. **Dividir el pipeline en etapas claras**
2. **Utilizar pipelines modulares y reutilizables**
3. **Integracion continua por rama**
4. **Pipelines en paralelo**

---

## Estructura del proyecto

```
jenkins-deber/
|-- docker-compose.yml         <- Levanta Jenkins en Docker
|-- Jenkinsfile                <- Pipeline declarativo principal
|-- pipeline/                  <- Modulos REUTILIZABLES (cargados con load())
|   |-- build.groovy
|   |-- test.groovy
|   `-- report.groovy
|-- tests/                     <- Scripts de pruebas
|   |-- unit-tests.sh
|   `-- integration-tests.sh
|-- src/
|   `-- app.js                 <- Codigo de ejemplo a probar
`-- README.md
```

---

## Como levantar Jenkins (todo desde Docker)

### 1. Iniciar el contenedor

```bash
cd jenkins-deber
docker compose up -d
```

### 2. Obtener la contrasena inicial

```bash
docker exec jenkins-deber cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Abrir Jenkins en el navegador

```
http://localhost:8090
```

### 4. Configuracion inicial (Wizard)

1. Pegar la contrasena inicial
2. Elegir **"Install suggested plugins"** (espera ~3 min)
3. Crear usuario admin (ej: admin / admin)
4. Confirmar URL: `http://localhost:8090/`
5. Click "Start using Jenkins"

---

## Como crear el job en Jenkins

### Opcion A: Repo local montado (mas rapido para la demo)

El `docker-compose.yml` monta esta carpeta dentro del contenedor en `/var/repos/jenkins-deber`, asi se puede usar como "repo" sin tener que subir nada a GitHub.

1. En Jenkins: **New Item** -> nombre: `jenkins-deber` -> tipo: **Pipeline** -> OK
2. En la seccion **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `/var/repos/jenkins-deber`
   - Branch: `*/main` (o la rama que quieras)
   - Script Path: `Jenkinsfile`
3. Save -> **Build Now**

### Opcion B: Repositorio privado en GitHub (para demostrar CI por rama real)

1. Subir esta carpeta a un repo PRIVADO de GitHub
2. En Jenkins: **New Item** -> tipo: **Multibranch Pipeline**
3. Branch Source: **GitHub** -> agregar credencial (Personal Access Token)
4. Build Configuration -> Mode: **by Jenkinsfile** -> Path: `Jenkinsfile`
5. Save -> Jenkins escaneara automaticamente todas las ramas (`main`, `develop`, `feature/*`)

---

## Como cumple cada requisito

### 1. Etapas claras
El `Jenkinsfile` esta dividido en 5 etapas numeradas y visibles en el "Stage View" de Jenkins:

| # | Etapa            | Que hace                                    |
|---|------------------|---------------------------------------------|
| 1 | Checkout         | Descarga codigo del repo                    |
| 2 | Build            | Compila / prepara el proyecto               |
| 3 | Test (Paralelo)  | Pruebas unitarias e integracion en paralelo |
| 4 | Deploy           | Despliega segun la rama                     |
| 5 | Reporte          | Imprime resumen final                       |

### 2. Pipelines modulares y reutilizables
La carpeta `pipeline/` contiene scripts Groovy cargados dinamicamente con `load()`:

```groovy
def build = load 'pipeline/build.groovy'
build.ejecutar()
```

Los modulos:
- `build.groovy` -> reutilizable en cualquier proyecto que necesite build
- `test.groovy` -> recibe parametro (`unit` o `integration`)
- `report.groovy` -> genera reporte estandar

### 3. Integracion continua por rama
Las etapas de Deploy usan la directiva `when { branch '...' }`:

```groovy
stage('4. Deploy a Desarrollo') {
    when { branch 'develop' }   // solo se ejecuta en develop
    ...
}
stage('4. Deploy a Produccion') {
    when { branch 'main' }      // solo se ejecuta en main
    ...
}
```

### 4. Pipelines en paralelo
Dentro de la etapa **3. Test** se usa el bloque `parallel { }`:

```groovy
stage('3. Test (Paralelo)') {
    parallel {
        stage('Unit Tests')        { ... }
        stage('Integration Tests') { ... }
    }
}
```

En el "Stage View" de Jenkins se vera claramente que ambas etapas corren al mismo tiempo.

---

## Las 2 pruebas implementadas

### Test 1 - Unitarias (`tests/unit-tests.sh`)
- Verifica que `src/app.js` exista
- Verifica que contenga la funcion `sumar`

### Test 2 - Integracion (`tests/integration-tests.sh`)
- Verifica estructura de carpetas (src, tests, pipeline)
- Verifica que el `Jenkinsfile` exista en la raiz

---

## Diagrama del flujo

```
   [Push a rama]
         |
         v
   +----------------+
   | 1. Checkout    |
   +-------+--------+
           |
           v
   +----------------+
   | 2. Build       |
   +-------+--------+
           |
           v
   +-----------------------------------+
   | 3. Test (PARALELO)                |
   |  +----------+    +-------------+  |
   |  |  Unit    |    | Integration |  |
   |  +----------+    +-------------+  |
   +-----------------+-----------------+
                     |
                     v
   +----------------------------+
   | 4. Deploy (segun rama)     |
   |   develop -> DEV           |
   |   main    -> PROD          |
   +-------+--------------------+
           |
           v
   +----------------+
   | 5. Reporte     |
   +----------------+
```

---

## Comandos utiles

```bash
# Ver logs de Jenkins
docker logs -f jenkins-deber

# Reiniciar Jenkins
docker compose restart

# Apagar Jenkins
docker compose down

# Apagar Y borrar todo (limpiar al final del deber)
docker compose down -v
```

---

## Nota
Este proyecto es **temporal**, solo para el deber. No forma parte del proyecto de titulacion.
