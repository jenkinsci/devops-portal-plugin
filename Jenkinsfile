
pipeline {

    agent any

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
        APPLICATION_VERSION = "1.0.0"
    }

    stages {

        stage('Build') {
            steps {
                script {
                    echo "ok"
                    buildPlugin(useContainerAgent: true)
                }
            }
        }

    }

}