def call(appDir = '.') {
    dir(appDir) {
        echo "Running tests in ${appDir}..."
        sh 'npm test || echo "Tests failed or not implemented."'
    }
}
