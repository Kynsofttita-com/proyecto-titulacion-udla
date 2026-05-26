# Frontend — Sistema de Gestión Escuelas de Conducción

SPA Vue 3 + Vite + TypeScript + PrimeVue + Tailwind CSS.

**Última actualización:** 2026-05-26 — Frontend del Grupo A funcional al 100% (Sprints 7-8). Frontend Grupo B planificado para Sprint 11.

---

## Stack

- **Vue.js 3** (Composition API + `<script setup lang="ts">`)
- **Vite 5** (dev server + build)
- **TypeScript** (strict mode)
- **Pinia** + **pinia-plugin-persistedstate** (state management con persistencia)
- **Vue Router 4** (routing con guards de autenticación)
- **Axios** (HTTP client con interceptors JWT auto-refresh)
- **PrimeVue 3.52** + **PrimeIcons** (UI components)
- **Tailwind CSS 3.4** (utility-first, brand colors customizados)
- **VeeValidate 4** + **Yup** (validación de forms)
- **date-fns** (manejo de fechas)
- **Vitest** + **Vue Test Utils** + **@testing-library/vue** (testing)
- **ESLint** + **Prettier** (lint + format)

---

## Setup

```bash
# Instalar dependencias
npm install

# Copiar variables de entorno (editar VITE_API_URL si es necesario)
cp .env.example .env

# Levantar dev server (puerto 5173)
npm run dev
```

> El frontend espera el backend en `http://localhost:8080` (API Gateway). Para levantar el backend completo ver [`infrastructure/docker/README.md`](../infrastructure/docker/README.md).

### Credenciales de prueba

```
admin@escuela.local / Admin123!
```

---

## Estructura del proyecto

```
frontend/
├── public/                    # Assets estáticos servidos tal cual
├── src/
│   ├── components/            # Componentes reutilizables
│   │   ├── admin/             # Componentes específicos del rol ADMIN
│   │   ├── asignaciones/      # Wizard + calendario tripartita
│   │   └── ui/                # Componentes UI base (StatusBadge, StatCard, etc.)
│   ├── layouts/               # MainLayout, AuthLayout
│   ├── router/                # Vue Router config + guards
│   ├── services/              # Axios HTTP services por dominio
│   ├── stores/                # Pinia stores (useAuthStore, useEstudiantesStore, etc.)
│   ├── styles/                # SCSS globales + variables Tailwind
│   ├── utils/                 # Helpers (validadores Ecuador, formatters)
│   ├── views/                 # Vistas por dominio
│   │   ├── auth/              # Login, ForgotPassword, ResetPassword
│   │   ├── configuracion/     # Config del sistema (ADMIN)
│   │   ├── estudiantes/       # Lista + Form + Detail con tabs
│   │   ├── instructores/      # Lista + Form + Detail + Calendario
│   │   ├── vehiculos/         # Lista + Form + Detail + Alertas SOAT
│   │   ├── asignaciones/      # Calendario + Wizard tripartita
│   │   └── cobros/            # Estado cuenta + Factura + Pago parcial
│   ├── App.vue
│   └── main.ts                # Bootstrap (PrimeVue, Pinia, Router, Axios interceptors)
├── index.html
├── vite.config.ts             # Config Vite + manualChunks
├── tsconfig.json              # TS config (strict, paths)
├── tailwind.config.js         # Brand colors teal + accent coral
├── postcss.config.js
├── package.json
└── .env.example               # Template variables de entorno
```

---

## Scripts

| Comando | Acción |
|---------|--------|
| `npm run dev` | Inicia dev server en puerto 5173 con HMR |
| `npm run build` | Build de producción (`vue-tsc --noEmit && vite build`) |
| `npm run preview` | Sirve el build de producción local |
| `npm run type-check` | Solo verifica tipos TypeScript |
| `npm run lint` | ESLint sobre todos los archivos |
| `npm run format` | Prettier sobre `src/` |

> ⚠ **Nota:** `vue-tsc` (usado en `npm run build`) tiene un bug upstream con TS 5.6+ y Node 22 (ver `DECISIONES.md §25.5`). Si el build local falla por type-check, ejecutá solo `vite build` para validar — `vite build` hace su propio type-check y reporta errores.

---

## Variables de entorno

Copiá `.env.example` a `.env` y ajustá:

| Variable | Default | Notas |
|----------|---------|-------|
| `VITE_API_URL` | `http://localhost:8080` | URL del API Gateway. En producción será `https://api.miescuela.com` |

---

## Convenciones

- **Componentes:** PascalCase para archivos y declaración (`EstudianteForm.vue`), kebab-case en templates (`<estudiante-form />`)
- **Composables:** camelCase con prefijo `use` (`useAuth()`, `useNotificaciones()`)
- **Stores:** `useXxxStore()` (`useAuthStore`, `useEstudiantesStore`)
- **Variables/funciones:** camelCase en TS
- **Tipos/Interfaces:** PascalCase (`Estudiante`, `CreateEstudianteRequest`)
- **Formateo:** Prettier (2 espacios, comilla simple, sin punto y coma)
- **Idioma:** español respetando convenciones de TS/Vue

---

## Estado de implementación al 2026-05-26

### ✅ Funcional (Sprints 7-8 + Sprint 9 pulido)

- Login + Forgot/Reset password + JWT auto-refresh
- Configuración del sistema (ADMIN)
- CRUDs completos: Estudiantes, Instructores, Vehículos, Asignaciones, Cobros
- Calendario de asignaciones con drag & drop (FullCalendar)
- Wizard tripartita con validación en vivo de las 6 reglas cross-MS
- StatCards clickeables + filtros + banners de alertas SOAT
- Form de instructor con sección de contrato (TIEMPO_COMPLETO / MEDIO_TIEMPO / POR_HORAS)
- Configuración de tipos de combustible con precios editables
- Modelo de pago a crédito con cuotas en form de factura

### 🟡 En proceso / Pendiente

- **Sprint 11 (Frontend Grupo B):**
  - `<NotificacionesDropdown />` en header con badge + polling 30s
  - `PlantillasEmailView` + `LogEnviosView`
  - `DashboardView` con KPIs (Chart.js)
  - 4 vistas de reportes operativos + 3 de reportes financieros
- **Sprint 12:** Testing Grupo B (vitest ≥80% en componentes nuevos + Cypress 3 flujos)
- **Sprint 13:** Limpieza + docs final + screenshots para manual usuario

---

## Pipelines CI/CD

| Workflow | Cuándo corre | Acción |
|----------|--------------|--------|
| `frontend-ci.yml` | PR/push a main que toca `frontend/**` | `npm ci` + `vite build` + upload `dist/` artifact |

Ver detalle en [`.github/workflows/frontend-ci.yml`](../.github/workflows/frontend-ci.yml).

---

## Documentación adicional

- [README.md raíz](../README.md) — Visión general del proyecto
- [DECISIONES.md](../DECISIONES.md) — Decisiones técnicas (§2 stack, §6 seguridad JWT, §7 convenciones)
- [PLAN_FASES.md](../PLAN_FASES.md) — Plan vigente de 13 sprints
- [docs/database/schema.md](../docs/database/schema.md) — Modelo BD (los DTOs del frontend deben matchear)
- [backend/README.md](../backend/README.md) — Backend setup (necesario para que el frontend funcione)
