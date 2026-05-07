---
name: generate-dockerfile
description: Generate a multi-stage Dockerfile for Spring Boot 3 (Java 21) microservices or Vue.js 3 frontend applications. Includes security hardening (non-root user, read-only filesystem), JVM container support, health checks, and proper layer caching for optimal build times.
---

# Generate Dockerfile Skill

Creates production-ready Dockerfiles following best practices.

## Inputs Needed

Ask the user for:
1. **Type**: backend (Spring Boot Java) or frontend (Vue/Nginx)
2. **Service name**: for tagging and naming
3. **Port**: exposed port
4. **Special requirements**: any custom JVM options, dependencies, etc.

## Templates

### 1. Spring Boot Backend Dockerfile

```dockerfile
# syntax=docker/dockerfile:1.6

# ============================================================================
# Stage 1: Build with Maven
# ============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Install Maven (alpine doesn't include it by default)
# Or copy mvnw wrapper from project
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Cache Maven dependencies (separate layer for better caching)
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Build application
COPY src ./src
RUN ./mvnw clean package -DskipTests -B --no-transfer-progress \
    && cp target/*.jar /app.jar

# Extract layered JAR for better Docker layer caching
RUN java -Djarmode=layertools -jar /app.jar extract --destination /extracted

# ============================================================================
# Stage 2: Runtime
# ============================================================================
FROM eclipse-temurin:21-jre-alpine

# Install required runtime tools
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Set timezone (Ecuador)
ENV TZ=America/Guayaquil

# Security: create non-root user
RUN addgroup -S -g 1000 appgroup && \
    adduser -S -u 1000 -G appgroup -h /home/appuser appuser

WORKDIR /app

# Copy layers in order from least to most volatile (best caching)
COPY --from=builder --chown=appuser:appgroup /extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /extracted/application/ ./

# Switch to non-root
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -fsS http://localhost:${SERVER_PORT:-8080}/actuator/health/liveness || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="\
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+ParallelRefProcEnabled \
    -XX:+UseStringDeduplication \
    -XX:+ExitOnOutOfMemoryError \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/tmp/heapdump.hprof \
    -Djava.security.egd=file:/dev/./urandom"

# Expose port (informational)
EXPOSE 8080

# Use exec form so signals propagate correctly
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

### 2. Vue.js Frontend Dockerfile

```dockerfile
# syntax=docker/dockerfile:1.6

# ============================================================================
# Stage 1: Build with Vite
# ============================================================================
FROM node:20-alpine AS builder

WORKDIR /build

# Install dependencies (with cache)
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --no-audit --no-fund --prefer-offline

# Copy source and build
COPY . .

ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

# ============================================================================
# Stage 2: Runtime with Nginx
# ============================================================================
FROM nginx:1.25-alpine

# Install runtime tools
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

ENV TZ=America/Guayaquil

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets
COPY --from=builder --chown=nginx:nginx /build/dist /usr/share/nginx/html

# Security: nginx runs as non-root by default in alpine variant
# But we need to make /var/run writable for nginx
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid /var/cache/nginx /etc/nginx /usr/share/nginx/html

USER nginx

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -fsS http://localhost:8080/health || exit 1

CMD ["nginx", "-g", "daemon off;"]
```

### Nginx Config (nginx.conf for frontend)

```nginx
server {
    listen 8080;
    server_name _;

    root /usr/share/nginx/html;
    index index.html;

    # Security headers
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "geolocation=(), microphone=(), camera=()" always;
    
    # Health check endpoint
    location /health {
        access_log off;
        return 200 "ok\n";
        add_header Content-Type text/plain;
    }

    # Static assets with hash → cache forever
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }

    # SPA: try file, then folder, then fallback to index.html
    location / {
        try_files $uri $uri/ /index.html;
        
        # No cache for index.html (always check for updates)
        add_header Cache-Control "no-cache, no-store, must-revalidate" always;
    }

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types
        application/javascript
        application/json
        application/xml
        text/css
        text/plain
        text/xml
        image/svg+xml;

    # Security: block dotfiles
    location ~ /\. {
        deny all;
        access_log off;
    }
}
```

### 3. .dockerignore

For Spring Boot:
```
# Build artifacts
target/
*.iml
.mvn/wrapper/maven-wrapper.jar

# IDE
.idea/
.vscode/
.classpath
.project
.settings/

# Local
*.log
*.tmp
.DS_Store

# Testing
*.exec

# Don't include in image
Dockerfile
docker-compose*.yml
README.md
```

For Vue.js:
```
# Build artifacts
node_modules/
dist/
dist-ssr/
.cache/
.vite/

# IDE
.idea/
.vscode/

# Local
*.log
.DS_Store
.env.local
.env.*.local

# Tests
coverage/
.nyc_output/
cypress/screenshots/
cypress/videos/

# Don't include
Dockerfile
docker-compose*.yml
README.md
```

## Workflow

1. **Identify** target type (backend / frontend)
2. **Read** existing Dockerfiles in repo for consistency
3. **Generate** Dockerfile with multi-stage build
4. **Generate** `.dockerignore` if missing
5. **Test** build:
   ```bash
   docker build -t <service>:dev .
   ```
6. **Test** run:
   ```bash
   docker run --rm -p 8080:8080 <service>:dev
   curl http://localhost:8080/actuator/health  # backend
   curl http://localhost:8080/health             # frontend
   ```
7. **Scan** for vulnerabilities:
   ```bash
   docker scout cves <service>:dev
   # or
   trivy image <service>:dev
   ```
8. **Verify** image size is reasonable:
   ```bash
   docker images <service>:dev
   # Backend target: < 300MB
   # Frontend target: < 50MB
   ```

## Quality Checklist

- [ ] Multi-stage build (small final image)
- [ ] Non-root user
- [ ] Specific base image versions (not `:latest`)
- [ ] Alpine variant when possible
- [ ] Health check defined
- [ ] EXPOSE directive informational
- [ ] Layer caching optimized (deps before source)
- [ ] No secrets in image
- [ ] `.dockerignore` excludes unnecessary files
- [ ] Timezone set
- [ ] JVM container support flags
- [ ] Image size < 300MB (backend), < 50MB (frontend)

## Build Args & Environment Variables

```bash
# Build with custom args
docker build \
    --build-arg VITE_API_BASE_URL=https://api.example.com \
    -t myapp:latest .

# Run with environment overrides
docker run -d \
    -e SPRING_PROFILES_ACTIVE=prod \
    -e DB_URL=jdbc:postgresql://db:5432/mydb \
    -e DB_USERNAME=user \
    -e DB_PASSWORD=secret \
    -p 8080:8080 \
    myapp:latest
```

## Anti-Patterns to Avoid

❌ Don't use `:latest` tag (non-reproducible)
❌ Don't run as root
❌ Don't include build tools in runtime image
❌ Don't bake secrets into the image
❌ Don't COPY entire context (use .dockerignore)
❌ Don't skip health checks
❌ Don't ignore image scanning
❌ Don't use ENTRYPOINT shell form when args expansion isn't needed (signals don't propagate)

## Notes

- For production, sign images with `cosign`
- Generate SBOM (`syft <image>`) and store with image
- Use BuildKit features (`--mount=type=cache`) for faster rebuilds
- Push to private registry, never Docker Hub for proprietary code
- Tag images with git SHA for traceability: `myapp:1.2.3` and `myapp:abc123def`
