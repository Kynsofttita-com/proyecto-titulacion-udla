package com.escuela.notificaciones.service;

import com.escuela.notificaciones.config.NotificacionesProperties;
import com.escuela.notificaciones.entity.LogEnvioEmail;
import com.escuela.notificaciones.repository.LogEnvioEmailRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock private TemplateEngine templateEngine;
    @Mock private LogEnvioEmailRepository logRepository;

    private NotificacionesProperties properties;
    private EmailService emailService;

    @BeforeEach
    void setup() {
        properties = new NotificacionesProperties();
        properties.setFromEmail("no-reply@escuela.local");
        properties.setFromName("Escuela");
        properties.setFrontendBaseUrl("http://localhost:5173");
        emailService = new EmailService(mailSenderProvider, templateEngine, logRepository, properties);
    }

    @Test
    @DisplayName("mail-enabled=false: no envia, persiste como PENDIENTE")
    void mailDeshabilitado() {
        properties.setMailEnabled(false);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>...</html>");

        emailService.enviar("a@b.com", "Asunto", "CODIGO", "emails/x", Map.of());

        ArgumentCaptor<LogEnvioEmail> captor = ArgumentCaptor.forClass(LogEnvioEmail.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("PENDIENTE");
        assertThat(captor.getValue().getDestinatario()).isEqualTo("a@b.com");
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("mail-enabled=true sin JavaMailSender: persiste como PENDIENTE")
    void mailHabilitadoPeroSinSender() {
        properties.setMailEnabled(true);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>...</html>");
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);

        emailService.enviar("a@b.com", "Asunto", "CODIGO", "emails/x", Map.of());

        ArgumentCaptor<LogEnvioEmail> captor = ArgumentCaptor.forClass(LogEnvioEmail.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Envio exitoso: persiste como ENVIADO con timestamp")
    void envioExitoso() {
        properties.setMailEnabled(true);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>...</html>");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        MimeMessage mimeMsg = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);

        emailService.enviar("a@b.com", "Asunto", "CODIGO", "emails/x",
                Map.of("nombre", "Ana"));

        ArgumentCaptor<LogEnvioEmail> captor = ArgumentCaptor.forClass(LogEnvioEmail.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("ENVIADO");
        assertThat(captor.getValue().getEnviadoEn()).isNotNull();
        assertThat(captor.getValue().getIntentos()).isEqualTo((short) 1);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Envio fallido: persiste como FALLIDO y relanza excepcion (para retry/DLQ)")
    void envioFallido() {
        properties.setMailEnabled(true);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>...</html>");
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        MimeMessage mimeMsg = new MimeMessage((jakarta.mail.Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMsg);
        org.mockito.Mockito.doThrow(new MailSendException("SMTP down"))
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() ->
                emailService.enviar("a@b.com", "x", "CODIGO", "emails/x", Map.of()))
                .isInstanceOf(EmailService.EmailEnvioException.class);

        ArgumentCaptor<LogEnvioEmail> captor = ArgumentCaptor.forClass(LogEnvioEmail.class);
        verify(logRepository).save(captor.capture());
        assertThat(captor.getValue().getEstado()).isEqualTo("FALLIDO");
        assertThat(captor.getValue().getErrorMensaje()).contains("SMTP down");
    }

    @Test
    @DisplayName("Template engine se invoca con el nombre y variables correctas")
    void templateEngineInvocado() {
        properties.setMailEnabled(false);
        when(templateEngine.process(anyString(), any(IContext.class))).thenReturn("<html>...</html>");

        emailService.enviar("a@b.com", "Asunto", "CODIGO", "emails/password-reset",
                Map.of("nombre", "Ana", "link", "http://x"));

        verify(templateEngine).process(org.mockito.ArgumentMatchers.eq("emails/password-reset"),
                any(IContext.class));
    }
}
