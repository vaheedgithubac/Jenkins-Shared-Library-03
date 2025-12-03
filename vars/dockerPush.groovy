def call(Map config = [:]) {

    def requiredDocker = ["DOCKER_IMAGE", "DOCKER_HUB_CREDENTIALS_ID", "DOCKER_REPO_URI"]
    requiredDocker.each { key ->
        if (!config[key]) {
            error "❌ DOCKER REGISTRY: Missing required parameter '${key}'"
        }
    }

    def dockerImage   = config.DOCKER_IMAGE
    def credentialsId = config.DOCKER_HUB_CREDENTIALS_ID
    def dockerRepoUri = config.DOCKER_REPO_URI ?: "docker.io"   

    withCredentials([usernamePassword(
        credentialsId: credentialsId, 
        usernameVariable: 'dockerUser', 
        passwordVariable: 'dockerPass'  
    )]) {
            sh """
                echo "🔖 Tagging Docker Image"  #docker.io/dockeruser/expense-backend:5d4ret
                # docker tag ${projectName}-${component}:${imageTag} ${dockerUser}/${projectName}-${component}:${imageTag}
                docker tag ${dockerImage} ${dockerUser}/${dockerImage}
                                        
                echo "🔐 Logging into Docker Hub as '${dockerUser}'"
                set +x
                echo "${dockerPass}" | docker login -u "${dockerUser}" --password-stdin
                set -x

                echo "🚀 Pushing Docker Image to Docker Hub"
                # docker push ${dockerUser}/${projectName}-${component}:${imageTag}
                docker push ${dockerUser}/${dockerImage}

                echo "✅ Pushed Docker Image to Docker Hub Successfully"

                # Logout and final confirmation
                docker logout
                echo "✅ Logged out from Docker Hub Successfully"
            """    
        }
}
