pipeline {

    agent any

    tools {
        maven '3.6.3'
    }

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
        APPLICATION_VERSION = "1.0.0"
    }

    stages {

        stage('Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B -V -U -e -DskipTests package'
                    }
                    else {
                        bat "mvn -B -V -U -e -DskipTests package"
                    }
                    reportBuild(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        artifactFileName: "target/plugin-devops-portal.hpi"
                    )
                }
            }
        }

        stage('Test') {
            steps {
                script {

                    reportUnitTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "other-component",
                        testsPassed: 31,
                        testsFailed: 6,
                        testsIgnored: 2,
                        testCoverage: 0.51
                    )

                    withMaven() {
                        if (isUnix()) {
                            sh 'mvn -B test'
                        }
                        else {
                            bat "mvn -B test"
                        }
                        reportSurefireTest(
                            applicationName: env.APPLICATION_NAME,
                            applicationVersion: env.APPLICATION_VERSION,
                            applicationComponent: "plugin-devops-portal",
                            surefireReportPath: "target/surefire-reports/TEST-InjectedTest.xml"
                        )
                    }
                }
            }
        }

        stage('Audit') {
            steps {
                script {

                    // Quality audit reported from Sonar Qube
                    withSonarQubeEnv(credentialsId: 'c191d43f-0199-4f04-95a1-3afe1cd9803e', installationName: 'SonarQube Scanner') {
                        withMaven() {
                            if (isUnix()) {
                                sh 'mvn -Djavax.net.ssl.trustStore=src/test/jobs/test.jks -Djavax.net.ssl.trustStorePassword=123456789 sonar:sonar'
                            }
                            else {
                                bat "mvn -Djavax.net.ssl.trustStore=src\\test\\jobs\\test.jks -Djavax.net.ssl.trustStorePassword=123456789 sonar:sonar"
                            }
                        }
                        reportSonarQubeAudit(
                            applicationName: env.APPLICATION_NAME,
                            applicationVersion: env.APPLICATION_VERSION,
                            applicationComponent: "plugin-devops-portal",
                            projectKey: "io.jenkins.plugins:plugin-devops-portal"
                        )
                    }

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

        stage('Release') {
            steps {
                script {
                    reportArtifactRelease(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        repositoryName: "registry.mydomain.com",
                        artifactName: env.APPLICATION_NAME,
                        tags: "docker-image,snapshot,${env.APPLICATION_VERSION}",
                        artifactURL: "https://registry.mydomain.com/projects/plugin-devops-portal/${env.APPLICATION_VERSION}"
                    )
                }
            }
        }

        stage('Performance') {
            steps {
                script {
                    reportPerformanceTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        testCount: 10,
                        sampleCount: 50,
                        errorCount: 0
                    )
                }
            }
        }

    }

}