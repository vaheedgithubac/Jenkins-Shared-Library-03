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
	   }

	   stages {
	   		
	   		stage("LATEST COMMIT ID") {
	   			steps {
					script {
	   					MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   					echo "MY_GIT_LATEST_COMMIT_ID: ${MY_GIT_LATEST_COMMIT_ID}"
						
					}
	   			}
	   		}
           
		   stage("") {
			   steps {
				   script { 
					   echo "env.GIT_LATEST_COMMIT_ID = ${GIT_LATEST_COMMIT_ID}"
			       }
	           }
		  }
		   
	   }  

		
    }

}
