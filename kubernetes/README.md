# Kubernetes — Escuela de Conducción

Deploy del sistema completo (14 componentes) en un cluster Kubernetes.

## Arquitectura K8s

```
Namespace: escuela-conduccion
│
├─ 20-infra (persistente, StatefulSet + PVC)
│   ├─ postgres (10Gi RWO)
│   ├─ rabbitmq (5Gi RWO)
│   └─ minio    (20Gi RWO)
│
├─ 30-platform (stateless)
│   ├─ eureka     (1 replica, service discovery)
│   └─ api-gateway (2 replicas, JWT + routing)
│
├─ 40-apps (stateless, 1 replica c/u)
│   ├─ ms-auth · ms-estudiantes · ms-instructores · ms-vehiculos
│   ├─ ms-asignaciones · ms-cobros · ms-reportes · ms-notificaciones
│   └─ frontend (Vue SPA + nginx)
│
└─ 50-ingress (nginx-ingress)
    /            -> frontend
    /api/*       -> api-gateway
```

## Estructura de archivos

```
kubernetes/
└─ manifests/
   ├─ 00-namespace/      namespace.yaml
   ├─ 10-config/         configmap + secrets (template)
   ├─ 20-infra/          postgres, rabbitmq, minio
   ├─ 30-platform/       eureka, gateway
   ├─ 40-apps/           8 MS + frontend
   ├─ 50-ingress/        ingress nginx
   └─ kustomization.yaml índice + tags de imágenes
```

---

## ✅ Lo que YA está listo (yo lo hice)

- 19 manifiestos completos (Namespace, ConfigMap, 4 Secrets, 3 StatefulSets,
  10 Deployments, 15 Services, Ingress).
- Kustomization.yaml con orden de aplicación e imágenes centralizadas.
- Health probes en todos los pods.
- Resource requests/limits (para caber en 2 nodes A1.Flex).
- Persistent Volume Claims para postgres/rabbitmq/minio.

## 📋 Lo que tenés que hacer VOS (checklist)

### 1) Crear cluster OKE en Oracle Cloud (30 min)

En la consola web Oracle:

1. Menú ☰ → **Developer Services → Containers & Artifacts → Kubernetes Clusters (OKE)**.
2. **Create cluster** → **Quick Create** (más simple).
3. Configuración:
   - **Name**: `escuela-conduccion-oke`
   - **Compartment**: (root o el que uses)
   - **Kubernetes API endpoint**: **Public endpoint**
   - **Kubernetes worker nodes**: **Private workers**
   - **Shape**: `VM.Standard.A1.Flex` (**ARM, gratuito hasta 4 OCPU / 24 GB**)
   - **OCPUs**: 2 por nodo
   - **Memory (GB)**: 12 por nodo
   - **Number of nodes**: 2 (total 4 OCPU / 24 GB, entra en el free tier)
   - **Image**: Oracle Linux 8
4. **Create cluster** — tarda ~10-15 min en provisionar.

### 2) Instalar herramientas locales (10 min)

En tu Windows/WSL:

```bash
# kubectl
curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client

# OCI CLI (para bajar kubeconfig)
bash -c "$(curl -L https://raw.githubusercontent.com/oracle/oci-cli/master/scripts/install/install.sh)"
oci setup config    # sigue el wizard, necesitas Tenancy OCID, User OCID, Region

# Helm (para instalar nginx-ingress y cert-manager)
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### 3) Bajar kubeconfig del cluster (5 min)

Desde la consola de OKE:
1. Entrá al cluster → botón **Access Cluster**.
2. Elegí **Local Access** → copiá el comando de `oci ce cluster create-kubeconfig` que te muestra.
3. Ejecutá ese comando en tu terminal. Ejemplo:
   ```bash
   oci ce cluster create-kubeconfig \
     --cluster-id ocid1.cluster.oc1.mx-queretaro-1.aaaaaaa... \
     --file $HOME/.kube/config \
     --region mx-queretaro-1 \
     --token-version 2.0.0
   ```
4. Verifica:
   ```bash
   kubectl get nodes
   # Debe listar 2 nodos "Ready"
   ```

### 4) Pushear las imágenes a GHCR (15 min)

Los manifiestos usan `ghcr.io/kynsofttita-com/proyecto-titulacion-udla/*`. Hay
que pushear las imágenes desde tu VM (donde ya están construidas).

En tu Windows/WSL, generá un GitHub PAT con scope `write:packages`:
- https://github.com/settings/tokens → Generate → scopes: `write:packages`, `read:packages`.

Luego en la VM Oracle (donde ya corre el docker-compose):

```bash
ssh -i ~/.ssh/oracle/titulacion-prod.key ubuntu@160.34.220.63

# Login a GHCR
echo "ghp_TUTOKENAQUI" | sudo docker login ghcr.io -u Hmateo205 --password-stdin

# Tag y push de cada imagen
GHCR_REPO="ghcr.io/kynsofttita-com/proyecto-titulacion-udla"
for svc in eureka api-gateway ms-auth ms-estudiantes ms-instructores \
           ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones frontend; do
  sudo docker tag docker-${svc}:latest ${GHCR_REPO}/${svc}:latest
  sudo docker push ${GHCR_REPO}/${svc}:latest
done
```

Nota: el package en GHCR se crea privado por default. Andá a
`https://github.com/orgs/Kynsofttita-com/packages` → cada package →
**Package settings** → **Change visibility → Public**. O si preferís que sea
privado, hay que crear un `imagePullSecret` en el cluster.

### 5) Editar los secrets antes de aplicar (5 min)

Editá `kubernetes/manifests/10-config/secrets.yaml` y cambiá los valores
placeholder por reales:

- `POSTGRES_PASSWORD` — password fuerte (mínimo 32 chars).
- `RABBITMQ_DEFAULT_PASS` — password fuerte.
- `MINIO_ROOT_PASSWORD` — password fuerte.
- `JWT_SECRET` — string de 64 chars random. Generá con:
  `openssl rand -base64 64`
- `MAIL_USERNAME`, `MAIL_PASSWORD` — los de Mailtrap (ya los tenés del deploy
  Docker).

**No commitear este archivo con secretos reales.** Considerá agregar a
`.gitignore` o usar Sealed Secrets (más adelante).

### 6) Instalar nginx-ingress-controller (5 min)

```bash
kubectl create namespace ingress-nginx
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install ingress-nginx ingress-nginx/ingress-nginx \
  --namespace ingress-nginx \
  --set controller.service.type=LoadBalancer

# Espera a que Oracle asigne una IP publica al LoadBalancer
kubectl -n ingress-nginx get svc -w
# Cuando aparezca EXTERNAL-IP (no <pending>), Ctrl+C
```

Anotate esa IP pública — es la nueva URL del sistema (reemplaza a la
`160.34.220.63` del docker-compose).

### 7) Aplicar los manifiestos (2 min)

```bash
cd ~/proyecto-titulacion-udla   # o donde clones el repo
git checkout feat/kubernetes    # o main si ya se mergeó
kubectl apply -k kubernetes/manifests/

# Verificar
kubectl -n escuela-conduccion get pods -w
```

Los 14 pods van a arrancar en secuencia. Postgres primero, después
Eureka, después el resto. **Tarda ~5-8 min** que todo pase a `Running` +
`Ready 1/1`.

### 8) Verificar

```bash
# Todos deben estar Ready
kubectl -n escuela-conduccion get pods

# Ver eureka
kubectl -n escuela-conduccion port-forward svc/eureka 8761:8761
# http://localhost:8761/ — debe listar los 8 MS + gateway

# Ver la app via ingress
kubectl -n ingress-nginx get svc ingress-nginx-controller
# EXTERNAL-IP → abrí http://<IP>/ en el navegador
```

Login con las credenciales por defecto que estén en la BD (si migraste los
datos del docker-compose) o el admin seed.

---

## Troubleshooting

**Pods en `ImagePullBackOff`**
```bash
kubectl -n escuela-conduccion describe pod <pod-name>
```
Suele ser que la imagen es privada en GHCR — cambia visibility a Public
(paso 4) o crea un `imagePullSecret`.

**Pods en `CrashLoopBackOff`**
```bash
kubectl -n escuela-conduccion logs <pod-name>
```
Suele ser DB no lista todavía. Los MS reintentan solos — esperá 2-3 min.
Si persiste, revisa `SPRING_DATASOURCE_*` en el ConfigMap/Secret.

**Postgres no arranca**
```bash
kubectl -n escuela-conduccion get pvc
```
Si el PVC está `Pending`, es que Oracle no tiene el StorageClass `oci-bv`.
Ejecuta:
```bash
kubectl get storageclass
kubectl patch storageclass oci-bv -p '{"metadata": {"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
```

**El ingress no expone la IP**
Oracle tarda 2-3 min en provisionar el LoadBalancer. Si sigue `<pending>` >
10 min, revisa los eventos:
```bash
kubectl -n ingress-nginx describe svc ingress-nginx-controller
```

---

## Migrar datos del docker-compose

Los datos actuales viven en volúmenes de la VM Oracle. Para migrarlos al
cluster K8s:

```bash
# En la VM
sudo docker exec proyecto-postgres pg_dump -U postgres proyecto_db \
  | gzip > /tmp/proyecto_db.sql.gz
scp -i ~/.ssh/oracle/titulacion-prod.key ubuntu@160.34.220.63:/tmp/proyecto_db.sql.gz .

# En tu máquina, restore al pod postgres del cluster
kubectl -n escuela-conduccion cp proyecto_db.sql.gz postgres-0:/tmp/
kubectl -n escuela-conduccion exec postgres-0 -- \
  bash -c "gunzip -c /tmp/proyecto_db.sql.gz | psql -U postgres proyecto_db"
```

MinIO no necesita migración inmediata (los archivos son opcionales) pero se
puede replicar con `mc mirror`.

---

## Estado actual (2026-07-20)

- ✅ Docker Compose corriendo en la VM Oracle (160.34.220.63) — no lo tocamos.
- 🔜 Kubernetes: **manifiestos listos**, falta que hagas los 8 pasos de arriba.

Cuando termines los pasos, avisame y verifico que todo esté OK. Después
podemos armar ArgoCD encima para GitOps.
