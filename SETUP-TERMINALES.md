# 🖥️ SETUP DE TERMINALES - Guía Visual

## PASO 0 - Abre 5 PowerShells / Terminales

En Windows, abre 5 ventanas de PowerShell así:

```
Presiona: Win + X → Selecciona "Windows PowerShell" 
(Repite 5 veces)
```

O abre PowerShell directamente 5 veces.

---

## DISPOSICIÓN DE PANTALLA IDEAL

```
Tu pantalla va a quedar así:

SUPERIOR 70%:
┌────────────────────────────────────────────────────────┐
│                                                        │
│              NAVEGADOR GOOGLE CHROME                   │
│         (2 pestañas: Minikube + Argo CD)              │
│                                                        │
└────────────────────────────────────────────────────────┘

INFERIOR 30%:
┌──────────────────────┬──────────────────────────────────┐
│   TERMINAL 4         │  TERMINAL 5                      │
│   (kubectl -w)       │  (git commands)                  │
│                      │                                  │
└──────────────────────┴──────────────────────────────────┘

BACKGROUND (corriendo pero no visible):
- TERMINAL 1: minikube dashboard
- TERMINAL 2: kubectl port-forward
- TERMINAL 3: Disponible
```

---

## CÓMO ORGANIZAR EN WINDOWS

### Opción 1: PowerShell Nativa (Fácil)

1. Abre PowerShell 1 (NO HAGAS NADA AÚN)
2. Abre PowerShell 2 (NO HAGAS NADA AÚN)
3. Abre PowerShell 3 (NO HAGAS NADA AÚN)
4. Abre PowerShell 4 (NO HAGAS NADA AÚN)
5. Abre PowerShell 5 (NO HAGAS NADA AÚN)
6. Abre Google Chrome

Ahora organiza visualmente:
- Chrome maximizado en la mitad superior
- Arrastra Terminal 4 y 5 en la mitad inferior
- Terminal 1, 2, 3 minimizadas (pero corriendo en background)

### Opción 2: Windows Terminal (Mejor)

Si tienes Windows Terminal instalado:

1. Abre Windows Terminal
2. Presiona Ctrl + Shift + N → Nueva pestaña (haz esto 5 veces)
3. En cada pestaña ejecuta un comando diferente

---

## COMANDOS EXACTOS POR TERMINAL

### TERMINAL 1 (Minimiza después de ejecutar)

```bash
cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto\ titulacion
minikube dashboard
```

✅ Se abrirá automáticamente en Chrome  
✅ Déjalo corriendo  
✅ Minimiza esta ventana

---

### TERMINAL 2 (Minimiza después de ejecutar)

```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```

✅ Debes ver: `Forwarding from 127.0.0.1:8443 -> 443`  
✅ Déjalo corriendo  
✅ Minimiza esta ventana

---

### TERMINAL 3 (Disponible para uso futuro)

```bash
cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto\ titulacion
```

✅ Solo cd al proyecto, no ejecutes nada más  
✅ Déjala lista

---

### TERMINAL 4 (VISIBLE - parte inferior izquierda)

**Durante ACTO 5, ejecuta ESTO:**

```bash
kubectl get deployments -n escuela-conduccion -w
```

✅ Se verá la lista de deployments  
✅ Mirará aquí el profesor cómo los pods suben de 1 a 3

---

### TERMINAL 5 (VISIBLE - parte inferior derecha)

**Durante ACTO 5, ejecuta ESTO:**

Primero ve a la carpeta:

```bash
cd C:\Users\hmate\OneDrive\Desktop\UDLA\Proyecto\ titulacion
```

Luego edita el archivo (elige uno):

```bash
notepad kubernetes/argocd/apps/01-api-gateway.yaml
```

O:

```bash
code kubernetes/argocd/apps/01-api-gateway.yaml
```

Después de editar (cambio replicas 1 → 3), guarda y ejecuta:

```bash
git add kubernetes/argocd/apps/01-api-gateway.yaml
git commit -m "Sprint 12 (Demo - Aumentar replicas a 3)"
git push origin main
```

---

## NAVEGADOR GOOGLE CHROME

### Pestaña 1 - Minikube Dashboard

Se abre automáticamente cuando ejecutas `minikube dashboard` en Terminal 1.

**URL:** `http://127.0.0.1:XXXXX` (el puerto cambia)

**QUÉ VER:**
- Namespace selector arriba: selecciona `escuela-conduccion`
- Workloads → Deployments (verás 6)
- Workloads → Pods (verás 6)
- Network → Services (verás 6)

---

### Pestaña 2 - Argo CD UI

**URL:** `https://localhost:8443`

**Login:**
- Usuario: `admin`
- Contraseña: `rJKEstJ0j0l3WtA7`

**QUÉ VER:**
- En la izquierda, haz clic en "escuela-conduccion"
- Debes ver el estado: "Synced" (verde) o "OutOfSync" (amarillo)
- El árbol de recursos que hay desplegado

---

## TIMELINE PARA PRESENTACIÓN

```
00:00 - 00:05  Setup inicial
        - Terminal 1 y 2 corriendo
        - Chrome abierto con 2 pestañas
        - Listo

00:05 - 00:07  ACTO 1 - Verificación
        - Terminal 3: minikube status + kubectl get pods

00:07 - 00:10  ACTO 2 - Minikube Dashboard
        - Chrome Pestaña 1: mostrar deployments, pods, services

00:10 - 00:13  ACTO 3 - Argo CD UI
        - Chrome Pestaña 2: mostrar estado, tree, source

00:13 - 00:15  ACTO 4 - Aplicación corriendo
        - Chrome nueva pestaña: http://localhost:3000

00:15 - 00:23  ACTO 5 - DEMO GITOPS (IMPORTANTE)
        - Terminal 4: kubernetes get deployments -w
        - Terminal 5: editar, commit, push
        - Chrome Pestaña 2: ver cambio a OutOfSync → Synced
        - Terminal 4: ver replicas 1 → 3

00:23 - 00:25  Resumen y preguntas
```

---

## CHECKLIST VISUAL

Antes de empezar, verifica:

```
☐ Chrome abierto
  ☐ Pestaña 1: Minikube Dashboard (localhost:XXXXX)
  ☐ Pestaña 2: Argo CD UI (https://localhost:8443)

☐ Terminales 1 y 2 corriendo (minimizadas):
  ☐ Terminal 1: minikube dashboard
  ☐ Terminal 2: kubectl port-forward (dice "Forwarding...")

☐ Terminales 4 y 5 VISIBLES y LISTAS:
  ☐ Terminal 4: En carpeta del proyecto
  ☐ Terminal 5: En carpeta del proyecto

☐ Git: 
  ☐ Sin cambios sin guardar (git status limpio)
  ☐ Acceso a internet (para git push)

☐ Tienes abierto:
  ☐ kubernetes/DEMO-CLASE.md (para seguir paso a paso)
  ☐ kubernetes/argocd/apps/01-api-gateway.yaml (sabes dónde está)
```

---

## 💡 TIPS

1. **Practica ANTES**: Haz el setup 1-2 veces antes de la clase
2. **No minimices Terminal 1 y 2**: Déjalas abiertas pero en background
3. **Fuentes grandes**: Aumenta zoom en terminales (Ctrl + plus) para que se vea bien
4. **Chrome zoom**: También ajusta Chrome a 125% para que todo se vea bien
5. **Mantén orden**: Terminal 4 a la izquierda, Terminal 5 a la derecha

---

## 🎯 RESUMEN VISUAL FINAL

```
┌─────────────────────────────────────────────────────────┐
│                  GOOGLE CHROME                          │
│  ┌──────────────────┬──────────────────────────────┐   │
│  │ TAB 1: Minikube  │ TAB 2: Argo CD               │   │
│  └──────────────────┴──────────────────────────────┘   │
│  (Aquí muestra deployments, pods, estado sinc.)       │
└─────────────────────────────────────────────────────────┘

┌──────────────────────┬──────────────────────────────┐
│ TERMINAL 4           │ TERMINAL 5                   │
│ kubectl -w           │ git commands                 │
│ (ves pods cambiar)   │ (haces push)                 │
└──────────────────────┴──────────────────────────────┘
```

---

## 🚀 LISTO PARA DEMO

Sigue esta disposición y tu presentación será profesional y clara.

**Tiempo total setup: 5 minutos**

¡Adelante! 🎬
