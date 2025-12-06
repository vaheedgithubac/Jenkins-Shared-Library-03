def call(Map config = [:]) {

    // Validate required parameters
    def sources = config.sources ?: "."
    def required = [ "SONARQUBEAPI", "SCANNER_HOME", "PROJECT_NAME", "PROJECT_KEY" ]

    required.each { key ->
        if (!config[key]) {
            error "❌ SONARQUBE: Missing required parameter '${key}'"
        }
    }

    def sonarqubeAPI = config.SONARQUBEAPI
    def scannerHome  = config.SCANNER_HOME
    def projectName  = config.PROJECT_NAME
    def projectKey   = config.PROJECT_KEY

    withSonarQubeEnv(sonarqubeAPI) {
        sh """
            ${scannerHome}/bin/sonar-scanner \
            -Dsonar.projectName='${projectName}' \
            -Dsonar.projectKey='${projectKey}' -X
        """
    }
  echo "✅ Sonarqube Scan completed Successfully"
}
