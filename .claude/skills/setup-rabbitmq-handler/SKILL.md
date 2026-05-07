---
name: setup-rabbitmq-handler
description: Generate RabbitMQ event publisher and consumer (subscriber) for Spring AMQP, including queue/exchange/binding declarations, idempotent consumer pattern, dead-letter queue configuration, retry logic, and message schema. Use when adding async event-driven communication between microservices.
---

# Setup RabbitMQ Handler Skill

Creates the full Spring AMQP setup for publishing or consuming events.

## Inputs Needed

Ask the user for:
1. **Direction**: publisher / consumer / both
2. **Event name**: PascalCase past-tense (e.g., `EstudianteMatriculadoEvent`)
3. **Routing key**: format `<context>.<entity>.<action>` (e.g., `estudiantes.estudiante.matriculado`)
4. **Source service**: which microservice publishes (e.g., `ms-estudiantes`)
5. **Consumer services**: which microservice(s) consume
6. **Schema fields**: data payload structure

## Topology

- **Exchange**: `proyecto.events` (topic)
- **Routing key**: `<context>.<entity>.<action>`
- **Queue**: `<consumer-service>.<routing-key>`
- **DLQ**: `<consumer-service>.<routing-key>.dlq`

## Templates

### 1. Event Schema (Java record)

```java
// In domain/event package
package com.kynsoft.<context>.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Event published when a student is enrolled in the system.
 * 
 * Routing key: estudiantes.estudiante.matriculado
 * Consumers: ms-cobros, ms-notificaciones
 */
public record EstudianteMatriculadoEvent(
    UUID messageId,
    String messageType,
    OffsetDateTime occurredAt,
    String version,
    String source,
    String correlationId,
    String causationId,
    String tenantId,
    Data data
) {
    public record Data(
        Long estudianteId,
        String cedula,
        String nombres,
        String apellidos,
        String email,
        String tipoLicencia,
        Long planPagoId,
        String matriculaCodigo
    ) {}
    
    public static EstudianteMatriculadoEvent of(Data data, String correlationId, String tenantId) {
        return new EstudianteMatriculadoEvent(
            UUID.randomUUID(),
            "EstudianteMatriculadoEvent",
            OffsetDateTime.now(),
            "1.0",
            "ms-estudiantes",
            correlationId,
            null,
            tenantId,
            data
        );
    }
}
```

### 2. RabbitMQ Configuration (in producer service)

```java
// In infrastructure/messaging/config
package com.kynsoft.<context>.infrastructure.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQProducerConfig {

    public static final String EVENTS_EXCHANGE = "proyecto.events";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EVENTS_EXCHANGE)
            .durable(true)
            .build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter converter
    ) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        
        template.setConfirmCallback((data, ack, cause) -> {
            if (!ack) {
                log.error("Message not confirmed: id={}, cause={}", 
                    data != null ? data.getId() : "unknown", cause);
            }
        });
        
        template.setReturnsCallback(returned -> 
            log.error("Message returned (no queue bound): {}", returned));
        
        return template;
    }
}
```

### 3. Event Publisher

```java
package com.kynsoft.<context>.infrastructure.messaging;

import com.kynsoft.<context>.domain.event.<EventName>;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class <Entity>EventPublisher {

    private static final String EXCHANGE = "proyecto.events";
    
    private final RabbitTemplate rabbitTemplate;

    public void publishEnrolled(<EventName> event) {
        var routingKey = "<routing-key>";  // e.g., "estudiantes.estudiante.matriculado"
        
        try {
            rabbitTemplate.convertAndSend(
                EXCHANGE,
                routingKey,
                event,
                new CorrelationData(event.messageId().toString())
            );
            log.info("Published {}: messageId={}, routingKey={}", 
                event.messageType(), event.messageId(), routingKey);
        } catch (AmqpException e) {
            log.error("Failed to publish event: {}", event.messageId(), e);
            throw e;
        }
    }
}
```

### 4. Consumer Configuration (in consumer service)

```java
package com.kynsoft.<context>.infrastructure.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    public static final String EVENTS_EXCHANGE = "proyecto.events";
    public static final String DLX_EXCHANGE = "proyecto.dlx";
    
    public static final String STUDENT_ENROLLED_QUEUE = 
        "ms-cobros.estudiantes.estudiante.matriculado";
    public static final String STUDENT_ENROLLED_DLQ = 
        STUDENT_ENROLLED_QUEUE + ".dlq";
    public static final String STUDENT_ENROLLED_ROUTING_KEY = 
        "estudiantes.estudiante.matriculado";

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EVENTS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE).durable(true).build();
    }

    // Main queue with DLX configured
    @Bean
    public Queue studentEnrolledQueue() {
        return QueueBuilder.durable(STUDENT_ENROLLED_QUEUE)
            .deadLetterExchange(DLX_EXCHANGE)
            .deadLetterRoutingKey(STUDENT_ENROLLED_DLQ)
            .ttl(86400000)  // 24h
            .build();
    }

    // Dead-letter queue
    @Bean
    public Queue studentEnrolledDlq() {
        return QueueBuilder.durable(STUDENT_ENROLLED_DLQ).build();
    }

    // Bind main queue to events exchange
    @Bean
    public Binding studentEnrolledBinding() {
        return BindingBuilder
            .bind(studentEnrolledQueue())
            .to(eventsExchange())
            .with(STUDENT_ENROLLED_ROUTING_KEY);
    }

    // Bind DLQ to DLX exchange
    @Bean
    public Binding studentEnrolledDlqBinding() {
        return BindingBuilder
            .bind(studentEnrolledDlq())
            .to(dlxExchange())
            .with(STUDENT_ENROLLED_DLQ);
    }
}
```

### 5. Event Handler/Consumer

```java
package com.kynsoft.<context>.infrastructure.messaging;

import com.kynsoft.<context>.application.service.AccountService;
import com.kynsoft.<context>.domain.event.EstudianteMatriculadoEvent;
import com.kynsoft.<context>.infrastructure.persistence.entity.ProcessedMessage;
import com.kynsoft.<context>.infrastructure.persistence.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentEnrolledHandler {

    private final AccountService accountService;
    private final ProcessedMessageRepository processedRepo;

    @RabbitListener(queues = "ms-cobros.estudiantes.estudiante.matriculado")
    @Transactional
    public void handle(EstudianteMatriculadoEvent event) {
        // Idempotency check
        if (processedRepo.existsByMessageId(event.messageId())) {
            log.warn("Duplicate event ignored: {}", event.messageId());
            return;
        }

        try {
            // Set MDC for distributed tracing
            MDC.put("correlationId", event.correlationId());
            MDC.put("messageId", event.messageId().toString());
            MDC.put("messageType", event.messageType());

            log.info("Processing event: studentId={}, planId={}",
                event.data().estudianteId(), event.data().planPagoId());

            // Idempotent business logic
            accountService.createAccountFor(
                event.data().estudianteId(),
                event.data().planPagoId()
            );

            // Mark as processed (in same transaction)
            processedRepo.save(ProcessedMessage.builder()
                .messageId(event.messageId())
                .messageType(event.messageType())
                .processedAt(OffsetDateTime.now())
                .build());

            log.info("Successfully processed event: {}", event.messageId());

        } catch (BusinessException e) {
            // Permanent failure → DLQ
            log.error("Business error processing event {}: {}", 
                event.messageId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException("Business error", e);
            
        } catch (Exception e) {
            // Transient failure → retry
            log.error("Transient error processing event {}, will retry: {}", 
                event.messageId(), e.getMessage());
            throw e;
            
        } finally {
            MDC.clear();
        }
    }
}
```

### 6. Application.yml Config

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    template:
      mandatory: true
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 2s
          max-attempts: 5
          max-interval: 30s
          multiplier: 2
        default-requeue-rejected: false
        acknowledge-mode: auto
        prefetch: 10
```

### 7. ProcessedMessage Entity (idempotency support)

```java
@Entity
@Table(name = "processed_messages", indexes = {
    @Index(name = "idx_processed_at", columnList = "processed_at")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProcessedMessage {

    @Id
    @Column(name = "message_id", nullable = false)
    private UUID messageId;

    @Column(name = "message_type", nullable = false, length = 100)
    private String messageType;

    @Column(name = "processed_at", nullable = false)
    private OffsetDateTime processedAt;
}
```

Migration:
```sql
CREATE TABLE processed_messages (
    message_id UUID PRIMARY KEY,
    message_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_processed_at ON processed_messages(processed_at);

-- Cleanup old entries periodically
-- Run this as a scheduled job:
-- DELETE FROM processed_messages WHERE processed_at < NOW() - INTERVAL '30 days';
```

## Workflow

1. **Define** the event schema (record class)
2. **Decide** routing key + queue name
3. **Configure** producer (exchange, RabbitTemplate)
4. **Configure** consumer (queue, DLQ, bindings)
5. **Implement** publisher in the producing service
6. **Implement** handler in the consuming service
7. **Add** idempotency via ProcessedMessage table
8. **Test** with embedded RabbitMQ or Testcontainers
9. **Update** event registry: `docs/messaging/event-registry.md`
10. **Verify** message flow:
    - Publish event from producer
    - Check RabbitMQ Management UI: http://localhost:15672
    - Verify queue receives message
    - Verify consumer processes message
    - Verify ProcessedMessage entry created

## Quality Checklist

- [ ] Event schema includes all envelope fields (messageId, messageType, occurredAt, version, source, correlationId, tenantId)
- [ ] Routing key follows `<context>.<entity>.<action>` format
- [ ] Queue name follows `<consumer-service>.<routing-key>` format
- [ ] DLQ configured for the queue
- [ ] Consumer is idempotent (checks ProcessedMessage)
- [ ] Consumer in @Transactional (DB write + business logic atomic)
- [ ] Permanent vs transient errors distinguished
- [ ] MDC populated for tracing
- [ ] Logs at INFO for successful processing
- [ ] Tested with broker (not just unit tests)
- [ ] Event registered in documentation

## Notes

- Always check ProcessedMessage at start of handler (prevents duplicate processing)
- Use `AmqpRejectAndDontRequeueException` for permanent errors → goes to DLQ
- Use regular `Exception` for transient errors → retried with backoff
- Store the message_id INSIDE the same transaction as business logic
- Cleanup old ProcessedMessage entries (>30 days) periodically
- For critical paths, use Outbox Pattern (see `messaging-engineer` agent)
- Don't include sensitive data in events (passwords, full PII) — use IDs and let consumers fetch
