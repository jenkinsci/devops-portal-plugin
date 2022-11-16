
@NonCPS
def getTestResults(path) {
  def results = [ passed: 0, failed: 0, ignored: 0 ]
  def filter = ~/.*\.xml$/
  new File(path).traverse(type: groovy.io.FileType.FILES, nameFilter: filter) { file ->
      def data = file.filterLine { line ->
          line.startsWith('<testsuite ')
      }
      if (data) {
        def pattern = ~/errors="(.*?)" skipped="(.*?)" failures="(.*?)"/
        def matcher = data =~ pattern
        println matcher.find()
        println matcher.size()
        println matcher[0..-1]
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

                    def results = getTestResults('target/surefire-reports')
                    println results
                }
            }
        }

    }

}