def call(appDir = '.') {
    dir(appDir) {
        echo "Installing dependencies in ${appDir}..."
        sh 'npm install'
    }
}
