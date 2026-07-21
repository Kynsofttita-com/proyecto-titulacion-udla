import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { layout: 'auth', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPasswordView.vue'),
    meta: { layout: 'auth', requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/auth/ResetPasswordView.vue'),
    meta: { layout: 'auth', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard',
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/estudiantes',
    name: 'Estudiantes',
    component: () => import('@/views/estudiantes/ListaEstudiantesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/estudiantes/nuevo',
    name: 'NuevoEstudiante',
    component: () => import('@/views/estudiantes/EstudianteFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/estudiantes/:id',
    name: 'EstudianteDetalle',
    component: () => import('@/views/estudiantes/EstudianteDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/estudiantes/:id/editar',
    name: 'EditarEstudiante',
    component: () => import('@/views/estudiantes/EstudianteFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/instructores',
    name: 'Instructores',
    component: () => import('@/views/instructores/ListaInstructoresView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/instructores/nuevo',
    name: 'NuevoInstructor',
    component: () => import('@/views/instructores/InstructorFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/instructores/:id',
    name: 'InstructorDetalle',
    component: () => import('@/views/instructores/InstructorDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/instructores/:id/editar',
    name: 'EditarInstructor',
    component: () => import('@/views/instructores/InstructorFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vehiculos',
    name: 'Vehiculos',
    component: () => import('@/views/vehiculos/ListaVehiculosView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vehiculos/nuevo',
    name: 'NuevoVehiculo',
    component: () => import('@/views/vehiculos/VehiculoFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vehiculos/:id',
    name: 'VehiculoDetalle',
    component: () => import('@/views/vehiculos/VehiculoDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/vehiculos/:id/editar',
    name: 'EditarVehiculo',
    component: () => import('@/views/vehiculos/VehiculoFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/configuracion/combustible',
    name: 'PreciosCombustible',
    component: () => import('@/views/configuracion/PreciosCombustibleView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/asignaciones',
    name: 'Asignaciones',
    component: () => import('@/views/asignaciones/CalendarioAsignacionesView.vue'),
    meta: { requiresAuth: true }
  },
  // {
  //   path: '/asignaciones-lista',
  //   name: 'AsignacionesLista',
  //   component: () => import('@/views/asignaciones/ListaAsignacionesView.vue'),
  //   meta: { requiresAuth: true }
  // },
  // {
  //   path: '/asignaciones/:id',
  //   name: 'AsignacionDetalle',
  //   component: () => import('@/views/asignaciones/AsignacionDetailView.vue'),
  //   meta: { requiresAuth: true }
  // },
  {
    path: '/cobros',
    name: 'Cobros',
    component: () => import('@/views/cobros/EstadoCuentaView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/finanzas/gastos',
    name: 'Gastos',
    component: () => import('@/views/finanzas/GastosView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/finanzas/saldo',
    name: 'Saldo',
    component: () => import('@/views/finanzas/SaldoView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/configuracion',
    name: 'Configuracion',
    component: () => import('@/views/configuracion/ConfiguracionView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/usuarios',
    name: 'Usuarios',
    component: () => import('@/views/configuracion/UsuariosView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/usuarios/:id',
    name: 'UsuarioDetalle',
    component: () => import('@/views/configuracion/UsuarioDetailView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/perfil',
    name: 'Perfil',
    component: () => import('@/views/auth/PerfilView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mi-progreso',
    name: 'MiProgreso',
    component: () => import('@/views/estudiantes/MiProgresoView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/mis-pagos',
    name: 'MisPagos',
    component: () => import('@/views/estudiantes/MisPagosView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/plantillas',
    name: 'Plantillas',
    component: () => import('@/views/notificaciones/PlantillasEmailView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/notificaciones',
    name: 'Notificaciones',
    component: () => import('@/views/notificaciones/NotificacionesView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reportes/operativos/estudiantes',
    name: 'ReporteEstudiantes',
    component: () => import('@/views/reportes/operativos/ReporteEstudiantesView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/operativos/instructores',
    name: 'ReporteInstructores',
    component: () => import('@/views/reportes/operativos/ReporteInstructoresView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/operativos/vehiculos',
    name: 'ReporteVehiculos',
    component: () => import('@/views/reportes/operativos/ReporteVehiculosView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/operativos/asistencia',
    name: 'ReporteAsistencia',
    component: () => import('@/views/reportes/operativos/ReporteAsistenciaView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/financieros/ingresos',
    name: 'ReporteIngresos',
    component: () => import('@/views/reportes/financieros/ReporteIngresosView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/financieros/morosidad',
    name: 'ReporteMorosidad',
    component: () => import('@/views/reportes/financieros/ReporteMorosidadView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/reportes/financieros/recibos',
    name: 'ReporteRecibos',
    component: () => import('@/views/reportes/financieros/ReporteRecibosView.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN', 'STAFF'] }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth
  const rolesPermitidos = to.meta.roles as string[] | undefined

  if (requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else if (
    authStore.isAuthenticated
    && authStore.mustChangePassword
    && to.name !== 'Perfil'
  ) {
    // Si el admin activo "forzar cambio de contraseña" para este usuario,
    // bloqueamos toda navegacion hasta que lo haga en /perfil.
    next({ name: 'Perfil' })
  } else if (rolesPermitidos && !rolesPermitidos.includes(authStore.currentRole)) {
    // Guard por ROL ACTIVO (no por roles del user). Si el user tiene ADMIN+STAFF
    // y eligio actuar como STAFF, NO debe poder entrar a rutas ADMIN-only.
    // Esto permite la previsualizacion realista del rol.
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
