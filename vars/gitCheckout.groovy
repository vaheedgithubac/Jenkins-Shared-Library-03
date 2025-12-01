def call(Map config = [:]) {
    
    echo "MY_GIT_URL: ${env.MY_GIT_URL}"
    echo "MY_GIT_REPO_TYPE: ${env.MY_GIT_REPO_TYPE}"
    
    // Required ENV parameters
    def requiredEnvParams = [ "MY_GIT_URL", "MY_GIT_REPO_TYPE" ]
   
   // Validate ENV parameters
    requiredEnvParams.each { key ->
        if (!env[key] || env[key].trim() == "") {
            error "❌ GIT: Missing required ENV parameter '${key}'"
        }
    }
 
    def my_git_repo_type = env.MY_GIT_REPO_TYPE.toLowerCase().trim()
    def my_git_url       = env.MY_GIT_URL.trim()
    
    // Set defaults for optional    
    def my_git_branch    = env.MY_GIT_BRANCH ?: 'main'
    def my_git_credentials_id = env.MY_GIT_CREDENTIALS_ID ?: null
    

   
    // === Handle credentials === 
    if (my_git_repo_type == "public") { echo "⚡ Public repo detected, git credentials not needed to checkout" } 
    if (my_git_repo_type == "private") {
       /*
       if (!my_git_credentials_id || my_git_credentials_id?.trim().toLowerCase() == "null" || my_git_credentials_id?.trim() == "") {
           echo "⚡ Private repo detected, git credentials needed"
           error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
           } 
        if (my_git_repo_type == "private") {
            if (
                !my_git_credentials_id ||
                my_git_credentials_id?.trim().toLowerCase() == "null"
            ) {
                echo "⚡ Private repo detected, git credentials needed"
                error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
            }
        } */
        if (
            !my_git_credentials_id ||
            my_git_credentials_id == null || 
            my_git_credentials_id.trim() == "" || 
            my_git_credentials_id.trim().toLowerCase() == "null"
        ) {
           echo "⚡ Private repo detected, git credentials needed"
           error "❌ MY_GIT_CREDENTIALS_ID is required for private repositories."
          }

       }
    if (!(my_git_repo_type in ['private', 'public'])) { error "❌ MY_GIT_REPO_TYPE must be 'public' or 'private'. Current: '${my_git_repo_type}'" }

    // === Log final values === 
    echo "✔ MY_GIT_URL            = ${my_git_url}"
    echo "✔ MY_GIT_REPO_TYPE      = ${my_git_repo_type}"
    echo "✔ MY_GIT_BRANCH         = ${my_git_branch}"
    echo "✔ MY_GIT_CREDENTIALS_ID = ${my_git_credentials_id}"

    
    // === Perform Git Checkout ===
    // def commitId = "no-SHA"
    git(
        url: my_git_url,
        branch: my_git_branch,
        credentialsId: my_git_credentials_id
    ) 
    
        // Capture Latest Commit ID
        commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        echo "Checked out Branch: '${my_git_branch}' from ${my_git_url} Successfully..."
        echo "From Shared Library Latest CommitId = ${commitId}"
    
        //env.MY_GIT_LATEST_COMMIT_ID = commitId
        // return commitId
    
}
