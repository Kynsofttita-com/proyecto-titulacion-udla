# ✅ CHECKLIST DE PRESENTACIÓN — Día de la Demo

**Evento:** Presentación técnica de Argo CD + Kubernetes  
**Fecha:** [Tu fecha]  
**Hora:** [Tu hora]  
**Duración:** 20-25 minutos  
**Público:** Profesor + compañeros

---

## 📋 El día anterior (24 horas antes)

### Preparación técnica
- [ ] Minikube instalado y funcionando
- [ ] Argo CD instalado (`kubectl get pods -n argocd | grep server`)
- [ ] Imágenes Docker construidas (`docker images | grep -E "(api-gateway|ms-auth|frontend)"`)
- [ ] Application sincronizada en Argo CD (`kubectl get application escuela-conduccion -n argocd`)
- [ ] Frontend accesible (`http://localhost:3000` después de port-forward)
- [ ] API Gateway respondiendo (`http://localhost:8080/actuator/health`)

### Prueba completa de demo
- [ ] Ejecutar **ACTO 1** (verificación) - debe pasar en 2 min
- [ ] Ejecutar **ACTO 2** (arquitectura visual) - debe pasar en 3 min
- [ ] Ejecutar **ACTO 3** (Argo CD UI) - debe pasar en 3 min
- [ ] Ejecutar **ACTO 4** (aplicación funcionando) - debe pasar en 3 min
- [ ] Ejecutar **ACTO 5** (GitOps demo) - **CRÍTICO**, practicar 2 veces - 8 min
- [ ] Ejecutar **ACTO 6** (conclusiones) - debe pasar en 2 min

### Documentación
- [ ] Imprimir guion: `kubernetes/DEMO-CLASE.md` (2 copias: una para ti, una para el profesor)
- [ ] Guardar screenshots de respaldo en carpeta `evidencia/` (por si falla algo en vivo)
- [ ] Documento de evidencia completado: `kubernetes/EVIDENCIA-PRESENTACION.md`

### Hardware y software
- [ ] Laptop con suficiente batería (o estar enchufado)
- [ ] PowerShell abierto, terminal limpia
- [ ] 3 terminales preparadas
- [ ] Navegador con 2 pestañas
- [ ] Resolución de pantalla: 1920x1080 o mayor (para que todo se vea)
- [ ] Fuente de terminal: mínimo 16pt (visible para todos)
- [ ] Micrófono funcionando

---

## 📱 El día de la presentación (30 minutos antes)

### En el aula (30 min antes de empezar)

**Terminal 1 - Minikube Dashboard:**
```bash
minikube dashboard
```
✅ Déjalo corriendo en background

**Terminal 2 - Argo CD port-forward:**
```bash
kubectl port-forward svc/argocd-server -n argocd 8443:443
```
✅ Debe mostrar: `Forwarding from 127.0.0.1:8443 -> 443`

**Terminal 3 - Lista para comandos:**
```bash
# Solo abierta, esperando tus comandos
```
✅ Posicionada para que todos vean

**Navegador:**
- Pestaña 1: `http://localhost:8080` (Minikube Dashboard)
- Pestaña 2: `https://localhost:8443` (Argo CD)
✅ Ambas cargadas y listas

---

## 🎬 DURANTE LA PRESENTACIÓN

### Introducción (1 minuto)
```
"Hola, soy [Tu nombre].
Hoy les voy a demostrar Argo CD + Kubernetes + GitOps.
Esto es lo que usan empresas como Google y Netflix.
Voy a mostrar cómo cambiar código en Git y que automáticamente
se actualice en un cluster de Kubernetes."
```

- [ ] Saludar y presentarse
- [ ] Explicar qué van a ver (30 segundos)
- [ ] Aclarar que es demo en vivo (puede tomar tiempo)

### ACTO 1 - Verificación (2 min)
```bash
minikube status
kubectl get pods -n escuela-conduccion
```
- [ ] Mostrar output
- [ ] Decir: "Minikube corriendo, aplicación sincronizada"

### ACTO 2 - Arquitectura (3 min)
- [ ] Ir a Minikube Dashboard (pestaña 1)
- [ ] Mostrar namespace `escuela-conduccion`
- [ ] Mostrar Deployments (6 servicios)
- [ ] Mostrar Pods (6 pods corriendo)
- [ ] Mostrar Services
- [ ] Decir explicación (ver DEMO-CLASE.md)

### ACTO 3 - Argo CD UI (3 min)
- [ ] Ir a Argo CD UI (pestaña 2)
- [ ] Login si no está hecho
- [ ] Mostrar Application "escuela-conduccion"
- [ ] Estado: Synced ✅
- [ ] Mostrar árbol de recursos
- [ ] Mostrar source (Git repository)
- [ ] Decir explicación (ver DEMO-CLASE.md)

### ACTO 4 - Aplicación (3 min)
- [ ] Abrir nueva pestaña del navegador
- [ ] Ir a `http://localhost:3000` (frontend)
- [ ] Mostrar que funciona
- [ ] Ir a `http://localhost:8080/actuator/health` (API Gateway)
- [ ] Mostrar respuesta JSON
- [ ] Decir explicación (ver DEMO-CLASE.md)

### ACTO 5 - GitOps Demo ⭐ (8 min) - EL MOMENTO IMPORTANTE

**Pre-requisito:** Abre Terminal 4 con:
```bash
kubectl get deployments -n escuela-conduccion -w
```

**Paso 1: Editar Git**
- [ ] Abrir `kubernetes/argocd/apps/01-api-gateway.yaml`
- [ ] Cambiar `replicas: 1` a `replicas: 3`
- [ ] Guardar

**Paso 2: Commit**
```bash
git add kubernetes/argocd/apps/01-api-gateway.yaml
git commit -m "Sprint 12 (Demo GitOps - Aumentar réplicas a 3)"
git push origin main
```
- [ ] Ejecutar y mostrar output

**Paso 3: Esperar detección (30 seg)**
- [ ] Ir a Argo CD UI
- [ ] Mostrar estado cambiando de "Synced" a "OutOfSync"
- [ ] IMPORTANTE: decir mientras espera

**Paso 4: Sincronización (2 min)**
- [ ] Click en "Sync"
- [ ] Observar en Terminal 4 como suben las replicas (1/3 → 2/3 → 3/3)
- [ ] NARRAR en vivo: "Miren cómo Kubernetes está creando nuevos pods"

**Paso 5: Confirmación (1 min)**
- [ ] Esperar a que termine (3/3 Ready)
- [ ] Ir a Argo CD: debe estar "Synced" de nuevo
- [ ] DECIR: "Perfecto, Git y Kubernetes son idénticos de nuevo"

### ACTO 6 - Conclusiones (2 min)

Resumen de 30 segundos:
- ✅ Kubernetes orquesta contenedores
- ✅ Argo CD implementa GitOps
- ✅ Git es la fuente de verdad
- ✅ Cambios se sincronizan automáticamente

Luego: "Preguntas?"

---

## 🆘 SI ALGO FALLA EN VIVO

### Plan B - Estrategia de contingencia

| Falla | Qué hacer |
|------|-----------|
| **Argo CD no abre** | Revisa Terminal 2, espera 10 seg más, muestra screenshot de respaldo |
| **Frontend no carga** | No es crítico, salta al ACTO 5 (lo importante) |
| **Git no sincroniza** | Espera 60 segundos, muestra screenshot de respaldo |
| **Pods no suben** | Muestra `kubectl get pods` mientras se crean |
| **La máquina se crashea** | Muestra las screenshots de respaldo y explica conceptualmente |

### Capturas de respaldo (GUARDAR HOY)
- [ ] Screenshot: Minikube Dashboard con 6 pods
- [ ] Screenshot: Argo CD con Application "Synced"
- [ ] Screenshot: Frontend funcionando
- [ ] Screenshot: git diff del cambio
- [ ] Screenshot: OutOfSync en Argo CD
- [ ] Screenshot: Pods siendo creados (watch)
- [ ] Screenshot: Synced final
- [ ] Screenshot: 3 replicas en lugar de 1

---

## 📊 Rúbrica de evaluación (lo que el profesor verá)

### ✅ Conceptos entendidos
- [ ] Explicaste qué es Kubernetes
- [ ] Explicaste qué es Argo CD
- [ ] Explicaste qué es GitOps
- [ ] Mostraste cómo funciona en vivo

### ✅ Demo técnica
- [ ] La aplicación está corriendo
- [ ] Argo CD está sincronizado
- [ ] Hiciste cambio en Git
- [ ] Se sincronizó automáticamente
- [ ] Explicaste el flujo

### ✅ Documentación
- [ ] Manifiestos YAML organizados
- [ ] Documento de evidencia completado
- [ ] Guion de demo claro
- [ ] Código comentado si es necesario

### ✅ Presentación
- [ ] Explicación clara
- [ ] Tiempo respetado (20-25 min)
- [ ] Respuestas a preguntas
- [ ] Profesionalismo

---

## ⏰ Timing exacto (cronómetro)

```
00:00-01:00  Introducción
01:00-03:00  ACTO 1 (Verificación)
03:00-06:00  ACTO 2 (Arquitectura)
06:00-09:00  ACTO 3 (Argo CD UI)
09:00-12:00  ACTO 4 (Aplicación)
12:00-20:00  ACTO 5 (GitOps demo) ⭐
20:00-22:00  ACTO 6 (Conclusiones)
22:00-25:00  Preguntas y respuestas
────────────────────────────
25:00 = FIN
```

**Nota:** Si algo toma más tiempo, salta el ACTO 6 (conclusiones) que es corto.

---

## 💬 Frases que debes practicar

### Entrada
> "Hola, les voy a mostrar cómo funciona Argo CD,
> una herramienta que implementa GitOps para Kubernetes."

### Explicación de GitOps
> "Git es la fuente de verdad. Cualquier cambio que hago aquí,
> Argo CD lo detects automáticamente y lo sincroniza en el cluster.
> Kubernetes ahora tiene exactamente lo que Git dice."

### Cuando algo sale bien
> "¡Lo ves? Sin que yo ejecute ningún comando kubectl manual,
> Kubernetes actualizó automáticamente la aplicación."

### Si algo falla
> "A veces en la tecnología pasan cosas inesperadas,
> pero mira estas capturas que tomé en el ensayo,
> muestra exactamente esto funcionando."

---

## 📋 Materiales a llevar/tener

- [ ] Laptop cargada
- [ ] Cable HDMI/USB-C para proyector
- [ ] Guion impreso (2 copias)
- [ ] Documento de evidencia impreso
- [ ] Pen drive con capturas de respaldo
- [ ] Notas personales (cheat sheet)

---

## 🎓 Qué practicar HICHOEM

Antes de la presentación, practica estos scripts 2 veces:

```bash
# Script 1: Verificación (ACTO 1)
minikube status && kubectl get pods -n escuela-conduccion

# Script 2: Demo GitOps (ACTO 5)
# 1. Editar kubernetes/argocd/apps/01-api-gateway.yaml
# 2. Cambiar replicas: 1 → replicas: 3
# 3. git add/commit/push
# 4. Observar en Argo CD
# 5. Click Sync
# 6. Ver pods aparecer en watch
```

---

## ✨ Tips finales

1. **Practica en voz alta** - Escúchate explicando
2. **Timing** - Usa un cronómetro, practica de verdad
3. **Respirar** - Habla lentamente, deja tiempo para que procesen
4. **Contacto visual** - Mira al profesor y compañeros, no solo a la pantalla
5. **Confianza** - Tú entiendes esto mejor que nadie, muéstalo
6. **Preguntas** - Si alguien pregunta algo que no sabes, di: "Buena pregunta, déjame verificar"

---

## 🎬 Resumen de "qué llevar"

**En la laptop:**
- [ ] Minikube con aplicación corriendo
- [ ] Argo CD accesible
- [ ] Terminal limpia y lista
- [ ] Navegador con 2 pestañas
- [ ] Git actualizado

**En papel/digital:**
- [ ] Guion DEMO-CLASE.md
- [ ] Evidencia completada
- [ ] Screenshots de respaldo
- [ ] Cheat sheet personal

**En la cabeza:**
- [ ] Qué es Kubernetes
- [ ] Qué es Argo CD
- [ ] Qué es GitOps
- [ ] Por qué es importante

---

## 🏁 PRESENTACIÓN LISTA

```
✅ Setup técnico verificado
✅ Demo ensayada 2 veces
✅ Documentación completada
✅ Capturas de respaldo guardadas
✅ Guion memorizado
✅ Timing practicado
✅ Respuestas a preguntas preparadas
✅ Profesionalismo garantizado

🚀 LISTO PARA BRILLAR EN CLASE
```

---

**Grupo:** Software Processes - UDLA  
**Fecha:** [Tu fecha de presentación]  
**Estado:** ✅ LISTO PARA PRESENTAR

**¡Éxito! 🎉**
