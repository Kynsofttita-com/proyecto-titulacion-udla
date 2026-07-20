// =============================================================================
// Jenkinsfile — Pipeline CI para Escuela de Conduccion
// =============================================================================
// Replica lo que hace GitHub Actions (backend-ci, frontend-ci, docker-build)
// pero corriendo en el Jenkins interno de la VM.
//
// Cada stage usa un agent Docker efimero. No hace deploy: solo build +
// tests + coverage + build de imagenes.
//
// Version "full" con SonarQube/Trivy/ArgoCD/Slack: ver Jenkinsfile.full.example
// =============================================================================

pipeline {
    agent any

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        DOCKER_BUILDKIT = '1'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Backend · build + tests') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-21'
                    reuseNode true
                    // Cache local de dependencias para acelerar builds sucesivos.
                    args '-v jenkins-maven-cache:/root/.m2'
                }
            }
            steps {
                dir('backend') {
                    // -T 1C = 1 thread por core; verify = compile + test + jacoco
                    sh 'mvn -B -ntp -T 1C clean verify'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'backend/**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Frontend · build') {
            agent {
                docker {
                    image 'node:20-alpine'
                    reuseNode true
                }
            }
            steps {
                dir('frontend') {
                    sh 'npm ci --prefer-offline --no-audit'
                    sh 'npm run build'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'frontend/dist/**',
                                     allowEmptyArchive: true,
                                     fingerprint: false
                }
            }
        }

        stage('Docker images (build only, no push)') {
            when {
                anyOf {
                    branch 'main'
                    // permite forzar el build en cualquier rama con flag manual
                    expression { return params.BUILD_DOCKER_IMAGES == true }
                }
            }
            steps {
                script {
                    def services = ['ms-auth', 'ms-estudiantes', 'ms-instructores',
                                    'ms-vehiculos', 'ms-asignaciones', 'ms-cobros',
                                    'ms-reportes', 'ms-notificaciones']
                    def tag = "${env.BRANCH_NAME.replaceAll('/', '-')}-${env.BUILD_NUMBER}"
                    services.each { s ->
                        sh "docker build -f backend/${s}/Dockerfile -t escuela/${s}:${tag} backend"
                    }
                    sh "docker build -t escuela/frontend:${tag} frontend"
                }
            }
        }
    }

    post {
        success {
            echo "✅ Build #${env.BUILD_NUMBER} OK — commit ${env.GIT_COMMIT?.take(8)}"
        }
        failure {
            echo "❌ Build #${env.BUILD_NUMBER} FAILED — commit ${env.GIT_COMMIT?.take(8)}"
        }
    }
}
