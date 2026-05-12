package com.escuela.estudiantes.service;

import com.escuela.common.events.estudiantes.EstudianteActualizadoEvent;
import com.escuela.common.events.estudiantes.EstudianteCreadoEvent;
import com.escuela.common.events.estudiantes.EstudianteEliminadoEvent;
import com.escuela.common.events.estudiantes.EstudianteMatriculadoEvent;
import com.escuela.estudiantes.config.RabbitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EstudianteEventDispatcher")
class EstudianteEventDispatcherTest {

    @Mock private ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    @Mock private RabbitTemplate rabbitTemplate;

    private EstudianteEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new EstudianteEventDispatcher(rabbitTemplateProvider, "ms-estudiantes");
    }

    @Test
    @DisplayName("publishCreado envia al exchange con routing key correcta")
    void publishCreado() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        EstudianteCreadoEvent event = EstudianteCreadoEvent.builder()
                .estudianteId(1L)
                .cedula("1710034065")
                .email("h@t.com")
                .nombreCompleto("Hernan Jurado")
                .estado("PRE_MATRICULADO")
                .build();

        dispatcher.publishCreado(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_NAME),
                eq(EstudianteCreadoEvent.ROUTING_KEY),
                eq((Object) event));
    }

    @Test
    @DisplayName("publishActualizado usa routing key estudiantes.actualizado")
    void publishActualizado() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        EstudianteActualizadoEvent event = EstudianteActualizadoEvent.builder()
                .estudianteId(1L).cedula("1710034065").email("h@t.com")
                .nombreCompleto("Hernan Jurado").estado("ACTIVO").build();

        dispatcher.publishActualizado(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_NAME),
                eq(EstudianteActualizadoEvent.ROUTING_KEY),
                eq((Object) event));
    }

    @Test
    @DisplayName("publishEliminado usa routing key estudiantes.eliminado")
    void publishEliminado() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        EstudianteEliminadoEvent event = EstudianteEliminadoEvent.builder()
                .estudianteId(1L).cedula("1710034065").build();

        dispatcher.publishEliminado(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_NAME),
                eq(EstudianteEliminadoEvent.ROUTING_KEY),
                eq((Object) event));
    }

    @Test
    @DisplayName("publishMatriculado usa routing key estudiantes.matriculado")
    void publishMatriculado() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(rabbitTemplate);
        EstudianteMatriculadoEvent event = EstudianteMatriculadoEvent.builder()
                .estudianteId(1L).cedula("1710034065").email("h@t.com")
                .nombreCompleto("Hernan Jurado")
                .fechaMatricula(LocalDate.of(2026, 5, 12))
                .build();

        dispatcher.publishMatriculado(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE_NAME),
                eq(EstudianteMatriculadoEvent.ROUTING_KEY),
                eq((Object) event));
    }

    @Test
    @DisplayName("Sin RabbitTemplate disponible (perfil test) -> publish es no-op")
    void publishSinRabbit() {
        when(rabbitTemplateProvider.getIfAvailable()).thenReturn(null);

        dispatcher.publishCreado(EstudianteCreadoEvent.builder()
                .estudianteId(1L).cedula("1710034065").build());

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }
}
