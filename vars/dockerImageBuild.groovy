def call(Map config = [:]) {
	def required = ["PROJECT_NAME", "COMPONENT", "MY_GIT_LATEST_COMMIT_ID"]
  required.each { key ->
       if (!config[key]) {
           error "❌ DOCKER BUILD: Missing required parameter '${key}'"
       }
  }

  def projectName   = env.PROJECT_NAME
  def component     = env.COMPONENT
  def imageTag      = env.MY_GIT_LATEST_COMMIT_ID

  def image = "${projectName}-${component}:${imageTag}"

  echo "🔨 Building Docker Image"
  sh "docker build . -t ${image}"
  echo "✅ Docker Image successfully built: ${image}"
}
