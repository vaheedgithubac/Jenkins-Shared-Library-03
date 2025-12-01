def call(Map config = [:]) {

    // Validate required parameters
    def required = ["JACOCO_GROUPID", "JACOCO_ARTIFACT_ID", "JACOCO_VERSION", "JACOCO_GOAL"]

    required.each { key ->
        if (!config[key]) {
            error "❌ JACOCO: Missing required parameter '${key}'"
        }
    }

    def jacoco_groupId     = config.JACOCO_GROUPID.trim()
    def jacoco_artifactId  = config.JACOCO_ARTIFACT_ID.trim()
    def jacoco_version     = config.JACOCO_VERSION.trim()
    def jacoco_goal        = config.JACOCO_GOAL.trim()

    echo "Running Jacoco step: ${jacoco_groupId}:${jacoco_artifactId}:${jacoco_version}:${jacoco_goal}"
    
    try { sh "mvn ${jacoco_groupId}:${jacoco_artifactId}:${jacoco_version}:${jacoco_goal}" }
    catch (Exception ex) { error "❌ Jacoco Maven step failed: ${ex.message}" }

    echo "JACOCO CODE COVERAGE completed Successfully"

    // sh 'mvn org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent'
}
