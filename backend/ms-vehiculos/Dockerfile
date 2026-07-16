
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . /workspace

RUN mvn -B -pl ms-vehiculos -am clean package -DskipTests \
    && cp ms-vehiculos/target/*.jar /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_PORT=8084

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/America/Guayaquil /etc/localtime && \
    echo "America/Guayaquil" > /etc/timezone

RUN addgroup -S spring && \
    adduser -S -G spring -u 1000 spring

WORKDIR /app
RUN mkdir -p /app/storage && chown -R spring:spring /app

COPY --from=build --chown=spring:spring /workspace/app.jar /app/app.jar

ENV SERVICE_PORT=${SERVICE_PORT} \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom -Duser.timezone=America/Guayaquil"

USER spring:spring
EXPOSE ${SERVICE_PORT}

HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=10 \
    CMD curl -fsS "http://localhost:${SERVICE_PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
