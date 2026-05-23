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
    path: '/cobros/factura/nuevo',
    name: 'NuevaFactura',
    component: () => import('@/views/cobros/FacturaFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cobros/factura/:id',
    name: 'EditarFactura',
    component: () => import('@/views/cobros/FacturaFormView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cobros/pago/nueva',
    name: 'NuevoPago',
    component: () => import('@/views/cobros/PagoFormView.vue'),
    meta: { requiresAuth: true }
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
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth

  if (requiresAuth && !authStore.isAuthenticated) {
    // Sin redirect query — siempre que vuelva al sistema irá al dashboard
    next({ name: 'Login' })
  } else if (to.name === 'Login' && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else if (to.meta.roles && !authStore.hasRole(to.meta.roles as string[])) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
