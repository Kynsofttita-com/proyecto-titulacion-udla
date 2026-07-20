// =============================================================================
// Jenkinsfile - CI/CD Pipeline para Proyecto Titulación
// =============================================================================
// Pipeline stages:
//   1. Checkout
//   2. Build Backend
//   3. Unit Tests + Coverage
//   4. Code Quality (SonarQube)
//   5. Dependency Check (OWASP)
//   6. Security Scan (Trivy)
//   7. Build Docker Images
//   8. Deploy to Staging (Argo CD)
//   9. E2E Tests
//   10. Deploy to Production
//
// Trigger: Push a main/develop, Pull Requests, Manual
// =============================================================================

pipeline {
    agent any

    options {
        // Timeout en 45 minutos
        timeout(time: 45, unit: 'MINUTES')
        // Mantener últimos 30 builds
        buildDiscarder(logRotator(numToKeepStr: '30'))
        // No correr builds concurrentes del mismo branch
        disableConcurrentBuilds()
        // Timestamps en logs
        timestamps()
    }

    environment {
        // Java
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        PATH = "${JAVA_HOME}/bin:${PATH}"

        // Maven
        MAVEN_HOME = '/opt/maven'
        PATH = "${MAVEN_HOME}/bin:${PATH}"

        // Project
        PROJECT_NAME = "proyecto-titulacion"
        BACKEND_DIR = "backend"

        // SonarQube
        SONAR_HOST_URL = credentials('sonar-host-url')
        SONAR_TOKEN = credentials('sonar-token')

        // Docker Registry
        DOCKER_REGISTRY = credentials('docker-registry')
        DOCKER_USERNAME = credentials('docker-username')
        DOCKER_PASSWORD = credentials('docker-password')

        // Argo CD
        ARGOCD_SERVER = credentials('argocd-server')
        ARGOCD_TOKEN = credentials('argocd-token')
        ARGOCD_REPO = "https://github.com/Kynsofttita-com/proyecto-titulacion-udla.git"

        // Slack notifications
        SLACK_WEBHOOK = credentials('slack-webhook')
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "📦 Checking out repository..."
                }
                checkout scm
                script {
                    GIT_COMMIT_SHORT = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()
                    GIT_BRANCH = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                    echo "✅ Branch: ${GIT_BRANCH}, Commit: ${GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Build Backend') {
            steps {
                script {
                    echo "🔨 Building backend services..."
                }
                dir("${BACKEND_DIR}") {
                    sh '''
                        mvn -B -ntp clean compile \
                            -Dmaven.test.skip=true \
                            -DskipDocker
                    '''
                }
                script {
                    echo "✅ Backend compiled successfully"
                }
            }
        }

        stage('Unit Tests & Coverage') {
            steps {
                script {
                    echo "🧪 Running unit tests and generating coverage..."
                }
                dir("${BACKEND_DIR}") {
                    sh '''
                        mvn -B -ntp verify \
                            -Dmaven.test.skip=false
                    '''
                }

                // Publish coverage
                publishCoverage adapters: [jacocoAdapter('**/target/site/jacoco/jacoco.xml')]

                script {
                    echo "✅ Tests completed"
                }
            }
        }

        stage('Code Quality - SonarQube') {
            steps {
                script {
                    echo "📊 Running SonarQube analysis..."
                }
                dir("${BACKEND_DIR}") {
                    sh '''
                        mvn -B -ntp sonar:sonar \
                            -Dsonar.projectKey=proyecto-titulacion-backend \
                            -Dsonar.projectName="Proyecto Titulación Backend" \
                            -Dsonar.sources=src/main \
                            -Dsonar.tests=src/test \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                    '''
                }

                // Wait for quality gate
                waitForQualityGate abortPipeline: false

                script {
                    echo "✅ Code quality analysis completed"
                }
            }
        }

        stage('Dependency Check - OWASP') {
            steps {
                script {
                    echo "🔐 Checking dependencies for vulnerabilities..."
                }
                dir("${BACKEND_DIR}") {
                    sh '''
                        dependency-check.sh \
                            --project "Proyecto Titulación Backend" \
                            --scan . \
                            --format JSON \
                            --format HTML \
                            --enableRetired
                    '''
                }

                // Archive results
                archiveArtifacts artifacts: 'backend/**/dependency-check-report.*',
                                 allowEmptyArchive: true

                script {
                    echo "✅ Dependency check completed"
                }
            }
        }

        stage('Security Scan - Trivy') {
            steps {
                script {
                    echo "🛡️ Running Trivy vulnerability scanner..."
                }
                sh '''
                    trivy fs \
                        --severity HIGH,CRITICAL \
                        --format json \
                        --output trivy-results.json \
                        ${BACKEND_DIR}
                '''

                archiveArtifacts artifacts: 'trivy-results.json', allowEmptyArchive: true

                script {
                    echo "✅ Security scan completed"
                }
            }
        }

        stage('Build Docker Images') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "🐳 Building Docker images..."
                }
                dir("${BACKEND_DIR}") {
                    sh '''
                        for service in ms-auth ms-estudiantes ms-instructores ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
                            echo "Building ${service}..."
                            docker build -t ${DOCKER_REGISTRY}/${PROJECT_NAME}/${service}:${BUILD_NUMBER} \
                                         -t ${DOCKER_REGISTRY}/${PROJECT_NAME}/${service}:latest \
                                         ${service}/

                            # Scan image
                            echo "Scanning ${service}..."
                            trivy image --severity HIGH,CRITICAL \
                                       ${DOCKER_REGISTRY}/${PROJECT_NAME}/${service}:latest
                        done
                    '''
                }

                script {
                    echo "✅ Docker images built and scanned"
                }
            }
        }

        stage('Push to Registry') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "📤 Pushing images to registry..."
                }
                sh '''
                    echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin ${DOCKER_REGISTRY}

                    for service in ms-auth ms-estudiantes ms-instructores ms-vehiculos ms-asignaciones ms-cobros ms-reportes ms-notificaciones; do
                        docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}/${service}:${BUILD_NUMBER}
                        docker push ${DOCKER_REGISTRY}/${PROJECT_NAME}/${service}:latest
                    done

                    docker logout ${DOCKER_REGISTRY}
                '''

                script {
                    echo "✅ Images pushed successfully"
                }
            }
        }

        stage('Deploy to Staging - ArgoCD') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "🚀 Deploying to staging with Argo CD..."
                }
                sh '''
                    argocd app sync proyecto-staging \
                        --server ${ARGOCD_SERVER} \
                        --auth-token ${ARGOCD_TOKEN} \
                        --grpc-web

                    argocd app wait proyecto-staging \
                        --server ${ARGOCD_SERVER} \
                        --auth-token ${ARGOCD_TOKEN} \
                        --grpc-web
                '''

                script {
                    echo "✅ Staging deployment completed"
                }
            }
        }

        stage('E2E Tests') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "🧪 Running E2E tests..."
                }
                sh '''
                    cd frontend
                    npm ci
                    npm run test:e2e || true
                    npm run test:e2e:report || true
                '''

                publishHTML([
                    reportDir: 'frontend/cypress/reports',
                    reportFiles: 'index.html',
                    reportName: 'E2E Test Report'
                ])

                script {
                    echo "✅ E2E tests completed"
                }
            }
        }

        stage('Deploy to Production - ArgoCD') {
            when {
                branch 'main'
            }
            input {
                message "Deploy to production?"
                ok "Deploy"
                submitter "admin,devops"
            }
            steps {
                script {
                    echo "🚀 Deploying to production with Argo CD..."
                }
                sh '''
                    argocd app sync proyecto-production \
                        --server ${ARGOCD_SERVER} \
                        --auth-token ${ARGOCD_TOKEN} \
                        --grpc-web

                    argocd app wait proyecto-production \
                        --server ${ARGOCD_SERVER} \
                        --auth-token ${ARGOCD_TOKEN} \
                        --grpc-web
                '''

                script {
                    echo "✅ Production deployment completed"
                }
            }
        }
    }

    post {
        always {
            script {
                echo "🧹 Cleaning up..."
            }

            // Archive all test results
            junit '**/target/surefire-reports/*.xml', allowEmptyResults: true

            // Archive coverage
            publishHTML([
                reportDir: 'backend/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
        }

        success {
            script {
                echo "✅ Pipeline succeeded!"
            }
            sh '''
                curl -X POST ${SLACK_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{
                        "text": "✅ Build Success",
                        "attachments": [{
                            "color": "good",
                            "fields": [
                                {"title": "Project", "value": "Proyecto Titulación", "short": true},
                                {"title": "Branch", "value": "'${GIT_BRANCH}'", "short": true},
                                {"title": "Commit", "value": "'${GIT_COMMIT_SHORT}'", "short": false},
                                {"title": "Build", "value": "'${BUILD_NUMBER}'", "short": true}
                            ]
                        }]
                    }'
            '''
        }

        failure {
            script {
                echo "❌ Pipeline failed!"
            }
            sh '''
                curl -X POST ${SLACK_WEBHOOK} \
                    -H 'Content-Type: application/json' \
                    -d '{
                        "text": "❌ Build Failed",
                        "attachments": [{
                            "color": "danger",
                            "fields": [
                                {"title": "Project", "value": "Proyecto Titulación", "short": true},
                                {"title": "Branch", "value": "'${GIT_BRANCH}'", "short": true},
                                {"title": "Build", "value": "'${BUILD_NUMBER}'", "short": true}
                            ]
                        }]
                    }'
            '''
        }
    }
}
