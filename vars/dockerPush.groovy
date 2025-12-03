def call(Map config = [:]) {

    def requiredDocker = ["DOCKER_IMAGE", "DOCKER_HUB_CREDENTIALS_ID", "DOCKER_REPO_URI"]
    requiredDocker.each { key ->
        if (!config[key]) {
            error "❌ DOCKER REGISTRY: Missing required parameter '${key}'"
        }
    }

    def dockerImage   = config.DOCKER_IMAGE
    def credentialsId = config.DOCKER_HUB_CREDENTIALS_ID
    def dockerRepoUri = config.DOCKER_REPO_URI ?: "docker.io"   // optional, default to Docker Hub

    // Use withCredentials to inject Docker credentials securely
    withCredentials([usernamePassword(
        credentialsId: credentialsId,
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        // Tag the Docker image
        sh """
            echo "🔖 Tagging Docker Image"
            docker tag ${dockerImage} ${DOCKER_USER}/${dockerImage}
        """

        // Login to Docker Hub
        sh """
            set +x
            echo "🔐 Logging into Docker Hub as ${DOCKER_USER}"
            echo "\${DOCKER_PASS}" | docker login -u "\${DOCKER_USER}" --password-stdin
            set -x
        """

        // Push the image
        sh """
            echo "🚀 Pushing Docker Image to Docker Hub"
            docker push ${DOCKER_USER}/${dockerImage}
            echo "✅ Pushed Docker Image Successfully"
        """

        // Logout from Docker Hub
        sh """
            docker logout
            echo "✅ Logged out from Docker Hub Successfully"
        """
    }


   /*
   echo  \${DOCKER_PASS}| docker login -u \${DOCKER_USER} --password-stdin
   withCredentials([usernamePassword(
    credentialsId: credentialsId, 
    usernameVariable: 'DOCKER_USER', 
    passwordVariable: 'DOCKER_PASS'
    )]) {
        sh """
            echo "🔖 Tagging Docker Image"
            docker tag $dockerImage $DOCKER_USER/$dockerImage 
        """
        sh '''                       
            echo "🔐 Logging into Docker Hub as '$DOCKER_USER'"
            echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
        '''
        sh """
            echo "🚀 Pushing Docker Image to Docker Hub"
            docker push $DOCKER_USER/$dockerImage   

            echo "✅ Pushed Docker Image to Docker Hub Successfully"

            docker logout
            echo "✅ Logged out from Docker Hub Successfully"
        """
    }
    */

}
