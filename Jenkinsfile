pipeline {

    agent any

    tools {
        maven '3'
    }

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
        APPLICATION_VERSION = "1.0.0"
        MAVEN_PATH = 'C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2022.2.3\\plugins\\maven\\lib\\maven3\\bin\\mvn'
    }

    stages {

        stage('Build') {
            steps {
                script {
                    /*if (isUnix()) {
                        sh 'mvn -B -V -U -e -DskipTests package'
                    }
                    else {
                        bat "\"${env.MAVEN_PATH}\" -B -V -U -e -DskipTests package"
                    }*/
                    reportBuild(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        artifactFileName: "${env.WORKSPACE}/target/plugin-devops-portal.hpi"
                    )
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    /*if (isUnix()) {
                        sh 'mvn -B test'
                    }
                    else {
                        bat "\"${env.MAVEN_PATH}\" -B test"
                    }*/
                    reportUnitTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "other-component",
                        testsPassed: 31,
                        testsFailed: 6,
                        testsIgnored: 2
                    )
                    reportSurefireTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        surefireReportPath: "target/surefire-reports/TEST-InjectedTest.xml"
                    )
                }
            }
        }

        stage('Audit') {
            steps {
                script {
                    // Dependencies analysis
                    reportDependenciesAnalysis(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        manifestFile: "pom.xml",
                        manager: "MAVEN"
                    )
                }
            }
        }

        stage('Publish') {
            steps {
                script {
                    reportImageRelease(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        registryName: "registry.mydomain.com",
                        imageName: env.APPLICATION_NAME,
                        tags: "latest,${env.APPLICATION_VERSION}"
                    )
                }
            }
        }

    }

}