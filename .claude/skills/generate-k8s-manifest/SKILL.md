---
name: generate-k8s-manifest
description: Generate Kubernetes manifests for deploying microservices including Deployment, Service, Ingress, ConfigMap, Secret, HorizontalPodAutoscaler, ServiceAccount, NetworkPolicy, and PodDisruptionBudget. Includes resource limits, health probes, security contexts, and rolling update strategies.
---

# Generate Kubernetes Manifest Skill

Creates production-ready Kubernetes manifests for the project.

## Inputs Needed

Ask the user for:
1. **Service name**: kebab-case (e.g., `ms-auth`)
2. **Image registry + tag**: e.g., `registry.example.com/ms-auth:1.2.3`
3. **Port**: container port (e.g., `8081`)
4. **Replicas**: minimum desired (e.g., `2`)
5. **Resource needs**: CPU/memory requests and limits
6. **Environment**: dev, staging, prod
7. **External access?**: needs Ingress (yes for `api-gateway` only)

## Output

Saved to: `infrastructure/kubernetes/<environment>/<service-name>/`

Files generated:
- `deployment.yaml`
- `service.yaml`
- `configmap.yaml`
- `secret.yaml`
- `hpa.yaml`
- `pdb.yaml`
- `serviceaccount.yaml`
- `ingress.yaml` (only for api-gateway)
- `networkpolicy.yaml`
- `kustomization.yaml`

## Templates

### Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: <service-name>
  namespace: proyecto-<env>
  labels:
    app: <service-name>
    version: v1
    tier: backend
    component: microservice
spec:
  replicas: <replicas>
  revisionHistoryLimit: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: <service-name>
  template:
    metadata:
      labels:
        app: <service-name>
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "<port>"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: <service-name>-sa
      automountServiceAccountToken: false
      
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        runAsGroup: 1000
        fsGroup: 1000
        seccompProfile:
          type: RuntimeDefault
      
      containers:
        - name: <service-name>
          image: <registry>/<service-name>:<tag>
          imagePullPolicy: IfNotPresent
          
          ports:
            - name: http
              containerPort: <port>
              protocol: TCP
          
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: <env>
            - name: SERVER_PORT
              value: "<port>"
            - name: EUREKA_URL
              valueFrom:
                configMapKeyRef:
                  name: <service-name>-config
                  key: eureka-url
            - name: DB_URL
              valueFrom:
                configMapKeyRef:
                  name: <service-name>-config
                  key: db-url
            - name: DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: <service-name>-secrets
                  key: db-username
            - name: DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: <service-name>-secrets
                  key: db-password
            - name: JWT_SECRET
              valueFrom:
                secretKeyRef:
                  name: shared-secrets
                  key: jwt-secret
          
          resources:
            requests:
              memory: "512Mi"
              cpu: "250m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          
          startupProbe:
            httpGet:
              path: /actuator/health
              port: http
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 30
            timeoutSeconds: 5
          
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: http
            initialDelaySeconds: 60
            periodSeconds: 10
            failureThreshold: 3
            timeoutSeconds: 5
          
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: http
            initialDelaySeconds: 30
            periodSeconds: 5
            failureThreshold: 3
            timeoutSeconds: 5
          
          securityContext:
            allowPrivilegeEscalation: false
            readOnlyRootFilesystem: true
            capabilities:
              drop: ["ALL"]
          
          volumeMounts:
            - name: tmp
              mountPath: /tmp
            - name: cache
              mountPath: /home/appuser/.cache
      
      volumes:
        - name: tmp
          emptyDir:
            sizeLimit: 100Mi
        - name: cache
          emptyDir:
            sizeLimit: 100Mi
      
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values: [<service-name>]
                topologyKey: kubernetes.io/hostname
      
      topologySpreadConstraints:
        - maxSkew: 1
          topologyKey: topology.kubernetes.io/zone
          whenUnsatisfiable: ScheduleAnyway
          labelSelector:
            matchLabels:
              app: <service-name>
```

### Service

```yaml
apiVersion: v1
kind: Service
metadata:
  name: <service-name>
  namespace: proyecto-<env>
  labels:
    app: <service-name>
spec:
  type: ClusterIP
  selector:
    app: <service-name>
  ports:
    - name: http
      port: 80
      targetPort: http
      protocol: TCP
```

### ConfigMap

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: <service-name>-config
  namespace: proyecto-<env>
data:
  eureka-url: "http://eureka:8761/eureka"
  db-url: "jdbc:postgresql://postgres:5432/<service>_db"
  rabbitmq-host: "rabbitmq"
  rabbitmq-port: "5672"
  log-level: "INFO"
```

### Secret (template — populate with sealed-secrets in real deployment)

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: <service-name>-secrets
  namespace: proyecto-<env>
type: Opaque
stringData:
  db-username: "<placeholder>"  # populate via Vault / sealed-secrets / external-secrets
  db-password: "<placeholder>"
```

### HorizontalPodAutoscaler

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: <service-name>-hpa
  namespace: proyecto-<env>
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: <service-name>
  minReplicas: 2
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
        - type: Percent
          value: 50
          periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
        - type: Percent
          value: 100
          periodSeconds: 30
        - type: Pods
          value: 2
          periodSeconds: 60
      selectPolicy: Max
```

### PodDisruptionBudget

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: <service-name>-pdb
  namespace: proyecto-<env>
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: <service-name>
```

### ServiceAccount

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: <service-name>-sa
  namespace: proyecto-<env>
automountServiceAccountToken: false
```

### NetworkPolicy

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: <service-name>-netpol
  namespace: proyecto-<env>
spec:
  podSelector:
    matchLabels:
      app: <service-name>
  policyTypes:
    - Ingress
    - Egress
  ingress:
    # Allow from API Gateway
    - from:
        - podSelector:
            matchLabels:
              app: api-gateway
      ports:
        - port: <port>
          protocol: TCP
    # Allow from monitoring (Prometheus)
    - from:
        - namespaceSelector:
            matchLabels:
              name: monitoring
      ports:
        - port: <port>
          protocol: TCP
  egress:
    # DNS
    - to:
        - namespaceSelector: {}
      ports:
        - port: 53
          protocol: UDP
    # PostgreSQL
    - to:
        - podSelector:
            matchLabels:
              app: postgres
      ports:
        - port: 5432
          protocol: TCP
    # RabbitMQ
    - to:
        - podSelector:
            matchLabels:
              app: rabbitmq
      ports:
        - port: 5672
          protocol: TCP
    # Eureka
    - to:
        - podSelector:
            matchLabels:
              app: eureka
      ports:
        - port: 8761
          protocol: TCP
```

### Ingress (only for api-gateway)

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-gateway
  namespace: proyecto-<env>
  annotations:
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/proxy-body-size: "10m"
    nginx.ingress.kubernetes.io/limit-rps: "100"
    nginx.ingress.kubernetes.io/configuration-snippet: |
      add_header X-Frame-Options "DENY" always;
      add_header X-Content-Type-Options "nosniff" always;
      add_header Referrer-Policy "strict-origin-when-cross-origin" always;
spec:
  ingressClassName: nginx
  tls:
    - hosts:
        - api.proyecto.local
      secretName: api-tls-cert
  rules:
    - host: api.proyecto.local
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: api-gateway
                port:
                  number: 80
```

### Kustomization

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

namespace: proyecto-<env>

resources:
  - serviceaccount.yaml
  - configmap.yaml
  - secret.yaml
  - deployment.yaml
  - service.yaml
  - hpa.yaml
  - pdb.yaml
  - networkpolicy.yaml
  # - ingress.yaml  # only for api-gateway

commonLabels:
  app.kubernetes.io/managed-by: kustomize
  app.kubernetes.io/part-of: proyecto-titulacion

images:
  - name: <service-name>
    newName: <registry>/<service-name>
    newTag: <tag>
```

## Workflow

1. **Identify** service requirements (resources, scaling, networking)
2. **Generate** all manifests with proper labels and annotations
3. **Validate** syntax: `kubectl apply --dry-run=client -f .`
4. **Validate** policies: `kubectl --dry-run=server apply -f .`
5. **Lint**: `kube-linter lint .` or `kubeval`
6. **Deploy** to staging first
7. **Verify**:
   ```bash
   kubectl get pods -l app=<service-name> -n proyecto-staging
   kubectl logs -l app=<service-name> -n proyecto-staging --tail=100
   kubectl describe deployment <service-name> -n proyecto-staging
   ```
8. **Test** rolling update: `kubectl rollout restart deployment/<service-name>`
9. **Test** rollback: `kubectl rollout undo deployment/<service-name>`

## Quality Checklist

- [ ] Resource requests AND limits set (avoid OOMKill, fair scheduling)
- [ ] Liveness, readiness, AND startup probes defined
- [ ] runAsNonRoot: true, readOnlyRootFilesystem: true
- [ ] All capabilities dropped
- [ ] No `latest` tag (specific version or SHA)
- [ ] Pod anti-affinity for HA
- [ ] HPA with reasonable min/max
- [ ] PDB to prevent total outage during upgrades
- [ ] NetworkPolicy restricts traffic (zero-trust)
- [ ] ServiceAccount per service (not default)
- [ ] Secrets externalized (sealed-secrets / external-secrets / Vault)
- [ ] Labels follow conventions (app, version, tier, component)
- [ ] Prometheus annotations for scraping

## Anti-Patterns

❌ Don't use `:latest` tag
❌ Don't run as root
❌ Don't skip resource limits
❌ Don't omit liveness/readiness probes
❌ Don't put secrets in plain text
❌ Don't allow all egress (network policy too lax)
❌ Don't use default ServiceAccount
❌ Don't allow privilege escalation

## Notes

- Use **Kustomize** for environment overlays (dev, staging, prod)
- Use **Helm** for parameterized deployments
- Store sensitive data with **Sealed Secrets** or **External Secrets Operator** + Vault
- Always test in staging before prod
- Use **GitOps** (ArgoCD/Flux) for declarative deployments
- Monitor with **Prometheus + Grafana**, log with **Loki/ELK**
