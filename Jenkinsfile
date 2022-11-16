
pipeline {

    agent any

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
        APPLICATION_VERSION = "1.0.0"
        MAVEN_PATH = 'C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2022.2.3\\plugins\\maven\\lib\\maven3\\bin\\mvn'
    }

    stages {

        /*stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B -V -U -e -DskipTests package'
                    }
                    else {
                        bat "\"${env.MAVEN_PATH}\" -B -V -U -e -DskipTests package"
                    }
                    reportBuild(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin",
                        artifactFileName: "${env.WORKSPACE}/target/plugin-devops-portal.hpi",
                        dependenciesToUpdate: 0
                    )
                }
            }
        }*/

        stage('Test') {
            steps {
                script {
                    /*if (isUnix()) {
                        sh 'mvn -B test'
                    }
                    else {
                        bat "\"${env.MAVEN_PATH}\" -B test"
                    }*/
                    def passed = 0
                    def failed = 0
                    def ignored = 0
                    def filter = ~/.*\.xml$/
                    new File('target/surefire-reports').traverse(type: groovy.io.FileType.FILES, nameFilter: filter) { file ->
                        file.eachLine { line ->
                            if (line.startsWith('<testsuite ')){
                                println line
                            }
                        }
                    }

                }
            }
        }

    }

}