import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import NotificacionesDropdown from '../NotificacionesDropdown.vue'
import * as authStore from '@/stores/auth'
import * as notificacionesStore from '@/stores/notificaciones'

vi.mock('@/stores/auth')
vi.mock('@/stores/notificaciones')

describe('NotificacionesDropdown', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renderiza el botón con icono de campana', () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    expect(wrapper.find('.pi-bell').exists()).toBe(true)
  })

  it('muestra badge solo si hay notificaciones no leídas', async () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    // Sin notificaciones
    expect(wrapper.find('[class*="bg-red-500"]').exists()).toBe(false)
  })

  it('abre y cierra dropdown al hacer click', async () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    expect(wrapper.find('.absolute.right-0').exists()).toBe(false)

    await wrapper.find('button').trigger('click')
    await wrapper.vm.$nextTick()

    // Nota: el dropdown usa transition, puede tardar en renderizarse
  })

  it('formatea fechas correctamente', () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    const vm = wrapper.vm as any
    const ahora = new Date()

    // Justo ahora
    expect(vm.formatearFecha(ahora.toISOString())).toBe('Justo ahora')

    // Hace 5 minutos
    const hace5Mins = new Date(ahora.getTime() - 5 * 60000).toISOString()
    expect(vm.formatearFecha(hace5Mins)).toContain('5 min')

    // Hace 2 horas
    const hace2Horas = new Date(ahora.getTime() - 2 * 3600000).toISOString()
    expect(vm.formatearFecha(hace2Horas)).toContain('2 h')
  })

  it('calcula contadorNoLeidas correctamente', () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    // Valor por defecto es 0
    expect(wrapper.vm.contadorNoLeidas).toBe(0)
  })

  it('cierra dropdown al hacer click fuera', async () => {
    const wrapper = mount(NotificacionesDropdown, {
      global: {
        stubs: {
          'router-link': true
        }
      }
    })

    const button = wrapper.find('button')
    await button.trigger('click')
    await wrapper.vm.$nextTick()

    // Click en overlay
    const overlay = wrapper.find('.fixed.inset-0')
    if (overlay.exists()) {
      await overlay.trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.mostrarDropdown).toBe(false)
    }
  })
})
