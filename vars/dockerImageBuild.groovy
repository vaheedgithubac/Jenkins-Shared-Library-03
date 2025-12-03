def call(Map config = [:]) {
	def required = ["PROJECT_NAME", "COMPONENT", "MY_GIT_LATEST_COMMIT_ID"]
    required.each { key ->
        if (!config[key]) {
           error "❌ DOCKER BUILD: Missing required parameter '${key}'"
        }
    }

  def projectName   = config.PROJECT_NAME
  def component     = config.COMPONENT
  def imageTag      = config.MY_GIT_LATEST_COMMIT_ID

  def dockerImage   = "${projectName}-${component}:${imageTag}"

  echo "🔨 Building Docker Image"
  sh "docker build . -t ${dockerImage}"
  echo "✅ Docker Image successfully built: ${dockerImage}"

  return dockerImage
}
