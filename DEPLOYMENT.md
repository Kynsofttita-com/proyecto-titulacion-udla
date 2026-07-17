# Deployment Guide — Producción

Instrucciones para desplegar el sistema en Oracle Cloud Free Tier o DigitalOcean.

## 🎯 Opciones de Deployment

### Opción A: Oracle Cloud Free Tier (Recomendado)

**Costo:** Gratuito (siempre y cuando cumplas límites)

**Especificaciones:**
- 2 vCPUs ARM
- 12 GB RAM
- 100 GB almacenamiento
- 10 Mbps bandwidth

**Pasos:**

1. **Crear instancia Compute:**
   - Imagen: Ubuntu 22.04 LTS
   - Shape: Ampere (ARM)
   - Crear key pair SSH

2. **Conectar por SSH:**
   ```bash
   ssh -i tu-key.key ubuntu@tu-ip-pública
   ```

3. **Instalar Docker y Docker Compose:**
   ```bash
   curl -fsSL https://get.docker.com -o get-docker.sh
   sudo sh get-docker.sh
   sudo usermod -aG docker ubuntu
   
   sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   sudo chmod +x /usr/local/bin/docker-compose
   ```

4. **Clonar y ejecutar:**
   ```bash
   git clone https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git
   cd proyecto-titulacion/infrastructure/docker
   docker-compose up -d
   ```

### Opción B: DigitalOcean

**Costo:** $6/mes (Droplet básico)

**Pasos similares a Oracle Cloud**

---

## 🔒 Variables de entorno (Producción)

Crear `.env` en `infrastructure/docker/`:

```env
# Database
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<generar-contraseña-segura>
POSTGRES_DB=proyecto_db

# JWT
JWT_SECRET=<generar-con: openssl rand -base64 64>

# Email (Gmail SMTP)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password

# Timezone
TZ=America/Guayaquil

# Nginx (si está frente)
FRONTEND_URL=https://tu-dominio.com
API_URL=https://api.tu-dominio.com
```

---

## 🔐 SSL/TLS (HTTPS)

### Con Let's Encrypt + Nginx Reverse Proxy

```bash
# Instalar certbot
sudo apt-get install certbot python3-certbot-nginx

# Generar certificado
sudo certbot certonly --standalone -d tu-dominio.com

# Crear config Nginx
sudo nano /etc/nginx/sites-available/default
```

**Nginx config:**
```nginx
server {
    listen 443 ssl http2;
    server_name tu-dominio.com;

    ssl_certificate /etc/letsencrypt/live/tu-dominio.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tu-dominio.com/privkey.pem;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name tu-dominio.com;
    return 301 https://$server_name$request_uri;
}
```

---

## 📊 Monitoreo

### Health Checks

```bash
# Frontend
curl https://tu-dominio.com

# API Gateway
curl https://api.tu-dominio.com/actuator/health

# Eureka
curl https://api.tu-dominio.com:8761/actuator/health
```

### Logs

```bash
# Ver logs en tiempo real
docker-compose logs -f

# Logs persistentes
docker-compose logs > /var/log/proyecto-titulacion.log
```

### Monitoreo avanzado

Integrar con:
- **Prometheus**: Métricas de aplicación
- **Grafana**: Dashboards
- **ELK Stack**: Logging centralizado

---

## 🔄 Updates y Mantenimiento

### Actualizar código

```bash
cd /home/ubuntu/proyecto-titulacion
git pull origin main
cd infrastructure/docker
docker-compose up -d --build
```

### Backup de BD

```bash
# Backup completo
docker-compose exec postgresql pg_dump -U postgres proyecto_db > backup-$(date +%Y%m%d).sql

# Restaurar
docker-compose exec -T postgresql psql -U postgres proyecto_db < backup-20260717.sql
```

### Limpieza de espacio

```bash
# Ver uso
docker system df

# Limpiar imágenes/contenedores no usados
docker system prune -a
```

---

## 🚨 Troubleshooting

### Containers mueren constantemente

```bash
# Revisar logs
docker-compose logs ms-auth

# Aumentar memoria
# En Docker Desktop: Preferences → Resources → Memory: 8GB
```

### BD corrupta

```bash
# Reset completo (CUIDADO - pierde datos)
docker-compose down -v
docker-compose up -d
```

### Bajo rendimiento

```bash
# Ver recursos usados
docker stats

# Aumentar replicas del Gateway
# En docker-compose.yml, agregar scale
docker-compose up -d --scale ms-auth=2
```

---

## 📈 Performance

### Objetivos

- **Latencia**: p95 < 500ms
- **Throughput**: 50 usuarios concurrentes
- **Uptime**: 99.9%

### Testing de carga

```bash
# Con JMeter
jmeter -n -t test-plan.jmx -l results.jtl -j logs.log

# Ver resultados
jmeter -g results.jtl -o html-report/
```

---

## 🔐 Seguridad en Producción

✅ **Hacer:**
- Cambiar credenciales por defecto
- Activar HTTPS/TLS
- Configurar firewall (solo puertos 80, 443)
- Habilitar rate limiting
- Hacer backups diarios
- Monitorear logs

❌ **Evitar:**
- Exponer puertos internos (8080, 8761, etc.)
- Usar contraseñas débiles
- Correr como root
- Deshabilitar HTTPS

---

## 🎯 Checklist Pre-Producción

- [ ] Variables de entorno (.env) configuradas
- [ ] SSL/TLS instalado (HTTPS)
- [ ] Firewall configurado (solo 80, 443)
- [ ] Backups automáticos configurados
- [ ] Monitoreo activo (health checks, logs)
- [ ] Tests de carga pasados
- [ ] OWASP security review completada
- [ ] Documentación de operaciones lista

---

## 📞 Soporte

Para problemas en producción:
- Revisar logs: `docker-compose logs`
- Contactar al tutor: víctor.gómez@udla.edu.ec
- Crear issue en GitHub
