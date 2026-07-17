FROM jenkins/jenkins:lts-jdk21

USER root

# Instalar dependencias necesarias
RUN apt-get update && apt-get install -y \
    docker.io \
    docker-compose \
    git \
    maven \
    && rm -rf /var/lib/apt/lists/*

# Agregar jenkins al grupo docker
RUN usermod -aG docker jenkins

USER jenkins

ENV JAVA_OPTS="-Duser.timezone=America/Guayaquil"
ENV JENKINS_OPTS="--httpPort=8090"
