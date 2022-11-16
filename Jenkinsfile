
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
                    if (isUnix()) {
                        sh 'mvn -B -V -U -e -DskipTests package'
                    }
                    else {
                        bat 'mvn -B -V -U -e -DskipTests package'
                    }
                }
            }
        }

    }

}