def call(Map config = [:]) {

    pipeline {
        agent any

        options {
            skipDefaultCheckout(true)
            disableConcurrentBuilds()
            timeout(time: 30, unit: 'MINUTES')
            timestamps()
            ansiColor('xterm')
        }

        environment {
            MY_GIT_LATEST_COMMIT_ID = ''
            DOCKER_IMAGE = ''
        }

        stages {

            stage("Printing Variables") {
                steps {
                    script {
                      echo ("PROJECT_NAME = config.PROJECT_NAME")
                      echo ("MY_GIT_URL = config.MY_GIT_URL")
                      echo ("EXECUTE_JACOCO_STAGE = config.EXECUTE_JACOCO_STAGE")
                      echo ("EXECUTE_SONARSCAN_STAGE = config.EXECUTE_SONARSCAN_STAGE")
                      echo ("EXECUTE_MAVEN_STAGE = config.EXECUTE_MAVEN_STAGE")
                      echo ("EXECUTE_SONAR_QG_STAGE = config.EXECUTE_SONAR_QG_STAGE")
                      echo ("EXECUTE_MAVEN_STAGE = config.EXECUTE_MAVEN_STAGE")
                      echo ("EXECUTE_TRIVY_FS_STAGE = config.EXECUTE_TRIVY_FS_STAGE")
                      echo ("EXECUTE_DOCKER_IMAGE_BUILD_STAGE = config.EXECUTE_DOCKER_IMAGE_BUILD_STAGE")
                      echo ("EXECUTE_TRIVY_IMAGE_STAGE = config.EXECUTE_TRIVY_IMAGE_STAGE")
                      echo ("NEXUS_ARTIFACT_VERSION = config.NEXUS_ARTIFACT_VERSION")
                    }
                }
            }
        }
   }
}
