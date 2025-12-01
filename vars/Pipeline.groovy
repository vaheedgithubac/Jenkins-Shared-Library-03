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
			GIT_LATEST_COMMIT_ID = ''
	   }

	   stages {
	   		
	   		stage("LATEST COMMIT ID") {
	   			steps {
					script {
	   					GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   					echo "GIT_LATEST_COMMIT_ID: ${GIT_LATEST_COMMIT_ID}"
						 echo "env.GIT_LATEST_COMMIT_ID = ${env.GIT_LATEST_COMMIT_ID}"
					}
	   			}
	   		}
           /*
		   stage("") {
			   steps {
				   script { 
					   GIT_LATEST_COMMIT_ID = env.GIT_COMMIT.take(7).trim()
					   echo "env.GIT_COMMIT = ${env.GIT_COMMIT.take(7).trim()}"}
				       echo "env.GIT_LATEST_COMMIT_ID = ${GIT_LATEST_COMMIT_ID}"
			   }
	       } */
		   
	   }  

		
    }

}
