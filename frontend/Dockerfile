# Dockerfile para Frontend Vue.js 3 - Nginx Solo
FROM nginx:1.27-alpine

# Copiar nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar aplicación compilada (dist ya generado por npm run build)
COPY dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
