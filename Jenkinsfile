@Library('Jenkins-Shared-Library-03') _

def configMap = [
                       // Project Variables //
    PROJECT_NAME              = "myapp"                    // required
    PROJECT_KEY               = "myapp"                    // required
    COMPONENT                 = "backend"                  // required

                       // Git Variables //    
    //MY_GIT_URL                = "https://github.com/vaheedgithubac/DevOps-Project-Two-Tier-Flask-App.git"
    //MY_GIT_REPO_TYPE          = "public"                   // required (public or private)
    //MY_GIT_CREDENTIALS_ID     = ""                         // required for private repos
    //MY_GIT_BRANCH             = ""                         // Defaults to "main" if not set (here "" means jenkins treats as "null")
    // MY_GIT_LATEST_COMMIT_ID   = "null" //get_latest_short_commit()  // <-- calls call() from shared library

                       // Sonarqube Variables //
    SONAR_SCAN_STAGE         = true                        // Defaults to false

]

Pipeline(configMap)
