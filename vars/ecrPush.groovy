def call(Map config = [:]) {

	def required = ["DOCKER_IMAGE", "ECR_REPO_URI", "REGION", "AWS_CREDENTIALS_ID"]
    required.each { key ->
        if (!config[key]) {
            error "❌ ECR REGISTRY: Missing required parameter '${key}'"
        }
    }
	
    def dockerImage   = config.DOCKER_IMAGE
    def region        = config.REGION ?: "ap-south-1"
    def ecrRepoUri    = config.ECR_REPO_URI
    def credentialsId = config.AWS_CREDENTIALS_ID

    withAWS(credentials: credentialsId, region: "${region}") {  // Plugin: AWS steps
        sh """
            echo "Tagging docker image"
            docker tag ${dockerImage} ${ecrRepoUri}/${dockerImage}
            # docker tag ${projectName}-${component}:${imageTag} ${ecrRepoUri}/${projectName}-${component}:${imageTag}

            echo "Logging into ECR"
            set +x
            aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecrRepoUri}
            set -x

            echo "Pushing docker image to ECR Repo"
            docker push ${ecrRepoUri}/${dockerImage}
            # docker push ${ecrRepoUri}/${projectName}-${component}:${imageTag}
            
            echo "✅ Pushed Docker Image to ECR Successfully"

            # Logout and final confirmation
            ecr logout "${ecrRepoUri}"
            echo "✅ Logged out from ECR Successfully"
        """
    } 
}
