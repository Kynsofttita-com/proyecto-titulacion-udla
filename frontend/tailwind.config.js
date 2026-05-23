/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif']
      },
      colors: {
        // Brand = Teal profesional (Stripe / Atlassian)
        brand: {
          50:  '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',  // primary
          800: '#115e59',
          900: '#134e4a',  // primary dark
          950: '#042f2e'
        },
        // Accent = Coral cálido para CTAs secundarios y highlights
        accent: {
          50:  '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12'
        },
        // Neutros (mantenidos)
        ink: {
          50:  '#f8fafc',
          100: '#f1f5f9',
          200: '#e2e8f0',
          300: '#cbd5e1',
          400: '#94a3b8',
          500: '#64748b',
          600: '#475569',
          700: '#334155',
          800: '#1e293b',
          900: '#0f172a'
        },
        // Verde lima en vez de emerald (combina mejor con teal)
        success: {
          50:  '#f7fee7',
          500: '#84cc16',
          600: '#65a30d',
          700: '#4d7c0f'
        },
        // Ámbar miel (más cálido que el orange)
        warning: {
          50:  '#fefce8',
          500: '#eab308',
          600: '#ca8a04',
          700: '#a16207'
        },
        // Rojo terracota
        danger: {
          50:  '#fef2f2',
          500: '#dc2626',
          600: '#b91c1c',
          700: '#991b1b'
        },
        // Info: cyan que armoniza con teal
        info: {
          50:  '#ecfeff',
          500: '#06b6d4',
          600: '#0891b2',
          700: '#0e7490'
        }
      },
      boxShadow: {
        card: '0 1px 3px 0 rgb(0 0 0 / 0.05), 0 1px 2px -1px rgb(0 0 0 / 0.03)',
        'card-hover': '0 4px 12px -2px rgb(0 0 0 / 0.08), 0 2px 4px -2px rgb(0 0 0 / 0.04)',
        elev: '0 10px 25px -5px rgb(0 0 0 / 0.08), 0 8px 10px -6px rgb(0 0 0 / 0.04)',
        'brand-glow': '0 0 0 3px rgba(15, 118, 110, 0.15)'
      },
      borderRadius: {
        '4xl': '2rem'
      }
    }
  },
  plugins: []
}
