def call(Map config = [:]) {
    pipeline {
        agent any
        environment {
            DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
            IMAGE_NAME = "${config.imageName ?: 'my-node-app'}"
        }
        stages {
            stage('Install Dependencies') {
                steps {
                    sh 'npm install'
                }
            }
            stage('Run Tests') {
                steps {
                    sh 'npm test'
                }
            }
            stage('Build Docker Image') {
                steps {
                    script {
                        dockerImage = docker.build("${IMAGE_NAME}:${env.BUILD_NUMBER}")
                    }
                }
            }
            stage('Push to DockerHub') {
                steps {
                    script {
                        docker.withRegistry('', DOCKERHUB_CREDENTIALS) {
                            dockerImage.push()
                        }
                    }
                }
            }
        }
    }
}
