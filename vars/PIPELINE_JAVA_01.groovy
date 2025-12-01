def call(Map config = [:]) {

    pipeline {
        agent any

        options {
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

            /*
            stage("SET AND PRINT LATEST COMMIT ID") {
                steps {
                    script {
                        MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
                        echo "MY_GIT_LATEST_COMMIT_ID: ${MY_GIT_LATEST_COMMIT_ID}"
                    }
                }
            }*/

            stage("JACOCO CODE COVERAGE") {
                when { expression { config.EXECUTE_JACOCO_STAGE == "yes" } }
                steps {
                    script {
                        jacocoCodeCoverage([
                            JACOCO_GROUPID:     config.JACOCO_GROUPID,
                            JACOCO_ARTIFACT_ID: config.JACOCO_ARTIFACT_ID,
                            JACOCO_VERSION:     config.JACOCO_VERSION,
                            JACOCO_GOAL:        config.JACOCO_GOAL
                        ])
                    }
                }
            }

            stage("TRIVY FILE SYSTEM SCAN") {
                when { expression { config.EXECUTE_TRIVY_FS_STAGE == "yes" } }
                steps {
                    script {
                        trivyScan([
                            MODE:                 "fs",
                            TARGET:               config.FS_TARGET,
                            PROJECT_NAME:         config.PROJECT_NAME,
                            COMPONENT:            config.COMPONENT,
                            MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
                            OUTPUT_REPORT_FORMAT: config.TRIVY_FILE_SYSTEM_REPORT_FORMAT
                        ])
                    }
                }
            }

            stage("SONARQUBE SCAN - SAST") {
                when { expression { config.EXECUTE_SONARSCAN_STAGE == "yes" } }
                steps {
                    script {
                        sonarqubeScan([
                            SONARQUBEAPI: config.SONARQUBEAPI,
                            SCANNER_HOME: config.SCANNER_HOME,
                            PROJECT_NAME: config.PROJECT_NAME,
                            PROJECT_KEY:  config.PROJECT_KEY
                        ])
                    }
                }
            }

            stage("SONARQUBE QUALITY GATE") {
                when { expression { config.EXECUTE_SONAR_QG_STAGE == "yes" } }
                steps {
                    script {
                        sonarqubeQG([
                            TIMEOUT_MINUTES: config.TIMEOUT_MINUTES
                        ])
                    }
                }
            }

            stage("MAVEN BUILD") {
                when { expression { config.EXECUTE_MAVEN_STAGE == "yes" } }
                steps {
                    script {
                        mavenBuild([ MAVEN_SKIP_TESTS: config.MAVEN_SKIP_TESTS ])
                    }
                }
            }

            stage("BUILD DOCKER IMAGE") {
                when { expression { config.EXECUTE_DOCKER_IMAGE_BUILD_STAGE == "yes" } }
                steps {
                    script {
                        DOCKER_IMAGE = dockerImageBuild()
                        echo "IMAGE BUILT SUCCESSFULLY: ${DOCKER_IMAGE}"
                    }
                }
            }

            stage("DOCKER IMAGE SCAN - TRIVY") {
                when { expression { config.EXECUTE_TRIVY_IMAGE_STAGE == "yes" } }
                steps {
                    script {
                        trivyScan([
                            MODE:                    "image",
                            TARGET:                  DOCKER_IMAGE,
                            PROJECT_NAME:            config.PROJECT_NAME,
                            COMPONENT:               config.COMPONENT,
                            MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
                            OUTPUT_REPORT_FORMAT:    config.TRIVY_IMAGE_REPORT_FORMAT
                        ])
                    }
                }
            }

            stage("NEXUS ARTIFACT UPLOAD") {
                when { expression { config.EXECUTE_NEXUS_STAGE == "yes" } }
                steps {
                    script {
                        nexusUpload([
                            NEXUS_VERSION:        config.NEXUS_VERSION,
                            NEXUS_PROTOCOL:       config.NEXUS_PROTOCOL,
                            NEXUS_HOST:           config.NEXUS_HOST,
                            NEXUS_PORT:           config.NEXUS_PORT,
                            NEXUS_GRP_ID:         config.NEXUS_GRP_ID,
                            NEXUS_ARTIFACT_VERSION: "${MY_GIT_LATEST_COMMIT_ID}-${config.NEXUS_ARTIFACT_VERSION}",
                            NEXUS_CREDENTIALS_ID: config.NEXUS_CREDENTIALS_ID,
                            NEXUS_BASE_REPO:      config.NEXUS_BASE_REPO
                        ])
                    }
                }
            }

            stage("DOCKER IMAGE UPLOAD - DOCKER HUB") {
                when { expression { config.EXECUTE_DOCKER_HUB_PUSH_STAGE == "yes" } }
                steps {
                    script {
                        dockerPush([
                            DOCKER_IMAGE: DOCKER_IMAGE,
                            DOCKER_REPO_URI: config.DOCKER_REPO_URI,
                            DOCKER_HUB_CREDENTIALS_ID: config.DOCKER_HUB_CREDENTIALS_ID
                        ])
                    }
                }
            }

            stage("DOCKER IMAGE UPLOAD - ECR") {
                when { expression { config.EXECUTE_ECR_PUSH_STAGE == "yes" } }
                steps {
                    script {
                        ecrPush([
                            DOCKER_IMAGE: DOCKER_IMAGE,
                            ECR_REPO_URI: config.ECR_REPO_URI,
                            AWS_CREDENTIALS_ID: config.AWS_CREDENTIALS_ID
                        ])
                    }
                }
            }

        } // stages

        post {
            always {
                script {
                    if (config.EXECUTE_EMAIL_STAGE == "yes") {
                        sendEmail([
                            JOB_NAME:     env.JOB_NAME,
                            BUILD_NUMBER: env.BUILD_NUMBER,
                            BUILD_URL:    env.BUILD_URL,
                            BRANCH_NAME:  env.BRANCH_NAME,
                            PIPELINE_STATUS: currentBuild.currentResult,
                            DURATION:        currentBuild.durationString,
                            FROM_MAIL:       config.FROM_MAIL,
                            TO_MAIL:         config.TO_MAIL,
                            REPLY_TO_MAIL:   config.REPLY_TO_MAIL,
                            CC_MAIL:         config.CC_MAIL,
                            BCC_MAIL:        config.BCC_MAIL,
                            ATTACHMENTS:     config.ATTACHMENTS
                        ])
                    }
                }
            }
        }

    } // pipeline
} // def
