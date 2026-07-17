# Multi-stage Dockerfile para Frontend Vue.js 3
# Stage 1: Build con Node.js (vite build genera /app/dist)
FROM node:20-alpine AS build

WORKDIR /app

# Copiar package files primero (cache de layer si no cambian dependencias)
COPY package.json package-lock.json ./
RUN npm ci

# Copiar codigo fuente y buildear
COPY . .
RUN npm run build

# Stage 2: Nginx sirve el /app/dist compilado
FROM nginx:1.27-alpine

# Copiar nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# Copiar aplicacion compilada desde stage 'build'
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
