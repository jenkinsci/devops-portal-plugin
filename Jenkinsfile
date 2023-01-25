pipeline {

    agent any

    tools {
        maven '3.6.3'
    }

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
    }

    stages {

        stage('Build') {
            steps {
                script {
                    env.APPLICATION_VERSION = sh(script: "grep -m1 '<release.version>' pom.xml | cut -d '<' -f2  | cut -d '>' -f2", returnStdout: true).trim()
                    withMaven() {
                        sh 'mvn -B -V -U -e -DskipTests clean package dependency-check:aggregate'
                    }
                    reportBuild(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        artifactFileName: "target/devops-portal.hpi"
                    )
                    reportMavenDependenciesAnalysis(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        reportPath: "target/dependency-check-report.xml"
                    )
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/devops-portal.hpi', onlyIfSuccessful: true
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    withMaven() {
                        sh 'mvn -B test'
                    }
                    reportSurefireTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        surefireReportPath: "target/surefire-reports/*.xml"
                    )
                }
            }
        }

        stage('Audit') {
            steps {
                script {

                    // Quality audit reported from Sonar Qube
                    withSonarQubeEnv(credentialsId: 'sonar-forge', installationName: 'SonarQube Scanner') {
                        withMaven() {
                            sh 'mvn -Djavax.net.ssl.trustStore=src/test/jobs/test.jks -Djavax.net.ssl.trustStorePassword=123456789 sonar:sonar'
                        }
                        reportSonarQubeAudit(
                            applicationName: env.APPLICATION_NAME,
                            applicationVersion: env.APPLICATION_VERSION,
                            applicationComponent: "plugin-devops-portal",
                            projectKey: "io.jenkins.plugins.devops-portal:" + env.APPLICATION_VERSION
                        )
                    }

                }
            }
        }

        stage('Release') {
            steps {
                script {
                    reportArtifactRelease(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        repositoryName: "github.com",
                        artifactName: env.APPLICATION_NAME,
                        tags: "hpi,${env.APPLICATION_VERSION}",
                        artifactURL: "https://github.com/rbello/jenkins-plugin-devops-portal/releases/tag/${env.APPLICATION_VERSION}"
                    )
                }
            }
        }

    }

}