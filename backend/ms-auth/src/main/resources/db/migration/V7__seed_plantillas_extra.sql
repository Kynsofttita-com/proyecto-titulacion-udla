-- =============================================================================
-- V7: Plantillas de email extra (invitación, morosidad, recordatorios)
-- =============================================================================
-- Añade 5 plantillas nuevas al conjunto ya sembrado en V1.5 (6 originales) para
-- cubrir escenarios operativos comunes: invitación de usuario, reporte semanal
-- de morosidad, recordatorio de cuota próxima a vencer, alerta SOAT y
-- notificación de nueva clase al instructor.
--
-- HTML profesional con inline CSS (compatible Gmail, Outlook, Apple Mail).
-- Idempotente: ON CONFLICT (codigo) DO NOTHING para tolerar re-ejecución.
-- =============================================================================

INSERT INTO auth_schema.plantillas_email (codigo, asunto, cuerpo_html, variables, activa, created_by) VALUES

-- 1. Invitación de usuario nuevo (staff, instructor)
(
    'INVITACION_USUARIO',
    'Bienvenido a {{escuela}} - Activa tu cuenta',
    '<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:20px;color:#333;">
      <div style="background:#0d47a1;color:#fff;padding:24px;text-align:center;border-radius:8px 8px 0 0;">
        <h1 style="margin:0;font-size:24px;">Bienvenido a {{escuela}}</h1>
      </div>
      <div style="background:#fff;padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 8px 8px;">
        <p style="font-size:16px;">Hola <strong>{{nombre}}</strong>,</p>
        <p>Se ha creado tu cuenta en el sistema de gestión de <strong>{{escuela}}</strong> con el rol de <strong>{{rol}}</strong>.</p>
        <p style="margin:24px 0;">Para activarla, hacé click en el siguiente botón y definí tu contraseña:</p>
        <p style="text-align:center;margin:32px 0;">
          <a href="{{enlace}}" style="background:#0d47a1;color:#fff;padding:12px 32px;text-decoration:none;border-radius:4px;font-weight:bold;display:inline-block;">Activar cuenta</a>
        </p>
        <p style="font-size:13px;color:#666;">Este enlace expira en 48 horas. Si no reconocés esta invitación, ignorá este mensaje.</p>
        <hr style="border:0;border-top:1px solid #e0e0e0;margin:24px 0;">
        <p style="font-size:12px;color:#999;text-align:center;margin:0;">
          {{escuela}} - Sistema de Gestión<br>
          Este es un mensaje automático, por favor no respondas.
        </p>
      </div>
    </div>',
    '["nombre", "escuela", "rol", "enlace"]'::jsonb,
    TRUE,
    'system'
),

-- 2. Reporte semanal de morosidad (para ADMIN)
(
    'REPORTE_MOROSIDAD_SEMANAL',
    '📊 Reporte semanal de morosidad - {{escuela}} ({{fecha}})',
    '<div style="font-family:Arial,Helvetica,sans-serif;max-width:700px;margin:0 auto;padding:20px;color:#333;">
      <div style="background:#c62828;color:#fff;padding:20px;border-radius:6px 6px 0 0;">
        <h2 style="margin:0;font-size:20px;">📊 Reporte de Morosidad Semanal</h2>
        <p style="margin:4px 0 0 0;font-size:13px;opacity:0.9;">{{escuela}} · {{fecha}}</p>
      </div>
      <div style="background:#fff;padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 6px 6px;">
        <p style="font-size:15px;">Hola <strong>{{nombre}}</strong>,</p>
        <p>Este es el resumen automático de morosidad de la semana:</p>
        <table style="width:100%;border-collapse:collapse;margin:20px 0;">
          <tr>
            <td style="background:#fef3f2;padding:16px;border:1px solid #fecaca;border-radius:4px;width:50%;">
              <div style="font-size:12px;color:#991b1b;text-transform:uppercase;font-weight:bold;">Facturas vencidas</div>
              <div style="font-size:28px;font-weight:bold;color:#c62828;margin-top:4px;">{{total_facturas}}</div>
            </td>
            <td style="width:16px;"></td>
            <td style="background:#fef3f2;padding:16px;border:1px solid #fecaca;border-radius:4px;width:50%;">
              <div style="font-size:12px;color:#991b1b;text-transform:uppercase;font-weight:bold;">Monto vencido</div>
              <div style="font-size:28px;font-weight:bold;color:#c62828;margin-top:4px;">${{monto_vencido}}</div>
            </td>
          </tr>
        </table>
        <table style="width:100%;border-collapse:collapse;margin:24px 0;font-size:13px;">
          <thead>
            <tr style="background:#f5f5f5;">
              <th style="text-align:left;padding:10px;border-bottom:2px solid #ddd;">Estudiante</th>
              <th style="text-align:right;padding:10px;border-bottom:2px solid #ddd;">Vencido</th>
              <th style="text-align:right;padding:10px;border-bottom:2px solid #ddd;">Días atraso</th>
            </tr>
          </thead>
          <tbody>
            {{tabla_top_morosos}}
          </tbody>
        </table>
        <p style="text-align:center;margin:24px 0;">
          <a href="{{enlace_reporte}}" style="background:#c62828;color:#fff;padding:10px 24px;text-decoration:none;border-radius:4px;font-weight:bold;">Ver reporte completo</a>
        </p>
        <hr style="border:0;border-top:1px solid #e0e0e0;margin:24px 0;">
        <p style="font-size:12px;color:#999;text-align:center;margin:0;">
          {{escuela}} - Reporte generado automáticamente todos los lunes a las 8am.
        </p>
      </div>
    </div>',
    '["nombre", "escuela", "fecha", "total_facturas", "monto_vencido", "tabla_top_morosos", "enlace_reporte"]'::jsonb,
    TRUE,
    'system'
),

-- 3. Recordatorio de cuota próxima a vencer (3 días antes)
(
    'RECORDATORIO_CUOTA_PROXIMA',
    'Recordatorio: tu cuota vence en {{dias_restantes}} días',
    '<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:20px;color:#333;">
      <div style="background:#f59e0b;color:#fff;padding:20px;border-radius:6px 6px 0 0;">
        <h2 style="margin:0;font-size:20px;">⏰ Recordatorio de Pago</h2>
      </div>
      <div style="background:#fff;padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 6px 6px;">
        <p style="font-size:16px;">Hola <strong>{{nombre}}</strong>,</p>
        <p>Te recordamos que tenés una cuota próxima a vencer:</p>
        <div style="background:#fffbeb;border-left:4px solid #f59e0b;padding:16px;margin:20px 0;border-radius:4px;">
          <p style="margin:0 0 8px 0;"><strong>Factura:</strong> {{numeroFactura}}</p>
          <p style="margin:0 0 8px 0;"><strong>Cuota:</strong> {{numero_cuota}} de {{total_cuotas}}</p>
          <p style="margin:0 0 8px 0;"><strong>Monto:</strong> <span style="font-size:20px;color:#f59e0b;font-weight:bold;">${{monto}}</span></p>
          <p style="margin:0;"><strong>Vence:</strong> {{fecha_vencimiento}} ({{dias_restantes}} días)</p>
        </div>
        <p>Para evitar recargos por mora, te sugerimos abonar antes del vencimiento.</p>
        <p style="text-align:center;margin:24px 0;">
          <a href="{{enlace_pago}}" style="background:#0d47a1;color:#fff;padding:12px 28px;text-decoration:none;border-radius:4px;font-weight:bold;display:inline-block;">Ver estado de cuenta</a>
        </p>
        <hr style="border:0;border-top:1px solid #e0e0e0;margin:24px 0;">
        <p style="font-size:12px;color:#999;text-align:center;margin:0;">
          Si ya realizaste el pago, ignorá este mensaje.<br>
          {{escuela}} - Sistema de Gestión
        </p>
      </div>
    </div>',
    '["nombre", "numeroFactura", "numero_cuota", "total_cuotas", "monto", "fecha_vencimiento", "dias_restantes", "enlace_pago", "escuela"]'::jsonb,
    TRUE,
    'system'
),

-- 4. Alerta SOAT próximo a vencer (para admins de flota)
(
    'ALERTA_SOAT_VENCIMIENTO',
    '🚨 SOAT del vehículo {{placa}} vence pronto',
    '<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:20px;color:#333;">
      <div style="background:#dc2626;color:#fff;padding:20px;border-radius:6px 6px 0 0;">
        <h2 style="margin:0;font-size:20px;">🚨 Alerta: SOAT próximo a vencer</h2>
      </div>
      <div style="background:#fff;padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 6px 6px;">
        <p style="font-size:15px;">Hola <strong>{{nombre}}</strong>,</p>
        <p>El SOAT del siguiente vehículo está próximo a vencer:</p>
        <div style="background:#fef2f2;border-left:4px solid #dc2626;padding:16px;margin:20px 0;border-radius:4px;">
          <p style="margin:0 0 8px 0;font-size:20px;font-weight:bold;color:#991b1b;">{{placa}}</p>
          <p style="margin:0 0 8px 0;"><strong>Modelo:</strong> {{modelo}}</p>
          <p style="margin:0 0 8px 0;"><strong>SOAT vence:</strong> {{fecha_vencimiento}}</p>
          <p style="margin:0;"><strong>Días restantes:</strong> <span style="font-weight:bold;color:#dc2626;">{{dias_restantes}}</span></p>
        </div>
        <p><strong>⚠️ Acciones inmediatas:</strong></p>
        <ul>
          <li>Renová el SOAT antes del vencimiento</li>
          <li>Cargá el nuevo comprobante en el sistema</li>
          <li>Suspendé las clases asignadas si no llegás a renovar a tiempo</li>
        </ul>
        <p style="text-align:center;margin:24px 0;">
          <a href="{{enlace_vehiculo}}" style="background:#dc2626;color:#fff;padding:12px 28px;text-decoration:none;border-radius:4px;font-weight:bold;display:inline-block;">Ver vehículo</a>
        </p>
        <hr style="border:0;border-top:1px solid #e0e0e0;margin:24px 0;">
        <p style="font-size:12px;color:#999;text-align:center;margin:0;">
          {{escuela}} - Alertas automáticas de documentación de flota.
        </p>
      </div>
    </div>',
    '["nombre", "placa", "modelo", "fecha_vencimiento", "dias_restantes", "enlace_vehiculo", "escuela"]'::jsonb,
    TRUE,
    'system'
),

-- 5. Notificación de nueva clase al instructor
(
    'NUEVA_CLASE_INSTRUCTOR',
    'Nueva clase asignada - {{fecha}} {{hora}}',
    '<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:20px;color:#333;">
      <div style="background:#059669;color:#fff;padding:20px;border-radius:6px 6px 0 0;">
        <h2 style="margin:0;font-size:20px;">📅 Nueva Clase Asignada</h2>
      </div>
      <div style="background:#fff;padding:24px;border:1px solid #e0e0e0;border-top:none;border-radius:0 0 6px 6px;">
        <p style="font-size:16px;">Hola <strong>{{nombre}}</strong>,</p>
        <p>Tenés una nueva clase asignada:</p>
        <table style="width:100%;background:#f0fdf4;border:1px solid #86efac;border-radius:6px;margin:20px 0;">
          <tr>
            <td style="padding:16px;">
              <p style="margin:0 0 8px 0;color:#166534;font-size:12px;text-transform:uppercase;font-weight:bold;">Fecha y hora</p>
              <p style="margin:0;font-size:18px;font-weight:bold;">{{fecha}} · {{hora}}</p>
            </td>
          </tr>
          <tr>
            <td style="padding:16px;border-top:1px solid #86efac;">
              <p style="margin:0 0 8px 0;color:#166534;font-size:12px;text-transform:uppercase;font-weight:bold;">Estudiante</p>
              <p style="margin:0;">{{estudianteNombre}} ({{cedula}})</p>
              <p style="margin:4px 0 0 0;font-size:13px;color:#666;">Categoría: {{categoria}}</p>
            </td>
          </tr>
          <tr>
            <td style="padding:16px;border-top:1px solid #86efac;">
              <p style="margin:0 0 8px 0;color:#166534;font-size:12px;text-transform:uppercase;font-weight:bold;">Vehículo</p>
              <p style="margin:0;">{{vehiculo}} - {{placa}}</p>
            </td>
          </tr>
        </table>
        <p style="font-size:13px;color:#666;"><strong>Recordá:</strong> registrar el km inicial al iniciar la clase y el km final al terminarla.</p>
        <p style="text-align:center;margin:24px 0;">
          <a href="{{enlace_clase}}" style="background:#059669;color:#fff;padding:12px 28px;text-decoration:none;border-radius:4px;font-weight:bold;display:inline-block;">Ver detalle de la clase</a>
        </p>
        <hr style="border:0;border-top:1px solid #e0e0e0;margin:24px 0;">
        <p style="font-size:12px;color:#999;text-align:center;margin:0;">
          {{escuela}} - Sistema de Gestión de Clases.
        </p>
      </div>
    </div>',
    '["nombre", "fecha", "hora", "estudianteNombre", "cedula", "categoria", "vehiculo", "placa", "enlace_clase", "escuela"]'::jsonb,
    TRUE,
    'system'
)
ON CONFLICT (codigo) DO NOTHING;
