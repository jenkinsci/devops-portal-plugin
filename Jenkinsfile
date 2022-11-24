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
                        testsIgnored: 2,
                        testCoverage: 0.51
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

                    // Quality audit manually reported
                    reportQualityAudit(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "other-component",
                        bugCount: 1,
                        bugScore: "B",
                        vulnerabilityCount: 3,
                        vulnerabilityScore: "C",
                        hotspotCount: 2,
                        hotspotScore: "D",
                        duplicationRate: 0.04,
                        testCoverage: 0.35,
                        linesCount: 32000,
                        qualityGatePassed: true
                    )

                    // Quality audit reported from Sonar Qube
                    withSonarQubeEnv(credentialsId: 'c191d43f-0199-4f04-95a1-3afe1cd9803e', installationName: 'SonarQube Scanner') {
                        withMaven() {
                            if (isUnix()) {
                                sh 'mvn -Djavax.net.ssl.trustStore=src/test/jobs/test.jks -Djavax.net.ssl.trustStorePassword=123456789 sonar:sonar'
                            }
                            else {
                                bat "\"${env.MAVEN_PATH}\" -Djavax.net.ssl.trustStore=src\\test\\jobs\\test.jks -Djavax.net.ssl.trustStorePassword=123456789 sonar:sonar"
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
                    /*reportDependenciesAnalysis(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin-devops-portal",
                        manifestFile: "pom.xml",
                        manager: "MAVEN"
                    )*/

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