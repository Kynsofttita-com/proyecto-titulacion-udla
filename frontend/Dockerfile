# Dockerfile para Frontend - Demostración Argo CD
# Versión minimalista para demo (Nginx + HTML estático)

FROM nginx:1.27-alpine

# Copiar archivo nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# Crear página HTML simple para la demo
RUN mkdir -p /usr/share/nginx/html && \
    cat > /usr/share/nginx/html/index.html << 'EOF'
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Escuela de Conducción - Frontend</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }
        .container { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); max-width: 600px; text-align: center; }
        h1 { color: #333; margin-bottom: 20px; }
        .status { background: #e8f5e9; border: 2px solid #4caf50; color: #2e7d32; padding: 15px; border-radius: 5px; margin: 20px 0; }
        .tech { background: #f5f5f5; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: left; }
        .tech h3 { color: #667eea; margin-bottom: 10px; }
        .tech ul { list-style: none; padding-left: 0; }
        .tech li { padding: 8px 0; color: #666; }
        .tech li:before { content: "✓ "; color: #4caf50; font-weight: bold; margin-right: 10px; }
        footer { margin-top: 30px; color: #999; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🎓 Sistema de Escuela de Conducción</h1>
        <div class="status">
            <strong>✓ Frontend Funcionando</strong><br>
            <small>Sincronizado por Argo CD desde Kubernetes</small>
        </div>
        <div class="tech">
            <h3>Stack Tecnológico</h3>
            <ul>
                <li><strong>Frontend:</strong> Vue.js 3 + Nginx</li>
                <li><strong>Backend:</strong> Spring Boot 3 (Java 21)</li>
                <li><strong>Orquestación:</strong> Kubernetes + Minikube</li>
                <li><strong>CI/CD:</strong> Argo CD (GitOps)</li>
                <li><strong>Base de datos:</strong> PostgreSQL</li>
                <li><strong>Descubrimiento:</strong> Eureka Server</li>
            </ul>
        </div>
        <footer>
            <p>Grupo: Software Processes - UDLA | Proyecto: Procesos de Software</p>
            <p>Demo: Argo CD sincronizando cambios desde Git</p>
        </footer>
    </div>
</body>
</html>
EOF

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
