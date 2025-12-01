def call(Map config = [:]) {
	pipeline {
		agent any 

		options { 
        	skipDefaultCheckout true 
        	disableConcurrentBuilds()
        	timeout(time: 30, unit: 'MINUTES')
        	timestamps() 
        	ansiColor('xterm')                                  // plugin: "AnsiColor"
       }

       environment {
			MY_GIT_LATEST_COMMIT_ID = ''
	   }

	   stages {
	   		
	   		stage("GIT CHECKOUT") {
	   			steps {
					script { 
              			gitCheckout()
					}
	   			}
	   		}
           
		   stage("SET AND PRINT LATEST COMMIT ID") {
			   steps {
				   script { 
             	   		MY_GIT_LATEST_COMMIT_ID = getLatestCommitIdShort()
	   				 	echo "GIT_LATEST_COMMIT_ID: ${GIT_LATEST_COMMIT_ID}"
			       }
	           }
		   }
		   
	   }  // stages
  }  // pipeline
} // def
