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
			// MY_GIT_LATEST_COMMIT_ID = ''
			DOCKER_IMAGE = ''   
		    // NEXUS_CREDENTIALS = ${credentials(config.NEXUS_CREDENTIALS_ID)} ?: ""
		    // NEXUS_USER = ''
		    // NEXUS_PASSWORD = ''
			NEXUS_ARTIFACT_VERSION = "${BUILD_ID}-${BUILD_TIMESTAMP}"  
	   }

	   stages {
	   		
	   		stage("SET AND PRINT LATEST COMMIT ID") {
	   			steps {
					script {
	   					env.MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   					echo "MY_GIT_LATEST_COMMIT_ID: ${env.MY_GIT_LATEST_COMMIT_ID}"	
					}
	   			}
	   		}

	   		stage("JACOCO CODE COVERAGE") {
	   			steps {
	   				script {
	   					if (config.EXECUTE_JACOCO_STAGE.toLowerCase()?.trim() == "yes") {
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
				   		if (config.EXECUTE_TRIVY_FS_STAGE.toLowerCase()?.trim() == "yes") {
				   			echo "Running... TRIVY FILE SYSTEM SCAN"
					   		def trivy_file_params = [
					   			MODE:                    "fs",
					   			TARGET:                  config.FS_TARGET,  // current directory
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
					   			OUTPUT_REPORT_FORMAT:     config.TRIVY_FILE_SYSTEM_REPORT_FORMAT
					   		]
					   		trivy_scan(trivy_file_params)
					   	} else { echo "Skipping...STAGE - TRIVY FILE SYSTEM SCAN" }
			       	}
	           	}
		   	}

		   stage("SONARQUBE SCAN - SAST") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_SONARSCAN_STAGE.toLowerCase()?.trim() == "yes") {
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
		   				if (config.EXECUTE_SONAR_QG_STAGE.toLowerCase()?.trim() == "yes") {
				   			echo "Running... SONARQUBE QUALITY GATE"
					   		def sonarqube_params = [ TIMEOUT_MINUTES: config.TIMEOUT_MINUTES ]				  
					   		sonarqubeQG(sonarqube_params)
					   	} else { echo "Skipping...STAGE - SONARQUBE QUALITY GATE" }

		   			}
		   		}
		    }

		   stage("MAVEN BUILD") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_MAVEN_STAGE.toLowerCase()?.trim() == "yes") {
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
		   				if (config.EXECUTE_DOCKER_IMAGE_BUILD_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo "Running...BUILD DOCKER IMAGE"
		   					DOCKER_IMAGE = dockerImageBuild()
		   					echo "IMAGE BUILT SUCCESSFULLY: ${DOCKER_IMAGE}"
		   				} else { echo "Skipping... STAGE - BUILD DOCKER IMAGE" }
		   			}
		   		}
		    }

		   stage("DOCKER IMAGE SCAN - TRIVY") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_TRIVY_IMAGE_STAGE.toLowerCase()?.trim() == "yes") {
		   					echo ("Running...DOCKER IMAGE SCAN - TRIVY")
		   					def trivy_image_params = [
					   			MODE:                    "image",
					   			TARGET:                  DOCKER_IMAGE,  
					   			PROJECT_NAME:            config.PROJECT_NAME,
					   			COMPONENT:               config.COMPONENT,
					   			MY_GIT_LATEST_COMMIT_ID: MY_GIT_LATEST_COMMIT_ID,
					   			OUTPUT_REPORT_FORMAT:     config.TRIVY_IMAGE_REPORT_FORMAT
					   		]
					   		trivy_scan(trivy_image_params)
					    } else { echo "Skipping... STAGE - DOCKER IMAGE SCAN - TRIVY" }
		   			}
		   		}
		    }

		   stage("NEXUS ARTIFACT UPLOAD") {
		   		steps {
		   			script {
		   				if (config.EXECUTE_NEXUS_STAGE.toLowerCase()?.trim() == "yes") {
		   					if (configMap.NEXUS_CREDENTIALS_ID?.trim()) {
    							echo "Nexus credentials ID is provided: ${configMap.NEXUS_CREDENTIALS_ID}"
		   						withCredentials([usernamePassword(
                            		credentialsId: configMap.NEXUS_CREDENTIALS_ID, 
                            		usernameVariable: 'nexus_user', 
                            		passwordVariable: 'nexus_password'
                            	)]) {
                            			//Assign to ENV variables
                            			env.NEXUS_USER = nexus_user
                            			env.NEXUS_PASSWORD = nexus_password
                               	}
                            }
                            // echo " NEXUS_USER: ${nexus_user} NEXUS_PASSWORD: ${nexus_password}"
                            echo "Running...NEXUS ARTIFACT UPLOAD"
		   					def nexusParams = [
					            NEXUS_VERSION:          config.NEXUS_VERSION,
					            NEXUS_PROTOCOL:         config.NEXUS_PROTOCOL,
					            NEXUS_HOST:             config.NEXUS_HOST,
					            NEXUS_PORT:             config.NEXUS_PORT,
					            NEXUS_GRP_ID:           config.NEXUS_GRP_ID,
					            NEXUS_ARTIFACT_VERSION: "${MY_GIT_LATEST_COMMIT_ID}-${NEXUS_ARTIFACT_VERSION}",
					            NEXUS_CREDENTIALS_ID:   config.NEXUS_CREDENTIALS_ID,
								NEXUS_BASE_REPO:        config.NEXUS_BASE_REPO
          					]
          					nexusUpload(nexusParams)
		   				}  else { echo "Skipping... STAGE - NEXUS ARTIFACT UPLOAD"}	
		   			}
		   		}
		    }

		   stage("DOCKER IMAGE UPLOAD - DOCKER HUB") {
		   		steps {
		   			script {
                        if (config.EXECUTE_DOCKER_HUB_PUSH_STAGE.toLowerCase()?.trim() == "yes") {
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
		   				if (config.EXECUTE_ECR_PUSH_STAGE.toLowerCase()?.trim() == "yes") {
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
	   				if (config.EXECUTE_EMAIL_STAGE.toLowerCase()?.trim() == "yes") {
	   					echo "Sending Email"
	   					def emailParams = [
			                 JOB_NAME:        env.JOB_NAME,
			                 BUILD_NUMBER:    env.BUILD_NUMBER,
			                 BUILD_URL:       env.BUILD_URL,
			                 BRANCH_NAME:     env.BRANCH_NAME,
			                 PIPELINE_STATUS: currentBuild.currentResult,
			                 DURATION:        currentBuild.durationString,
			                 FROM_MAIL:       config.FROM_MAIL,
			                 TO_MAIL:         config.TO_MAIL,
			                 REPLY_TO_MAIL:   config.REPLY_TO_MAIL,
			                 CC_MAIL:         config.CC_MAIL,
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
