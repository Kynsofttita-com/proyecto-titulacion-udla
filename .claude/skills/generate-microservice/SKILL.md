---
name: generate-microservice
description: Generate a complete Spring Boot 3 microservice boilerplate with proper hexagonal package structure, pom.xml, Application class, application.yml, Dockerfile, and Spring Cloud integration (Eureka client, Config client). Use when starting a new microservice for the driving school project.
---

# Generate Microservice Skill

Bootstraps a new Spring Boot 3 microservice with project conventions.

## Prerequisites

Ask the user for:
1. **Service name**: kebab-case (e.g., `ms-mantenimiento`)
2. **Port**: unique TCP port (e.g., 8090)
3. **Database name**: PostgreSQL database (e.g., `mantenimiento_db`)
4. **Description**: one-line purpose

## Output Structure

```
microservices/<service-name>/
├── pom.xml
├── Dockerfile
├── README.md
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/kynsoft/<package>/
    │   │   ├── Application.java
    │   │   ├── application/
    │   │   │   ├── service/
    │   │   │   ├── usecase/
    │   │   │   └── port/
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   ├── exception/
    │   │   │   └── event/
    │   │   ├── infrastructure/
    │   │   │   ├── persistence/
    │   │   │   ├── messaging/
    │   │   │   ├── client/
    │   │   │   └── config/
    │   │   └── interfaces/
    │   │       ├── rest/
    │   │       ├── dto/
    │   │       └── mapper/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    │           └── V0001__init.sql
    └── test/
        ├── java/com/kynsoft/<package>/
        │   ├── ApplicationTests.java
        │   └── ...
        └── resources/
            └── application-test.yml
```

## Templates

### pom.xml Dependencies (always include)

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-actuator
- spring-cloud-starter-netflix-eureka-client
- spring-cloud-starter-config
- spring-cloud-starter-openfeign
- spring-cloud-starter-circuitbreaker-resilience4j
- spring-boot-starter-amqp
- springdoc-openapi-starter-webmvc-ui
- postgresql (runtime)
- flyway-core
- lombok (provided)
- mapstruct
- spring-boot-starter-test
- testcontainers-postgresql (test)

Use Spring Boot 3.2+, Spring Cloud 2023.0+, Java 21.

### application.yml

```yaml
spring:
  application:
    name: <service-name>
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/<db-name>}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:dev_password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        jdbc.time_zone: UTC
  flyway:
    enabled: true
    baseline-on-migrate: true
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER:guest}
    password: ${RABBITMQ_PASS:guest}

server:
  port: <port>
  error:
    include-message: always
    include-binding-errors: always

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when-authorized

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId},%X{spanId}] %-5level %logger{36} - %msg%n"
  level:
    com.kynsoft: INFO
    org.springframework.web: INFO
```

### Dockerfile

Use multi-stage build (Eclipse Temurin 21 JDK → JRE), non-root user, health check, JVM container support flags.

### README.md

Auto-generate with: service name, responsibility, tech stack, quick start commands, configuration, API link, key endpoints.

## Workflow

1. **Ask** the user for required inputs (above)
2. **Validate** port not already in use by another service
3. **Read** an existing microservice as template (e.g., `microservices/ms-auth`)
4. **Generate** all files following project conventions in CLAUDE.md
5. **Run** `mvn clean compile` from the new service to verify compilation
6. **Update** root `docker-compose.yml` to include the new service
7. **Update** `api-gateway` route configuration if it should be exposed
8. **Report** what was created and next steps

## Verification

After generation, verify:
- [ ] `mvn clean compile` succeeds
- [ ] Service starts without errors (`mvn spring-boot:run`)
- [ ] Service registers with Eureka
- [ ] Health endpoint responds: `curl http://localhost:<port>/actuator/health`
- [ ] Swagger UI accessible: `http://localhost:<port>/swagger-ui.html`
- [ ] Flyway runs migration `V0001__init.sql`

## Notes

- Match existing service naming exactly (don't deviate from `ms-<context>` convention)
- Java package: `com.kynsoft.<context>` (e.g., `com.kynsoft.mantenimiento`)
- Database name: `<context>_db` (e.g., `mantenimiento_db`)
- Refer the user to `documentation-writer` agent to create the service's README and ADR
