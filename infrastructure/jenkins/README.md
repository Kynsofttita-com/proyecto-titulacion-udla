# Jenkins CI — Setup

Jenkins corre como stack independiente del docker-compose principal. Sirve como
CI adicional junto a GitHub Actions (útil para pipelines internos y para
demostrar CI/CD durante la defensa).

## 1. Levantar Jenkins

Desde la VM (donde vive el proyecto):

```bash
cd ~/app/infrastructure/docker
sudo docker compose -f docker-compose.jenkins.yml up -d
```

Espera ~1 min al primer arranque (descarga imagen `jenkins/jenkins:lts-jdk21`,
~450 MB, inicializa el volumen).

Verifica que arrancó:

```bash
sudo docker ps --filter name=proyecto-jenkins
sudo docker logs -f proyecto-jenkins
```

## 2. Obtener la password inicial

```bash
sudo docker exec proyecto-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Copia esa string (32 chars hex).

## 3. Setup wizard

Abrí en el navegador: **http://160.34.220.63:8100/** (o `<IP>:8100`).

1. Pega la password inicial → **Continue**.
2. **Install suggested plugins** — instala el pack estándar (Git, Pipeline,
   Blue Ocean, Docker, etc.). Tarda ~3-5 min.
3. **Create First Admin User**:
   - Username: `admin` (o el que quieras)
   - Password: uno fuerte
   - Full name / Email: los tuyos
4. **Instance Configuration** → dejá la URL default (`http://160.34.220.63:8100/`).
5. **Start using Jenkins**.

## 4. Instalar plugins adicionales

Menú **Manage Jenkins → Plugins → Available plugins**, buscá e instalá:

- **Docker Pipeline** — para que el pipeline pueda usar `agent { docker { image ... } }`.
- **JUnit** — publicar reportes de tests (ya viene con suggested).
- **HTML Publisher** — para reportes JaCoCo (opcional).

Después: **Restart Jenkins after installation** (checkbox al pie).

## 5. Conectar al repo GitHub

Menú **New Item** → nombre `escuela-conduccion-ci` → tipo **Multibranch Pipeline** → **OK**.

Configuración:

- **Branch Sources → Add source → GitHub**:
  - Credentials: **Add → Jenkins** (username + PAT de GitHub con scope `repo`).
  - Repository HTTPS URL: `https://github.com/Kynsofttita-com/proyecto-titulacion-udla`
- **Build Configuration → by Jenkinsfile** (default). Path: `Jenkinsfile` (raíz).
- **Scan Multibranch Pipeline Triggers → Periodically if not otherwise run → 5 min**.

**Save**. Jenkins escanea el repo, detecta ramas con `Jenkinsfile`, y arranca
un build por cada una.

## 6. Ver los builds

- **Blue Ocean** (menú lateral) — vista gráfica del pipeline por stages.
- **Job → Branches** — lista de ramas con status.

## Estructura del Jenkinsfile

El pipeline (`/Jenkinsfile` raíz del repo) tiene 4 stages:

1. **Checkout** — clone del repo, log del commit.
2. **Backend · build + tests** — corre en contenedor `maven:3.9-eclipse-temurin-21`,
   ejecuta `mvn verify` (compile + test + jacoco).
3. **Frontend · build** — contenedor `node:20-alpine`, `npm ci && npm run build`.
   Archiva `frontend/dist` como artefacto.
4. **Docker images** — solo en rama `main` (o forzando `BUILD_DOCKER_IMAGES=true`).
   Compila las 8 imágenes de MS + frontend.

## Notas

- El pipeline **no hace deploy** (por ahora). Solo build + tests. Deploy sigue
  siendo manual vía SSH + `docker compose`. Cuando montemos ArgoCD, éste se
  encarga del deploy.
- **Docker-in-Docker**: el socket `/var/run/docker.sock` está montado dentro
  del contenedor Jenkins. Es seguro para un entorno de un solo usuario, pero
  en prod compartido conviene usar un docker daemon separado (DinD real).
- **Recursos**: Jenkins usa ~500MB RAM idle, sube a 1-2GB durante builds. La
  VM tiene 24GB con margen suficiente.
- **Persistencia**: todo lo que configures (jobs, credenciales, plugins) se
  guarda en el volumen `proyecto-jenkins-home`. Al reiniciar el contenedor
  no se pierde nada.

## Versión "full" del Jenkinsfile

En la raíz del repo hay un `Jenkinsfile.full.example` con la versión completa
que incluye SonarQube, Trivy, OWASP Dependency-Check, ArgoCD y notificaciones
Slack. Requiere configurar todas esas herramientas + credenciales antes de
usarla. Sirve como referencia para el trabajo escrito.

## Borrar todo Jenkins

Si querés empezar de cero:

```bash
sudo docker compose -f docker-compose.jenkins.yml down -v
# El -v también borra el volumen jenkins_home
```
