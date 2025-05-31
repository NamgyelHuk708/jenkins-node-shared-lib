def call(Map config = [:]) {
    // Set defaults
    def defaults = [
        appDir: '.',
        dockerImage: '',
        testCommand: 'npm test',
        testReportPath: 'junit.xml',
        dockerCredentials: 'dockerhub-creds'
    ]
    config = defaults + config

    if (!config.dockerImage) {
        error('dockerImage parameter is required')
    }

    pipeline {
        agent any
        
            stage('Install Dependencies') {
                steps {
                    script {
                        dir(env.APP_DIR) {
                            // Remove existing node_modules if any
                            sh 'rm -rf node_modules package-lock.json'
                            
                            // Verify Node.js version being used
                            sh 'node --version'
                            sh 'npm --version'
                            
                            // Fresh install
                            sh 'npm ci'
                        }
                    }
                }
            }
            stage('Run Tests') {
                steps {
                    script {
                        dir(config.appDir) {
                            sh 'rm -f junit.xml || true'
                            sh config.testCommand
                        }
                    }
                }
                post {
                    always {
                        junit "${config.appDir}/${config.testReportPath}"
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
                            credentialsId: config.dockerCredentials,
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
                cleanWs()
            }
        }
    }
}