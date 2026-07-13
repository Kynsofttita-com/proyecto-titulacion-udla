#!/usr/bin/env python3
"""
Generar PDF profesional con diagrama de proceso principal para asignacion
"""

from reportlab.lib.pagesizes import letter, A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch, cm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, Table, TableStyle, PageBreak
from reportlab.lib import colors
from reportlab.pdfgen import canvas
from datetime import datetime
from pathlib import Path

def crear_pdf_asignacion():
    """Crear PDF con diagrama principal para asignacion"""

    output_path = Path(__file__).parent / "DIAGRAMA_PROCESO_PRINCIPAL.pdf"
    doc = SimpleDocTemplate(
        str(output_path),
        pagesize=A4,
        rightMargin=0.5*inch,
        leftMargin=0.5*inch,
        topMargin=0.5*inch,
        bottomMargin=0.5*inch
    )

    # Elementos del documento
    elements = []
    styles = getSampleStyleSheet()

    # Estilos personalizados
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=28,
        textColor=colors.HexColor('#1a237e'),
        spaceAfter=20,
        alignment=1,  # Center
        fontName='Helvetica-Bold'
    )

    subtitle_style = ParagraphStyle(
        'CustomSubtitle',
        parent=styles['Heading2'],
        fontSize=14,
        textColor=colors.HexColor('#0277bd'),
        spaceAfter=12,
        alignment=1,
        fontName='Helvetica-Bold'
    )

    heading_style = ParagraphStyle(
        'CustomHeading',
        parent=styles['Heading3'],
        fontSize=12,
        textColor=colors.HexColor('#1565c0'),
        spaceAfter=10,
        fontName='Helvetica-Bold'
    )

    body_style = ParagraphStyle(
        'CustomBody',
        parent=styles['BodyText'],
        fontSize=10,
        spaceAfter=8,
        alignment=4,  # Justify
    )

    # Portada
    elements.append(Spacer(1, 1*inch))
    elements.append(Paragraph("UNIVERSIDAD DE LAS AMERICAS", title_style))
    elements.append(Spacer(1, 6))
    elements.append(Paragraph("CARRERA DE INGENIERIA EN SISTEMAS", subtitle_style))
    elements.append(Spacer(1, 0.3*inch))
    elements.append(Paragraph("PROYECTO DE TITULACION", title_style))
    elements.append(Spacer(1, 0.5*inch))

    elements.append(Paragraph(
        "DIAGRAMA DE PROCESO PRINCIPAL DEL SISTEMA",
        ParagraphStyle('title2', parent=styles['Heading2'], fontSize=18,
                      textColor=colors.HexColor('#1a237e'), alignment=1, spaceAfter=20)
    ))

    elements.append(Spacer(1, 0.3*inch))
    elements.append(Paragraph(
        "Sistema de Control Administrativo y Financiero para Escuelas de Conduccion",
        ParagraphStyle('subtitle2', parent=styles['Normal'], fontSize=11,
                      alignment=1, spaceAfter=20, textColor=colors.HexColor('#424242'))
    ))

    elements.append(Spacer(1, 0.5*inch))

    # Informacion del proyecto
    info_data = [
        ['Estudiantes:', 'Raul Sebastian Cruz Bano, Hernan Mateo Jurado Moran'],
        ['Tutor:', 'Victor Javier Gomez Regalado'],
        ['Universidad:', 'Universidad de las Americas (UDLA)'],
        ['Fecha:', datetime.now().strftime('%d de %B de %Y')],
        ['Periodo:', 'Capstone Project - Ciclo II 2026']
    ]

    info_table = Table(info_data, colWidths=[2*cm, 13*cm])
    info_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (0, -1), colors.HexColor('#e3f2fd')),
        ('TEXTCOLOR', (0, 0), (-1, -1), colors.black),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (0, -1), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, -1), 10),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
        ('TOPPADDING', (0, 0), (-1, -1), 8),
        ('GRID', (0, 0), (-1, -1), 1, colors.HexColor('#bdbdbd'))
    ]))

    elements.append(info_table)
    elements.append(PageBreak())

    # Indice
    elements.append(Paragraph("INDICE DE CONTENIDOS", heading_style))
    elements.append(Spacer(1, 12))

    indice_items = [
        "1. Resumen Ejecutivo",
        "2. Diagrama de Contexto del Sistema (Nivel Mayor)",
        "3. Procesos Principales del Sistema",
        "4. Componentes Principales",
        "5. Arquitectura Tecnica",
        "6. Tecnologias Utilizadas"
    ]

    for item in indice_items:
        elements.append(Paragraph(item, body_style))
        elements.append(Spacer(1, 6))

    elements.append(PageBreak())

    # 1. Resumen Ejecutivo
    elements.append(Paragraph("1. RESUMEN EJECUTIVO", heading_style))
    elements.append(Spacer(1, 8))

    resumen_text = """
    El Sistema de Control Administrativo y Financiero para Escuelas de Conduccion es una
    solucion integral desarrollada en arquitectura de microservicios que permite gestionar
    de manera eficiente todos los procesos operacionales y financieros de una escuela de
    conduccion. El sistema integra 8 microservicios independientes, una base de datos PostgreSQL
    con 9 esquemas logicamente separados, y un sistema de mensajeria asincrona con RabbitMQ.
    <br/><br/>
    El diagrama de proceso principal ilustra como el cliente (Vue.js 3 SPA) interactua con
    el API Gateway, que a su vez enruta las solicitudes a los microservicios correspondientes.
    Cada microservicio es responsable de un dominio especifico del negocio y se comunica con
    la base de datos central y el sistema de mensajeria para coordinar procesos asincrónicos.
    """

    elements.append(Paragraph(resumen_text, body_style))
    elements.append(Spacer(1, 12))

    # 2. Diagrama de Contexto
    elements.append(Paragraph("2. DIAGRAMA DE CONTEXTO DEL SISTEMA (NIVEL MAYOR)", heading_style))
    elements.append(Spacer(1, 8))

    elementos_contexto = """
    Este diagrama ilustra los componentes principales del sistema y como se relacionan entre si:
    <br/><br/>
    <b>Cliente (Frontend):</b> Aplicacion Vue.js 3 desarrollada en TypeScript con Vite,
    PrimeVue para componentes UI, y Axios para comunicacion HTTP.<br/><br/>

    <b>API Gateway:</b> Spring Cloud Gateway que actua como punto de entrada unico, responsable de
    routing, validacion JWT, rate limiting y CORS.<br/><br/>

    <b>Microservicios (8 servicios):</b> Cada uno especializado en un dominio de negocio,
    comunicandose entre si mediante Feign clients y con la base de datos mediante JDBC.<br/><br/>

    <b>Service Discovery (Eureka):</b> Responsable del registro dinamico y descubrimiento
    de servicios en el ecosistema de microservicios.<br/><br/>

    <b>Data Layer:</b> PostgreSQL para datos persistentes, RabbitMQ para eventos asincrónicos,
    MinIO para almacenamiento de archivos compatible con S3.
    """

    elements.append(Paragraph(elementos_contexto, body_style))
    elements.append(Spacer(1, 12))

    # Descripcion visual del Diagrama 1
    elements.append(Paragraph("DIAGRAMA 1: ARQUITECTURA DEL SISTEMA - CONTEXTO COMPLETO",
                            ParagraphStyle('diagram_title', parent=styles['Normal'],
                                         fontSize=10, alignment=1,
                                         textColor=colors.HexColor('#1565c0'),
                                         fontName='Helvetica-Bold')))
    elements.append(Spacer(1, 8))

    diagrama1_desc = """
    <b>Estructura del Diagrama:</b><br/>
    &nbsp;&nbsp;&nbsp;&nbsp;VUE.JS 3 SPA (Cliente)<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↓ HTTPS<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;SPRING CLOUD GATEWAY:8080<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ Routing<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ Rate Limiting<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├─ JWT Validation<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└─ CORS<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↓<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;MS-AUTH:8081, MS-EST:8082, MS-INST:8083, MS-VEH:8084, MS-ASIG:8085, MS-COB:8086, MS-REP:8087, MS-NOT:8088<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↓<br/>
    &nbsp;&nbsp;&nbsp;&nbsp;POSTGRESQL 15 (9 schemas) | RABBITMQ 3.12 | MINIO | EUREKA:8761<br/>
    <br/>
    <b>Flujos Principales:</b> Cliente → Gateway → 8 MS → Base Datos, Mensajeria, Descubrimiento
    """

    elements.append(Paragraph(diagrama1_desc, body_style))
    elements.append(Spacer(1, 8))

    elements.append(PageBreak())

    # 3. Procesos Principales
    elements.append(Paragraph("3. PROCESOS PRINCIPALES DEL SISTEMA", heading_style))
    elements.append(Spacer(1, 8))

    procesos_text = """
    El sistema gestiona 5 procesos principales que cubren el ciclo de vida completo de operacion
    de una escuela de conduccion:
    """
    elements.append(Paragraph(procesos_text, body_style))
    elements.append(Spacer(1, 8))

    procesos = [
        ("1. AUTENTICACION",
         "Los usuarios ingresa credenciales, MS-Auth valida y genera un JWT de 120 minutos "
         "que se almacena en una HttpOnly cookie para mantener la sesion activa."),

        ("2. MATRICULA DE ESTUDIANTES",
         "El administrador crea un nuevo estudiante, valida la cedula ecuatoriana, "
         "y guarda los datos en la base de datos, enviando notificacion por email."),

        ("3. PROGRAMACION DE CLASES",
         "Se selecciona instructor + estudiante + vehiculo, se ejecutan 6 validaciones "
         "(disponibilidad, documentos vigentes, sin conflictos), se sincronizan km/horas "
         "y se notifica a los 3 actores involucrados."),

        ("4. COBROS Y PAGOS",
         "Al completar las horas requeridas, se genera una factura, se crean cuotas de pago, "
         "y se envian notificaciones al estudiante."),

        ("5. REPORTES Y ANALYTICS",
         "El dashboard agrega datos de multiples microservicios, calcula KPIs operacionales "
         "y financieros, y exporta reportes en PDF/Excel.")
    ]

    for titulo, descripcion in procesos:
        elements.append(Paragraph(f"<b>{titulo}:</b> {descripcion}", body_style))
        elements.append(Spacer(1, 6))

    elements.append(Spacer(1, 12))

    # Descripcion visual del Diagrama 2
    elements.append(Paragraph("DIAGRAMA 2: PROCESOS PRINCIPALES DEL SISTEMA",
                            ParagraphStyle('diagram_title', parent=styles['Normal'],
                                         fontSize=10, alignment=1,
                                         textColor=colors.HexColor('#1565c0'),
                                         fontName='Helvetica-Bold')))
    elements.append(Spacer(1, 8))

    diagrama2_desc = """
    <b>Flujos Operacionales (5 procesos principales):</b><br/><br/>

    <b>FLUJO 1 - AUTENTICACION:</b><br/>
    Usuario → Ingresa credenciales → MS-Auth valida → Genera JWT 120min → HttpOnly Cookie → Sesion activa<br/><br/>

    <b>FLUJO 2 - MATRICULA:</b><br/>
    Admin crea estudiante → Valida cedula Ecuador → Guarda en BD → Envia Email<br/><br/>

    <b>FLUJO 3 - PROGRAMACION (6 validaciones):</b><br/>
    Selecciona (instructor + estudiante + vehiculo) → 6 Validaciones → OK Asigna → Sync km/horas → Notifica 3 actores<br/><br/>

    <b>FLUJO 4 - COBROS:</b><br/>
    Completa horas → Genera factura → Crea cuotas → Envia Email<br/><br/>

    <b>FLUJO 5 - REPORTES:</b><br/>
    Dashboard → Agrega datos → Calcula KPIs → Exporta PDF/Excel
    """

    elements.append(Paragraph(diagrama2_desc, body_style))

    elements.append(PageBreak())

    # 4. Componentes Principales
    elements.append(Paragraph("4. COMPONENTES PRINCIPALES", heading_style))
    elements.append(Spacer(1, 8))

    # Tabla de Microservicios
    ms_data = [
        ['Microservicio', 'Puerto', 'Responsabilidad'],
        ['MS-Auth', '8081', 'Autenticacion y control de acceso'],
        ['MS-Estudiantes', '8082', 'Matricula y progreso academico'],
        ['MS-Instructores', '8083', 'Perfiles y disponibilidad'],
        ['MS-Vehiculos', '8084', 'Flota y mantenimiento'],
        ['MS-Asignaciones', '8085', 'Programacion de clases'],
        ['MS-Cobros', '8086', 'Facturacion y pagos'],
        ['MS-Reportes', '8087', 'Analytics y reporteria'],
        ['MS-Notificaciones', '8088', 'Emails y alertas'],
    ]

    ms_table = Table(ms_data, colWidths=[3.5*cm, 1.5*cm, 7.5*cm])
    ms_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1565c0')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 9),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
        ('BACKGROUND', (0, 1), (-1, -1), colors.HexColor('#eceff1')),
        ('GRID', (0, 0), (-1, -1), 1, colors.HexColor('#bdbdbd')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f5f5f5')]),
        ('FONTSIZE', (0, 1), (-1, -1), 9),
        ('TOPPADDING', (0, 1), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 1), (-1, -1), 6),
    ]))

    elements.append(ms_table)
    elements.append(Spacer(1, 12))

    # 5. Arquitectura Tecnica
    elements.append(PageBreak())
    elements.append(Paragraph("5. ARQUITECTURA TECNICA", heading_style))
    elements.append(Spacer(1, 8))

    arquitectura_text = """
    <b>Patrón Arquitectonico:</b> Microservicios con patrones de comunicacion sincrona (Feign)
    y asincrona (RabbitMQ).<br/><br/>

    <b>Base de Datos:</b> PostgreSQL 15 con 9 esquemas logicamente separados, uno por dominio
    de negocio, garantizando independencia y escalabilidad.<br/><br/>

    <b>Mensajeria:</b> RabbitMQ con exchange fanout que distribuye eventos a multiples
    suscriptores de forma desacoplada.<br/><br/>

    <b>Descubrimiento de Servicios:</b> Eureka Server que mantiene un registro dinamico
    de todos los microservicios y facilita el load balancing.<br/><br/>

    <b>Almacenamiento:</b> MinIO compatible con S3 para documentos, certificados y archivos.<br/><br/>

    <b>Validaciones:</b> 6 validaciones criticas en la creacion de asignaciones para garantizar
    integridad operacional.
    """

    elements.append(Paragraph(arquitectura_text, body_style))
    elements.append(Spacer(1, 12))

    # 6. Tecnologias
    elements.append(Paragraph("6. TECNOLOGIAS UTILIZADAS", heading_style))
    elements.append(Spacer(1, 8))

    tech_data = [
        ['Capa', 'Tecnologia'],
        ['Frontend', 'Vue.js 3 (TypeScript, Vite, PrimeVue, Axios)'],
        ['API Gateway', 'Spring Cloud Gateway'],
        ['Microservicios', 'Java 21, Spring Boot 3.4, Spring Cloud'],
        ['Base de Datos', 'PostgreSQL 15 (9 schemas)'],
        ['Mensajeria', 'RabbitMQ 3.12'],
        ['Descubrimiento', 'Eureka Server'],
        ['Almacenamiento', 'MinIO (S3 Compatible)'],
        ['Contenedores', 'Docker (14 contenedores en docker-compose)'],
        ['Testing', 'JUnit 5, Mockito (172 tests, 82%+ coverage)'],
    ]

    tech_table = Table(tech_data, colWidths=[3*cm, 10*cm])
    tech_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#0277bd')),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 9),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
        ('BACKGROUND', (0, 1), (-1, -1), colors.HexColor('#e1f5fe')),
        ('GRID', (0, 0), (-1, -1), 1, colors.HexColor('#81d4fa')),
        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f0f7fa')]),
        ('FONTSIZE', (0, 1), (-1, -1), 9),
        ('TOPPADDING', (0, 1), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 1), (-1, -1), 6),
    ]))

    elements.append(tech_table)
    elements.append(Spacer(1, 24))

    # Pie de pagina
    elements.append(Paragraph("_" * 80, ParagraphStyle('line', fontSize=1)))
    elements.append(Spacer(1, 8))

    footer_text = f"""
    <font size="8">
    Documento generado: {datetime.now().strftime('%d/%m/%Y %H:%M')}<br/>
    Proyecto de Titulacion - UDLA<br/>
    Diagrama de Proceso Principal del Sistema
    </font>
    """

    elements.append(Paragraph(footer_text,
                            ParagraphStyle('footer', parent=styles['Normal'],
                                         fontSize=8, alignment=1,
                                         textColor=colors.HexColor('#666666'))))

    # Construir PDF
    doc.build(elements)

    print(f"\n[OK] PDF generado: {output_path}")
    print(f"[OK] Tamaño: {output_path.stat().st_size / 1024:.1f} KB")
    return str(output_path)

if __name__ == '__main__':
    try:
        crear_pdf_asignacion()
        print("\n[SUCCESS] PDF listo para asignacion")
    except Exception as e:
        print(f"[ERROR] {e}")
        import traceback
        traceback.print_exc()
