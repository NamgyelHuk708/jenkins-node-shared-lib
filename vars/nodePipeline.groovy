def call(Map config = [:]) {
    // Set defaults
    def defaults = [
        appDir: '.',
        dockerImage: '',
        testCommand: 'npm test'
    ]
    config = defaults + config

    if (!config.dockerImage) {
        error "dockerImage parameter is required"
    }

    pipeline {
        agent any
        
        stages {
            stage('Install Dependencies') {
                steps {
                    script {
                        dir(config.appDir) {
                            sh 'npm ci'
                        }
                    }
                }
            }
            
            stage('Run Tests') {
                steps {
                    script {
                        dir(config.appDir) {
                            sh config.testCommand
                        }
                    }
                }
            }
            
            stage('Build Docker Image') {
                steps {
                    script {
                        dir(config.appDir) {
                            sh "docker build -t ${config.dockerImage} ."
                        }
                    }
                }
            }
            
            stage('Push to DockerHub') {
                steps {
                    script {
                        withCredentials([usernamePassword(
                            credentialsId: 'dockerhub-creds',
                            usernameVariable: 'DOCKER_USER',
                            passwordVariable: 'DOCKER_PASS'
                        )]) {
                            sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
                            sh "docker push ${config.dockerImage}"
                        }
                    }
                }
            }
        }
        
        post {
            always {
                sh 'docker logout'
            }
        }
    }
}