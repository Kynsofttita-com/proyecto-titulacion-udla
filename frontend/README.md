# Frontend - Sistema de Gestión Escuelas de Conducción

Vue 3 + Vite + TypeScript + PrimeVue

## Setup

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
```

## Project Structure

```
src/
├── components/       # Componentes reutilizables
├── layouts/         # Layouts principales (MainLayout, AuthLayout)
├── router/          # Vue Router configuración
├── stores/          # Pinia stores
├── views/           # Vistas por dominio
│   ├── auth/       # LoginView, ForgotPasswordView, etc
│   ├── estudiantes/
│   ├── instructores/
│   ├── vehiculos/
│   ├── asignaciones/
│   └── cobros/
├── services/        # Axios HTTP services
├── App.vue
└── main.ts
```

## Scripts

- `npm run dev` - Inicia dev server (puerto 5173)
- `npm run build` - Build production
- `npm run type-check` - Verificar tipos TypeScript
- `npm run lint` - ESLint
- `npm run format` - Prettier format
