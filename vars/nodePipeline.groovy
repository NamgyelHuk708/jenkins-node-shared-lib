def call(Map config = [:]) {
    // Default configuration values
    def defaults = [
        appDir: '.',               // Default to root directory
        dockerImage: '',           // Required - no default
        dockerCredentials: 'dockerhub-creds', // Jenkins credentials ID
        testScript: 'npm test',    // Default test command
        buildScript: 'npm run build' // Default build command
    ]
    
    // Merge user config with defaults
    config = defaults + config
    
    // Validate required parameters
    if (!config.dockerImage) {
        error('dockerImage parameter is required')
    }
    
    pipeline {
        agent any
        
        stages {
            stage('Install Dependencies') {
                steps {
                    script {
                        dir(config.appDir) {
                            echo "Installing dependencies in ${config.appDir}"
                            sh 'npm ci' // Clean install for CI environments
                        }
                    }
                }
            }
            
            stage('Run Tests') {
                steps {
                    script {
                        dir(config.appDir) {
                            echo "Running tests"
                            sh config.testScript
                        }
                    }
                }
            }
            
            stage('Build Docker Image') {
                steps {
                    script {
                        dir(config.appDir) {
                            echo "Building Docker image"
                            sh "docker build -t ${config.dockerImage} ."
                        }
                    }
                }
            }
            
            stage('Push to DockerHub') {
                steps {
                    script {
                        echo "Pushing to DockerHub"
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
                echo "Cleaning up Docker credentials"
                sh 'docker logout'
            }
        }
    }
}