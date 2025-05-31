// vars/nodeUtils.groovy
def installDependencies(String appDir = '.') {
    dir(appDir) {
        sh 'npm ci'
    }
}

def runTests(String appDir = '.') {
    dir(appDir) {
        sh 'npm test'
    }
}

def buildDockerImage(String appDir = '.', String imageName) {
    if (!imageName) {
        error "Docker image name is required"
    }
    dir(appDir) {
        sh "docker build -t ${imageName} ."
    }
}

def pushToDockerHub(String imageName) {
    withCredentials([usernamePassword(
        credentialsId: 'dockerhub-creds',
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        sh "echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin"
        sh "docker push ${imageName}"
    }
}
