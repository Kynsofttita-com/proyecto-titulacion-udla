package com.escuela.notificaciones.service;

import com.escuela.notificaciones.config.NotificacionesProperties;
import com.escuela.notificaciones.entity.LogEnvioEmail;
import com.escuela.notificaciones.repository.LogEnvioEmailRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Servicio de envio de emails con templates Thymeleaf.
 *
 * <p>Si {@link NotificacionesProperties#isMailEnabled()} es {@code false}
 * o {@link JavaMailSender} no esta disponible, los envios se persisten en
 * {@code log_envios_email} con estado {@code PENDIENTE} pero NO se envian
 * (modo "dry-run" para dev sin credenciales SMTP).</p>
 *
 * <p>El estado del envio se persiste SIEMPRE (auditoria):
 * {@code ENVIADO} si se envio OK, {@code FALLIDO} si fallo, {@code PENDIENTE}
 * si mail-enabled=false.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final TemplateEngine templateEngine;
    private final LogEnvioEmailRepository logRepository;
    private final NotificacionesProperties properties;

    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                        TemplateEngine templateEngine,
                        LogEnvioEmailRepository logRepository,
                        NotificacionesProperties properties) {
        this.mailSenderProvider = mailSenderProvider;
        this.templateEngine = templateEngine;
        this.logRepository = logRepository;
        this.properties = properties;
    }

    /**
     * Renderiza el template y envia (o registra como pendiente si mail-enabled=false).
     *
     * @param destinatario   email del destinatario
     * @param asunto         subject del email
     * @param plantillaCodigo codigo logico de la plantilla (ej: "RECUPERAR_PASSWORD")
     *                       — solo se usa en log_envios_email para auditoria
     * @param templateName   nombre del archivo Thymeleaf (sin extension), ej "emails/password-reset"
     * @param variables      variables a interpolar en el template
     */
    public void enviar(String destinatario, String asunto, String plantillaCodigo,
                       String templateName, Map<String, Object> variables) {

        // Renderizar template (siempre, incluso si mail-enabled=false, para
        // detectar errores en templates en dev)
        Context ctx = new Context();
        variables.forEach(ctx::setVariable);
        String html = templateEngine.process(templateName, ctx);

        LogEnvioEmail logEntry = LogEnvioEmail.builder()
                .destinatario(destinatario)
                .asunto(asunto)
                .plantillaCodigo(plantillaCodigo)
                .estado("PENDIENTE")
                .intentos((short) 0)
                .build();

        if (!properties.isMailEnabled()) {
            log.info("mail-enabled=false: NO se envia email a {} (asunto={}); persistido como PENDIENTE",
                    destinatario, asunto);
            logRepository.save(logEntry);
            return;
        }

        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("JavaMailSender no disponible (probablemente test); persistido como PENDIENTE");
            logRepository.save(logEntry);
            return;
        }

        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(properties.getFromEmail(), properties.getFromName());
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);

            sender.send(msg);

            logEntry.setEstado("ENVIADO");
            logEntry.setEnviadoEn(LocalDateTime.now());
            logEntry.setIntentos((short) 1);
            logRepository.save(logEntry);

            log.info("Email enviado a {} (asunto={})", destinatario, asunto);

        } catch (Exception e) {
            // Capturamos cualquier excepcion (MessagingException, UnsupportedEncodingException,
            // MailException de Spring, etc.) para registrar el log de fallo y luego relanzar
            // como EmailEnvioException — Spring AMQP la propaga y aplica retry/DLQ.
            logEntry.setEstado("FALLIDO");
            logEntry.setIntentos((short) 1);
            logEntry.setErrorMensaje(e.getMessage());
            logRepository.save(logEntry);

            log.error("Fallo enviando email a {}: {}", destinatario, e.getMessage());
            throw new EmailEnvioException("Error enviando email a " + destinatario, e);
        }
    }

    public static class EmailEnvioException extends RuntimeException {
        public EmailEnvioException(String msg, Throwable cause) { super(msg, cause); }
    }
}
