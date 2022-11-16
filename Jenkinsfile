/*
 See the documentation for more options:
 https://github.com/jenkins-infra/pipeline-library/
*/


pipeline {

    agent any

    options {
        disableConcurrentBuilds()
        buildDiscarder(
            logRotator(numToKeepStr: '3')
        )
    }

    tools {
        jdk 'openjdk-17'
        maven '3.8.3'
    }

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