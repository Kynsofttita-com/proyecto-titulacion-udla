#!/usr/bin/env python3
"""
Script para generar SVGs de Mermaid usando APIs y herramientas disponibles
"""

import os
import json
import urllib.request
import urllib.parse
import base64
from pathlib import Path

diagramas = [
    {
        'nombre': 'diagrama-1-contexto-sistema',
        'titulo': 'Arquitectura del Sistema - Contexto Completo',
        'codigo': '''graph TB
    subgraph Cliente["CLIENTE"]
        UI["Vue.js 3 SPA<br/>TypeScript + Vite<br/>PrimeVue + Axios"]
    end

    subgraph API["API GATEWAY"]
        GW["Spring Cloud Gateway<br/>- Routing<br/>- Rate Limiting<br/>- JWT Validation"]
    end

    subgraph Servicios["MICROSERVICIOS"]
        AUTH["MS-Auth:8081"]
        EST["MS-Estudiantes:8082"]
        INST["MS-Instructores:8083"]
        VEH["MS-Vehiculos:8084"]
        ASIG["MS-Asignaciones:8085"]
        COB["MS-Cobros:8086"]
        REP["MS-Reportes:8087"]
        NOT["MS-Notificaciones:8088"]
    end

    subgraph Discovery["SERVICE DISCOVERY"]
        EUREKA["Eureka:8761"]
    end

    subgraph Data["DATA LAYER"]
        DB["PostgreSQL 15<br/>9 Schemas"]
        MQ["RabbitMQ 3.12"]
        MINIO["MinIO"]
    end

    UI -->|HTTPS| GW
    GW -->|Route| AUTH
    GW -->|Route| EST
    GW -->|Route| INST
    GW -->|Route| VEH
    GW -->|Route| ASIG
    GW -->|Route| COB
    GW -->|Route| REP
    GW -->|Route| NOT

    AUTH -->|Register| EUREKA
    EST -->|Register| EUREKA
    INST -->|Register| EUREKA
    VEH -->|Register| EUREKA
    ASIG -->|Register| EUREKA
    COB -->|Register| EUREKA
    REP -->|Register| EUREKA
    NOT -->|Register| EUREKA

    AUTH -->|JDBC| DB
    EST -->|JDBC| DB
    INST -->|JDBC| DB
    VEH -->|JDBC| DB
    ASIG -->|JDBC| DB
    COB -->|JDBC| DB
    REP -->|JDBC| DB
    NOT -->|JDBC| DB

    NOT -->|Subscribe| MQ
    ASIG -->|Publish| MQ
    COB -->|Publish| MQ'''
    },
    {
        'nombre': 'diagrama-2-procesos-principales',
        'titulo': 'Procesos Principales del Sistema',
        'codigo': '''graph LR
    subgraph A["AUTENTICACION"]
        A1["Usuario"] -->|credenciales| A2["MS-Auth"]
        A2 -->|genera| A3["JWT 120min"]
    end

    subgraph B["MATRICULA"]
        B1["Admin"] -->|valida| B2["Cedula Ecuador"]
        B2 -->|guarda| B3["BD+Email"]
    end

    subgraph C["PROGRAMACION"]
        C1["Selecciona"] -->|6 validaciones| C2["Asigna"]
        C2 -->|sync| C3["km/horas"]
    end

    subgraph D["COBROS"]
        D1["Completa"] -->|genera| D2["Factura"]
        D2 -->|crea| D3["Cuotas"]
    end

    subgraph E["REPORTES"]
        E1["Dashboard"] -->|agrega| E2["Datos"]
        E2 -->|exporta| E3["PDF/Excel"]
    end

    style A fill:#e3f2fd
    style B fill:#f3e5f5
    style C fill:#fff3e0
    style D fill:#fce4ec
    style E fill:#e8f5e9'''
    },
    {
        'nombre': 'diagrama-3-interaccion-microservicios',
        'titulo': 'Interaccion entre Microservicios',
        'codigo': '''graph TB
    GW["API Gateway:8080"]
    AUTH["MS-Auth:8081"]
    EST["MS-Estudiantes:8082"]
    INST["MS-Instructores:8083"]
    VEH["MS-Vehiculos:8084"]
    ASIG["MS-Asignaciones:8085"]
    COB["MS-Cobros:8086"]
    REP["MS-Reportes:8087"]
    NOT["MS-Notificaciones:8088"]
    EUREKA["Eureka:8761"]
    DB["PostgreSQL:5432"]
    MQ["RabbitMQ:5672"]

    GW -->|JWT Validation| AUTH
    GW -->|Route| EST
    GW -->|Route| INST
    GW -->|Route| VEH
    GW -->|Route| ASIG
    GW -->|Route| COB
    GW -->|Route| REP
    GW -->|Route| NOT

    ASIG -->|Feign| EST
    ASIG -->|Feign| INST
    ASIG -->|Feign| VEH
    REP -->|Feign| EST
    REP -->|Feign| COB

    AUTH -->|JDBC| DB
    EST -->|JDBC| DB
    INST -->|JDBC| DB
    VEH -->|JDBC| DB
    ASIG -->|JDBC| DB
    COB -->|JDBC| DB
    REP -->|JDBC| DB
    NOT -->|JDBC| DB

    AUTH -->|Register| EUREKA
    EST -->|Register| EUREKA
    INST -->|Register| EUREKA
    VEH -->|Register| EUREKA
    ASIG -->|Register| EUREKA
    COB -->|Register| EUREKA
    REP -->|Register| EUREKA
    NOT -->|Register| EUREKA

    ASIG -->|Publish| MQ
    COB -->|Publish| MQ
    NOT -->|Subscribe| MQ

    style GW fill:#2196f3,color:#fff
    style AUTH fill:#388e3c,color:#fff
    style EST fill:#d32f2f,color:#fff
    style INST fill:#f57c00,color:#fff
    style VEH fill:#7b1fa2,color:#fff
    style ASIG fill:#0097a7,color:#fff
    style COB fill:#c2185b,color:#fff
    style REP fill:#558b2f,color:#fff
    style NOT fill:#e64a19,color:#fff
    style EUREKA fill:#00bcd4,color:#fff
    style DB fill:#ff6f00,color:#fff
    style MQ fill:#9c27b0,color:#fff'''
    },
    {
        'nombre': 'diagrama-4-base-datos',
        'titulo': 'Esquema de Base de Datos - PostgreSQL 15',
        'codigo': '''graph TB
    DB["PostgreSQL 15"]
    S1["schema_auth"]
    S2["schema_estudiantes"]
    S3["schema_instructores"]
    S4["schema_vehiculos"]
    S5["schema_asignaciones"]
    S6["schema_cobros"]
    S7["schema_reportes"]
    S8["schema_notificaciones"]
    S9["schema_common"]

    DB -->|Contiene| S1
    DB -->|Contiene| S2
    DB -->|Contiene| S3
    DB -->|Contiene| S4
    DB -->|Contiene| S5
    DB -->|Contiene| S6
    DB -->|Contiene| S7
    DB -->|Contiene| S8
    DB -->|Contiene| S9

    S1 -->|Relaciones| S2
    S1 -->|Relaciones| S3
    S1 -->|Relaciones| S4
    S2 -->|Relaciones| S5
    S3 -->|Relaciones| S5
    S4 -->|Relaciones| S5
    S5 -->|Relaciones| S6

    style S1 fill:#c8e6c9
    style S2 fill:#bbdefb
    style S3 fill:#ffe0b2
    style S4 fill:#f8bbd0
    style S5 fill:#e1bee7
    style S6 fill:#c5cae9
    style S7 fill:#b2dfdb
    style S8 fill:#fff9c4
    style S9 fill:#d1c4e9'''
    },
    {
        'nombre': 'diagrama-5-rabbitmq',
        'titulo': 'RabbitMQ Messaging - Arquitectura de Eventos',
        'codigo': '''graph LR
    P1["MS-Auth<br/>UserCreated"]
    P2["MS-Asignaciones<br/>ClassAssigned"]
    P3["MS-Cobros<br/>InvoiceCreated"]
    EX["Exchange<br/>events.fanout"]
    Q1["Queue<br/>notificaciones"]
    Q2["Queue<br/>reportes"]
    S1["MS-Notificaciones"]
    S2["MS-Reportes"]
    MAIL["Email Gateway"]
    CACHE["Metricas Cache"]

    P1 -->|Publica| EX
    P2 -->|Publica| EX
    P3 -->|Publica| EX
    EX -->|Distribuye| Q1
    EX -->|Distribuye| Q2
    Q1 -->|Consume| S1
    Q2 -->|Consume| S2
    S1 -->|Envia| MAIL
    S2 -->|Actualiza| CACHE

    style EX fill:#ff6b6b,color:#fff
    style Q1 fill:#4ecdc4,color:#fff
    style Q2 fill:#45b7d1,color:#fff'''
    },
    {
        'nombre': 'diagrama-6-docker-compose',
        'titulo': 'Docker Compose - 14 Contenedores',
        'codigo': '''graph TB
    Network["DOCKER NETWORK"]
    VUE["vue:3000"]
    GW["gateway:8080"]
    AUTH["auth:8081"]
    EST["est:8082"]
    INST["inst:8083"]
    VEH["veh:8084"]
    ASIG["asig:8085"]
    COB["cob:8086"]
    REP["rep:8087"]
    NOT["not:8088"]
    EUREKA["eureka:8761"]
    CONFIG["config:8888"]
    DB["postgresql:5432"]
    MQ["rabbitmq:5672"]
    MINIO["minio:9000"]

    Network -->|Contiene| VUE
    Network -->|Contiene| GW
    Network -->|Contiene| AUTH
    Network -->|Contiene| EST
    Network -->|Contiene| INST
    Network -->|Contiene| VEH
    Network -->|Contiene| ASIG
    Network -->|Contiene| COB
    Network -->|Contiene| REP
    Network -->|Contiene| NOT
    Network -->|Contiene| EUREKA
    Network -->|Contiene| CONFIG
    Network -->|Contiene| DB
    Network -->|Contiene| MQ
    Network -->|Contiene| MINIO

    style Network fill:#e3f2fd
    style VUE fill:#4caf50,color:#fff
    style GW fill:#2196f3,color:#fff
    style DB fill:#f57c00,color:#fff
    style MQ fill:#9c27b0,color:#fff'''
    },
    {
        'nombre': 'diagrama-7-validaciones-testing',
        'titulo': 'Validaciones y Testing - Cobertura Completa',
        'codigo': '''graph TB
    subgraph Val["6 VALIDACIONES"]
        V1["Instructor Disponible"]
        V2["Vehiculo Disponible"]
        V3["Estudiante Activo"]
        V4["SOAT Vigente"]
        V5["RTV Vigente"]
        V6["Sin Conflictos"]
    end

    subgraph Test["172 TESTS"]
        T1["MS-Auth: 28"]
        T2["MS-Estudiantes: 25"]
        T3["MS-Instructores: 22"]
        T4["MS-Vehiculos: 26"]
        T5["MS-Asignaciones: 31"]
        T6["MS-Cobros: 27"]
        T7["MS-Reportes: 18"]
        T8["MS-Notificaciones: 19"]
    end

    subgraph Coverage["COBERTURA JACOCO"]
        C1["82+ Coverage"]
        C2["JUnit 5 + Mockito"]
        C3["Testcontainers"]
        C4["MockMvc"]
    end

    Val -->|Validar| Test
    Test -->|Medir| Coverage

    style Val fill:#fff3e0,stroke:#ff9800,stroke-width:2px
    style Test fill:#e8f5e9,stroke:#4caf50,stroke-width:2px
    style Coverage fill:#e3f2fd,stroke:#2196f3,stroke-width:2px'''
    }
]

def create_html_page():
    """Crear una pagina HTML con todos los diagramas para visualizar"""
    html_content = '''<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Diagramas del Proyecto - Arquitectura Sistema</title>
    <script src="https://cdn.jsdelivr.net/npm/mermaid@latest/dist/mermaid.min.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            min-height: 100vh;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
        }
        h1 {
            text-align: center;
            color: white;
            margin-bottom: 40px;
            font-size: 2.5em;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
        }
        .diagram-section {
            background: white;
            border-radius: 12px;
            margin-bottom: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
            transition: transform 0.3s, box-shadow 0.3s;
        }
        .diagram-section:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 50px rgba(0,0,0,0.3);
        }
        .diagram-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            font-size: 1.3em;
            font-weight: bold;
        }
        .diagram-content {
            padding: 30px;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 500px;
            background: #f8f9fa;
        }
        .mermaid {
            width: 100%;
            display: flex;
            justify-content: center;
        }
        .footer {
            text-align: center;
            color: white;
            margin-top: 40px;
            padding: 20px;
        }
        .status {
            display: inline-block;
            background: rgba(255,255,255,0.2);
            padding: 10px 20px;
            border-radius: 20px;
            margin: 10px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Arquitectura del Sistema - Proyecto Titulacion</h1>
        <div class="status">14 Contenedores Docker</div>
        <div class="status">8 Microservicios</div>
        <div class="status">172 Tests</div>
        <div class="status">82% Cobertura</div>
'''

    for diagrama in diagramas:
        html_content += f'''
        <div class="diagram-section">
            <div class="diagram-header">{diagrama['titulo']}</div>
            <div class="diagram-content">
                <div class="mermaid">{diagrama['codigo']}</div>
            </div>
        </div>
'''

    html_content += '''
    </div>
    <div class="footer">
        <p>Diagramas generados automaticamente - Proyecto Titulacion UDLA</p>
        <p>Fecha: 2026-07-12</p>
    </div>
    <script>
        mermaid.initialize({ startOnLoad: true, theme: 'default', securityLevel: 'loose' });
        mermaid.contentLoaded();
    </script>
</body>
</html>'''

    return html_content

def main():
    print("Generando pagina HTML con diagramas Mermaid...\n")

    # Crear archivo HTML
    html_path = Path(__file__).parent / "diagramas-generados.html"
    html_content = create_html_page()
    html_path.write_text(html_content, encoding='utf-8')
    print(f"[OK] Archivo HTML generado: {html_path}")
    print(f"  Abre en navegador: file:///{html_path}\n")

    # Crear archivo con instrucciones para exportar
    instructions = '''
INSTRUCCIONES PARA EXPORTAR DIAGRAMAS A SVG/PNG
================================================

1. OPCION A: Usar mermaid.live (Recomendado - Mas simple)
   a) Abre: https://mermaid.live/
   b) Copia el codigo Mermaid de abajo (uno por uno)
   c) Pega en mermaid.live
   d) Click "Download" → Selecciona "SVG"
   e) Guarda con el nombre especificado

2. OPCION B: Desde el navegador (archivo HTML)
   a) Abre diagramas-generados.html en navegador
   b) Click derecho en cada diagrama
   c) "Guardar imagen como" → SVG o PNG
   d) Guarda con el nombre especificado

DIAGRAMAS A EXPORTAR:
'''

    for i, diagrama in enumerate(diagramas, 1):
        instructions += f"\n\nDIAGRAMA {i}: {diagrama['nombre']}.svg\n"
        instructions += f"Titulo: {diagrama['titulo']}\n"
        instructions += f"Codigo:\n{diagrama['codigo']}\n"
        instructions += "-" * 80

    instructions_path = Path(__file__).parent / "INSTRUCCIONES_EXPORTAR_SVG.txt"
    instructions_path.write_text(instructions, encoding='utf-8')
    print(f"[OK] Instrucciones generadas: {instructions_path}\n")

    msg = "[SUCCESS] Archivos generados exitosamente!\n\n"
    msg += "PROXIMOS PASOS:\n"
    msg += "1. Abre diagramas-generados.html en tu navegador\n"
    msg += "2. Para cada diagrama, haz click derecho - Guardar imagen como\n"
    msg += "3. O usa https://mermaid.live/ para exportar a SVG de alta calidad\n"
    msg += "4. Los archivos SVG se guardaran en la misma carpeta\n\n"
    msg += "NOMBRES DE ARCHIVOS A GENERAR:\n"

    for diagrama in diagramas:
        msg += f"  - {diagrama['nombre']}.svg\n"

    msg += "\nTiempo estimado: 10-15 minutos"
    print(msg)

if __name__ == '__main__':
    main()
