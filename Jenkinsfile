
@NonCPS
def getTestResults(path) {
  def results = [ passed: 0, failed: 0, ignored: 0 ]
  def filter = ~/.*\.xml$/
  new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: filter) { file ->
      def data = file.filterLine { line ->
          line.startsWith('<testsuite ')
      }
      if (data) {
        def pattern = ~/tests="(.*?)" errors="(.*?)" skipped="(.*?)" failures="(.*?)"/
        def matcher = data =~ pattern
        if (matcher.size() == 1 && matcher[0].size() == 5) {
            results.passed += matcher[0][1].toInteger();
            results.failed += matcher[0][2].toInteger() + matcher[0][4].toInteger();
            results.ignored += matcher[0][3].toInteger();
        }
      }
  }
  return results
}

pipeline {

    agent any

    environment {
        APPLICATION_NAME = "jenkins-plugin-devops-portal"
        APPLICATION_VERSION = "1.0.0"
        MAVEN_PATH = 'C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2022.2.3\\plugins\\maven\\lib\\maven3\\bin\\mvn'
    }

    stages {

        stage('Build') {
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
                        artifactFileName: "${env.WORKSPACE}/target/plugin-devops-portal.hpi"
                    )
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'mvn -B test'
                    }
                    else {
                        bat "\"${env.MAVEN_PATH}\" -B test"
                    }
                    def results = getTestResults('target/surefire-reports')
                    reportUnitTest(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin",
                        testsPassed: results.passed,
                        testsFailed: results.failed,
                        testsIgnored: results.ignored
                    )
                }
            }
        }

        stage('Audit') {
            steps {
                script {
                    reportDependenciesAnalysis(
                        applicationName: env.APPLICATION_NAME,
                        applicationVersion: env.APPLICATION_VERSION,
                        applicationComponent: "plugin",
                        baseDirectory: "",
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
                        applicationComponent: "plugin",
                        registryName: "registry.mydomain.com",
                        imageName: env.APPLICATION_NAME,
                        tags: "latest,${env.APPLICATION_VERSION}"
                    )
                }
            }
        }

    }

}