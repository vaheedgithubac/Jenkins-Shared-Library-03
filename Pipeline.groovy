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
	   				GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   				echo "GIT_LATEST_COMMIT_ID: ${GIT_LATEST_COMMIT_ID}"
	   			}
	   		}
	   }
    }

}
