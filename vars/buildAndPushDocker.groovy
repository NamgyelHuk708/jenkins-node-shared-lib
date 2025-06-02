def call(appDir = '.', imageName, dockerhubCredsId) {
    dir(appDir) {
        echo "Building Docker image ${imageName}..."
        sh "docker build -t ${imageName} ."

        echo "Logging in and pushing to DockerHub..."
        withCredentials([usernamePassword(credentialsId: dockerhubCredsId, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
            sh "docker push ${imageName}"
        }
    }
}
