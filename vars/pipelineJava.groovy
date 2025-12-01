def call(Map config = [:]) {
	pipeline {
		agent any 

		options { 
        	//skipDefaultCheckout true 
        	disableConcurrentBuilds()
        	timeout(time: 30, unit: 'MINUTES')
        	timestamps() 
        	ansiColor('xterm')                                  // plugin: "AnsiColor"
        }

       environment {
			MY_GIT_LATEST_COMMIT_ID = ''
			DOCKER_IMAGE = ''            
	   }

	   stages {
	   		
	   		stage("SET AND PRINT LATEST COMMIT ID") {
	   			steps {
					script {
	   					MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   					echo "MY_GIT_LATEST_COMMIT_ID: ${MY_GIT_LATEST_COMMIT_ID}"	
					}
	   			}
	   		}

	   		stage("JACOCO CODE COVERAGE") {
	   			steps {
	   				script {
	   					if (EXECUTE_JACOCO_STAGE.toLowerCase()?.trim() == "yes") {
	   						echo "Running JACOCO CODE COVERAGE"
	   						def jacoco_params = [
            					JACOCO_GROUPID:     config.JACOCO_GROUPID,
            					JACOCO_ARTIFACT_ID: config.JACOCO_ARTIFACT_ID,
            					JACOCO_VERSION:     config.JACOCO_VERSION,
            					JACOCO_GOAL:        config.JACOCO_GOAL
        					]
        					jacocoCodeCoverage(jacoco_params)
        			   	} else { echo "Skipping...STAGE - JACOCO CODE COVERAGE" }
	   				}
	   			}
	   		 }
           
		   stage("TRIVY FILE SYSTEM SCAN") {
			   steps {
				   script { 
				   		if (EXECUTE_TRIVY_FS_STAGE.toLowerCase()?.trim() == "yes") {
				   			echo "Running... TRIVY FILE SYSTEM SCAN"
					   		def trivy_file_params = [
					   			MODE:                    "fs",
					   			TARGET:                  config.FS_TARGET,  // current directory
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
					   			OUPUT_REPORT_FORMAT:     config.TRIVY_FILE_SYSTEM_REPORT_FORMAT
					   		]
					   		trivy_scan(trivy_file_params)
					   	} else { echo "Skipping...STAGE - TRIVY FILE SYSTEM SCAN" }
			       	}
	           	}
		   	}

		   stage("SONARQUBE SCAN - SAST") {
		   		steps {
		   			script {
		   				if (EXECUTE_SONARSCAN_STAGE.toLowerCase()?.trim() == "yes") {
				   			echo "Running... SONARQUBE SCAN - SAST"
					   		def sonarqube_params = [
					   			SONARQUBEAPI: config.SONARQUBEAPI,
					   			SCANNER_HOME: config.SCANNER_HOME,  
					   			PROJECT_NAME: config.PROJECT_NAME,
					   			PROJECT_KEY:  config.PROJECT_KEY
					   		]
					   		sonarqubeScan(sonarqube_params)
					   	} else { echo "Skipping...STAGE - SONARQUBE SCAN - SAST" }
		   			}
		   		}
		    }

		   stage("SONARQUBE QUALITY GATE") {
		   		steps {
		   			script {
		   				if (EXECUTE_SONARSCAN_STAGE.toLowerCase()?.trim() == "yes") {
				   			echo "Running... SONARQUBE QUALITY GATE"
					   		def sonarqube_params = [ config.TIMEOUT_MINUTES ]				  
					   		sonarqubeQG(sonarqube_params)
					   	} else { echo "Skipping...STAGE - SONARQUBE QUALITY GATE" }

		   			}
		   		}
		    }

		   stage("MAVEN BUILD") {
		   		steps {
		   			script {
		   				if (EXECUTE_MAVEN_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo "Running... MAVEN BUILD"
		   					def maven_params = [ MAVEN_SKIP_TESTS: config.MAVEN_SKIP_TESTS ]
		   					mavenBuild(maven_params)
		   				} else { echo "Skipping...STAGE - MAVEN BUILD" }
		   			}
		   		}
		    }

		   stage("BUILD DOCKER IMAGE") {
		   		steps {
		   			script {
		   				if (EXECUTE_DOCKER_IMAGE_BUILD_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo "Running...BUILD DOCKER IMAGE"
		   					DOCKER_IMAGE = dockerImageBuild()
		   					echo "IMAGE BUILT SUCCESSFULLY: ${IMAGE}"
		   				} else { echo "Skipping... STAGE - BUILD DOCKER IMAGE" }
		   			}
		   		}
		    }

		   stage("DOCKER IMAGE SCAN - TRIVY") {
		   		steps {
		   			script {
		   				if (EXECUTE_TRIVY_IMAGE_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo ("Running...DOCKER IMAGE SCAN - TRIVY")
		   					def trivy_image_params = [
					   			MODE:                    "image",
					   			TARGET:                  DOCKER_IMAGE,  
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
					   			OUPUT_REPORT_FORMAT:     config.TRIVY_IMAGE_REPORT_FORMAT
					   		]
					   		trivy_scan(trivy_image_params)
					    } else { echo "Skipping... STAGE - DOCKER IMAGE SCAN - TRIVY" }
		   			}
		   		}
		    }

		   stage("NEXUS ARTIFACT UPLOAD") {
		   		steps {
		   			script {
		   				if (EXECUTE_NEXUS_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo "Running...NEXUS ARTIFACT UPLOAD"
		   					def nexusParams = [
					            NEXUS_VERSION:          config.NEXUS_VERSION,
					            NEXUS_PROTOCOL:         config.NEXUS_PROTOCOL,
					            NEXUS_HOST:             config.NEXUS_HOST,
					            NEXUS_PORT:             config.NEXUS_PORT,
					            NEXUS_GRP_ID:           config.NEXUS_GRP_ID,
					            NEXUS_ARTIFACT_VERSION: "${MY_GIT_LATEST_COMMIT_ID}-${config.NEXUS_ARTIFACT_VERSION}",
					            NEXUS_CREDENTIALS_ID:   config.NEXUS_CREDENTIALS_ID,
					            NEXUS_BASE_REPO:        config.NEXUS_BASE_REPO
          					]
          					nexusUpload(nexusParams)
		   				}
		   				
		   			}
		   		}
		    }

		   stage("DOCKER IMAGE UPLOAD - DOCKER HUB") {
		   		steps {
		   			script {
                        if (EXECUTE_DOCKER_HUB_PUSH_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo "Running...DOCKER IMAGE UPLOAD - DOCKER HUB"
		   					def dockerhub_upload_params = [
		   						DOCKER_IMAGE:              DOCKER_IMAGE,
		   						DOCKER_REPO_URI:           config.DOCKER_REPO_URI,
		   						DOCKER_HUB_CREDENTIALS_ID: config.DOCKER_HUB_CREDENTIALS_ID
		   					]
		   					dockerPush(dockerhub_upload_params)
		   				} else { echo "Skipping... STAGE - DOCKER IMAGE UPLOAD - DOCKER HUB" }
		   			}
		   		}
		    }

		   stage("DOCKER IMAGE UPLOAD - ECR") {
		   		steps {
		   			script {
		   				if (EXECUTE_DOCKER_HUB_PUSH_STAGE.toLowerCase()?.trim() == "yes") {
		   				echo "Running...DOCKER IMAGE UPLOAD - ECR"
		   				def ecr_upload_params = [
		   						DOCKER_IMAGE:       DOCKER_IMAGE,
		   						ECR_REPO_URI:       config.ECR_REPO_URI,
		   						AWS_CREDENTIALS_ID: config.AWS_CREDENTIALS_ID
		   					]
		   					ecrPush(ecr_upload_params)
		   				} else { echo "Skipping... STAGE - DOCKER IMAGE UPLOAD - ECR" }
		   			}
		   		}
		    }


		   
	   } // stages

	   post {
	   		always {
	   			script {
	   				if (EXECUTE_EMAIL_STAGE.toLowerCase()?.trim() == "yes") {
	   					echo "Sending Email"
	   					def emailParams = [
			                 JOB_NAME:        config.JOB_NAME,
			                 BUILD_NUMBER:    config.BUILD_NUMBER,
			                 BUILD_URL:       config.BUILD_URL,
			                 BRANCH_NAME:     config.BRANCH_NAME,
			                 PIPELINE_STATUS: currentBuild.currentResult,
			                 DURATION:        currentBuild.durationString,
			                 FROM_MAIL:       config.FROM_MAIL,
			                 TO_MAIL:         config.TO_MAIL,
			                 REPLY_TO_MAIL:   config.REPLY_TO_MAIL,
			                 CC_MAIL:         env.CC_MAIL,
			                 BCC_MAIL:        config.BCC_MAIL,
			                 ATTACHMENTS:     config.ATTACHMENTS                      // "trivy-reports/*, owasp-reports/*"
			            ]               
	   				    sendEmail(emailParams)
	   				} else { echo "Skipping... POST - STAGE - Sending Mail" }
	   			}
	   		}
	   }


    } // pipeline

} // def
