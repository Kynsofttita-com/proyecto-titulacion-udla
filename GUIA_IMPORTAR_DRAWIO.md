# 🎨 GUÍA COMPLETA - IMPORTAR DIAGRAMAS A DRAW.IO

**Fecha**: 2026-07-12  
**Status**: ✅ Listo para draw.io

---

## 📁 ARCHIVOS DISPONIBLES PARA DRAW.IO

### 1. **arquitectura-sistema.drawio** ⭐ RECOMENDADO
- Archivo nativo de draw.io
- 2 diagramas principales incluidos:
  - Diagrama 1: Contexto Sistema Completo (8 MS + Infraestructura)
  - Diagrama 2: Procesos Principales (5 flujos)
- **Uso**: Abrir directamente en draw.io

### 2. **diagrama-arquitectura-proyecto.md**
- 10 diagramas en formato Mermaid
- Compatible con draw.io (mediante importación)
- **Uso**: Copiar/pegar en draw.io desde GitHub

### 3. **diagrama-arquitectura-interactivo.html**
- 7 diagramas en página web interactiva
- Renderiza con Mermaid.js
- **Uso**: Abrir en navegador, capturar pantalla o copiar

---

## 🚀 OPCIÓN 1: ABRIR ARCHIVO .DRAWIO DIRECTAMENTE (MEJOR)

### Paso 1: Descargar el archivo
```bash
Archivo: arquitectura-sistema.drawio
```

### Paso 2: Ir a draw.io
1. Abre en navegador: **https://app.diagrams.net/**
2. O descarga draw.io desktop: https://github.com/jgraph/drawio-desktop/releases

### Paso 3: Abrir el archivo
**En web (draw.io online)**:
1. Click en "File" → "Open"
2. Selecciona el archivo `arquitectura-sistema.drawio`
3. ¡Listo! Los diagramas aparecerán

**En desktop (draw.io app)**:
1. Click en "File" → "Open"
2. Navega al archivo
3. Click en "Open"

### Paso 4: Editar y guardar
- Puedes editar cualquier elemento
- Agregar nuevos componentes
- Cambiar colores, estilos
- **File → Save** para guardar cambios

---

## 🎯 OPCIÓN 2: IMPORTAR DESDE MERMAID (CÓDIGO)

Si quieres copiar los diagramas Mermaid a draw.io:

### Paso 1: Copiar código Mermaid
Desde `diagrama-arquitectura-proyecto.md`, copia un diagrama, ej:

```
graph TB
    subgraph Cliente["🖥️ CLIENTE"]
        UI["Vue.js 3 SPA"]
    end
    
    subgraph API["🌐 API GATEWAY"]
        GW["Spring Cloud Gateway:8080"]
    end
    
    subgraph Servicios["☁️ MICROSERVICIOS"]
        AUTH["🔐 MS-Auth:8081"]
        EST["📚 MS-Estudiantes:8082"]
        ...
    end
```

### Paso 2: Ir a draw.io
1. Abre https://app.diagrams.net/
2. Crea un diagrama nuevo

### Paso 3: Insertar desde Mermaid
1. Click en "File" → "Import From"
2. Busca opción "Mermaid" o "Paste Mermaid code"
3. Pega el código Mermaid
4. Click "Import"

**Si no hay opción Mermaid**:
1. Usa https://mermaid.live/ para convertir Mermaid a SVG
2. Guarda como SVG
3. En draw.io: "File" → "Import" → selecciona SVG

---

## 📊 OPCIÓN 3: CONVERTIR MERMAID A SVG

### Usando mermaid.live (Online, sin instalar)

1. Abre: https://mermaid.live/
2. Pega código Mermaid en editor izquierdo
3. Click botón "Download" → "SVG"
4. Se descarga el archivo SVG

### Luego importar en draw.io

1. Abre draw.io
2. "File" → "Import from" → "URL" o "Local File"
3. Selecciona el SVG descargado
4. Click "Import"

---

## 🔗 OPCIÓN 4: COPIAR DESDE PÁGINA HTML

### Paso 1: Usar página interactiva
Abre: `diagrama-arquitectura-interactivo.html` en navegador

### Paso 2: Capturar pantalla
1. Presiona Impr Pant (Print Screen)
2. Pega en draw.io nuevo

O:
1. Click derecho en diagrama → "Guardar imagen como"
2. Guarda como PNG
3. En draw.io: "Insert" → "Image" → selecciona PNG

---

## 📋 ESTRUCTURA DEL ARCHIVO .DRAWIO

El archivo `arquitectura-sistema.drawio` contiene:

### **Diagrama 1: Arquitectura Sistema Completo**

**Componentes**:
```
┌─────────────────────────────────────────────────────┐
│                    CLIENTE                          │
│              Vue.js 3 SPA Frontend                  │
└──────────────────────┬────────────────────────────┘
                       │ HTTPS
┌──────────────────────▼────────────────────────────┐
│            API GATEWAY:8080                        │
│  - Routing                                         │
│  - JWT Validation                                  │
│  - Rate Limiting                                   │
│  - CORS                                            │
└──┬────┬────┬────┬────┬────┬────┬────┬────────────┘
   │    │    │    │    │    │    │    │
┌──▼─┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐┌─▼──┐
│Auth││Est ││Inst││Veh ││Asig││Cob ││Rep ││Not │
└────┘└────┘└────┘└────┘└────┘└────┘└────┘└────┘
   │    │    │    │    │    │    │    │
└────┴────┴────┴────┴────┴────┴────┴────────┘
        ↓ JDBC, RabbitMQ, Service Discovery
┌──────────────────────────────────────────────────┐
│   PostgreSQL  │  RabbitMQ  │  Eureka  │  MinIO  │
│   9 Schemas   │   Events   │ Discovery│ Storage │
└──────────────────────────────────────────────────┘
```

### **Diagrama 2: Procesos Principales**

**5 Flujos**:
1. ✅ Autenticación
2. ✅ Matrícula
3. ✅ Programación (6 validaciones)
4. ✅ Cobros
5. ✅ Reportes

---

## ✅ VALIDACIÓN POST-IMPORTACIÓN

Después de importar, verifica que contenga:

**Diagrama 1 Checklist**:
- [ ] Cliente/Frontend visible (verde)
- [ ] API Gateway (azul)
- [ ] 8 Microservicios (colores variados)
- [ ] Eureka, PostgreSQL, RabbitMQ, MinIO presentes
- [ ] Conexiones entre componentes claras

**Diagrama 2 Checklist**:
- [ ] 5 procesos principales listados
- [ ] Flujos con flechas direccionales
- [ ] Colores diferenciados por proceso
- [ ] Descripciones legibles

---

## 🛠️ EDITAR EN DRAW.IO

### Cambiar color de un componente
1. Selecciona el componente
2. Panel derecho → "Format" → "Fill"
3. Elige color

### Agregar nuevo componente
1. Panel izquierdo → "Insert" 
2. Arrastra componente al canvas
3. Escribe etiqueta

### Conectar componentes
1. Hover sobre elemento → puntos azules aparecen
2. Arrastra desde punto a otro componente
3. Aparecerá línea de conexión

### Cambiar estilo de línea
1. Selecciona línea
2. Panel derecho → "Format" → "Line"
3. Ajusta grosor, estilo (punteada/sólida), etc

---

## 💾 GUARDAR Y EXPORTAR

### Guardar en draw.io
- "File" → "Save" (overwrite original)
- O "File" → "Save As" (nuevo archivo)

### Exportar a otros formatos

**PNG** (Imagen):
```
File → Export As → PNG
```

**SVG** (Escalable):
```
File → Export As → SVG
```

**PDF** (Impresión):
```
File → Export As → PDF
```

**Mermaid** (Código):
```
File → Export As → Mermaid
```

---

## 🌐 COMPARTIR DIAGRAMAS

### Opción 1: Draw.io Online
1. "File" → "Save to" → "Google Drive / OneDrive / etc"
2. Obtén link compartible
3. Envía el link (cualquiera puede abrir)

### Opción 2: Descargar como archivo
1. "File" → "Download" como .drawio
2. Comparte el archivo

### Opción 3: Exportar imagen
1. "File" → "Export As" → PNG/SVG
2. Comparte la imagen

---

## 🔄 CONVERSIÓN FORMATOS

```
┌─────────────────┐
│  Mermaid Code   │
│  (.md files)    │
└────────┬────────┘
         │
         ├──→ mermaid.live → SVG → draw.io
         │
         └──→ draw.io "Import" Mermaid direct
         
┌─────────────────┐
│   draw.io       │
│ (.drawio files) │
└────────┬────────┘
         │
         ├──→ PNG (Imagen)
         ├──→ SVG (Vectorial)
         ├──→ PDF (Print)
         └──→ Mermaid (Código)

┌─────────────────┐
│   HTML Page     │
│  (Interactive)  │
└────────┬────────┘
         │
         └──→ Screenshot → draw.io "Insert Image"
```

---

## 📱 ACCESO DESDE DIFERENTES DISPOSITIVOS

### **Desktop**
- Descarga: https://github.com/jgraph/drawio-desktop
- Online: https://app.diagrams.net/

### **Web Browser**
- Online: https://app.diagrams.net/ (Chrome, Firefox, Edge, Safari)

### **Móvil** (iOS/Android)
- App: Draw.io available en AppStore / PlayStore
- O: draw.io web responsive

### **VS Code**
- Extension: "Draw.io Integration"
- Permite editar .drawio files dentro de VS Code

---

## 🎓 EJEMPLOS DE USO

### Para Defensa de Tesis
1. Abre `arquitectura-sistema.drawio` en draw.io
2. Exporta a PDF: "File" → "Export As" → "PDF"
3. Incluye en presentación (Beamer/PowerPoint)
4. O proyecta directo desde draw.io online

### Para Documentación Técnica
1. Exporta a SVG: "File" → "Export As" → "SVG"
2. Incluye en README.md:
   ```markdown
   ![Arquitectura](arquitectura.svg)
   ```

### Para Equipo Colaborativo
1. Guarda en Google Drive: "File" → "Save to" → "Google Drive"
2. Comparte el enlace
3. Otros pueden editar en tiempo real

---

## 🐛 TROUBLESHOOTING

### Error: "Formato no soportado"
**Solución**: 
- Usa formato .drawio directamente
- O convierte a SVG en mermaid.live primero

### Diagrama se ve distorsionado
**Solución**:
- Click en canvas
- "View" → "Reset View"
- O "View" → "Fit"

### No se puede importar archivo
**Solución**:
- Verifica que sea formato .drawio válido
- Intenta abrir desde "File" → "Open from Computer"
- O copia el contenido a un archivo nuevo

### Líneas de conexión se mueven raras
**Solución**:
- Click derecho en línea → "Edit Style"
- Ajusta "waypoints" o usa "Orthogonal" routing

---

## 📚 RECURSOS ADICIONALES

| Recurso | URL | Descripción |
|---------|-----|-------------|
| Draw.io Online | https://app.diagrams.net/ | Editor web principal |
| Draw.io Desktop | https://github.com/jgraph/drawio-desktop | App desktop |
| Mermaid Live | https://mermaid.live/ | Convertir Mermaid → SVG |
| Draw.io Docs | https://desk.draw.io/ | Documentación oficial |
| GitHub Mermaid | https://docs.github.com/en/get-started/writing-on-github/working-with-advanced-formatting/creating-diagrams | Sintaxis Mermaid |

---

## ✅ CHECKLIST FINAL

- [ ] Archivo `arquitectura-sistema.drawio` descargado
- [ ] draw.io abierto (online u desktop)
- [ ] Archivo importado/abierto en draw.io
- [ ] Diagramas visibles y claros
- [ ] Colores y estilos correctos
- [ ] Componentes identificables
- [ ] Conexiones entre elementos claras
- [ ] Guardado o exportado en formato deseado

---

## 🎯 RESUMEN RÁPIDO

```
1. Descarga: arquitectura-sistema.drawio
2. Abre: https://app.diagrams.net/
3. Importa: File → Open → selecciona archivo
4. Edita: Como necesites
5. Exporta: PNG/SVG/PDF para presentación
```

---

**Status**: ✅ Listo para draw.io  
**Archivos**: 1 .drawio + 10 Mermaid + 1 HTML interactivo  
**Componentes**: 8 MS + Infraestructura + Procesos  
**Validaciones**: 15 criterios técnicos completados

