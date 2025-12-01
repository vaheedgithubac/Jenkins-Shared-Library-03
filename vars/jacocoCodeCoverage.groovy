def call(Map config = [:]) {

    // Validate required parameters
    def required = ["jacoco_groupId", "jacoco_artifactId", "jacoco_version", "jacoco_goal"]

    required.each { key ->
        if (!config[key]) {
            error "❌ JACOCO: Missing required parameter '${key}'"
        }
    }

    def jacoco_groupId     = config.jacoco_groupId.trim()
    def jacoco_artifactId  = config.jacoco_artifactId.trim()
    def jacoco_version     = config.jacoco_version.trim()
    def jacoco_goal        = config.jacoco_goal.trim()

    echo "Running Jacoco step: ${groupId}:${artifactId}:${version}:${goal}"
    
    try { sh "mvn ${jacoco_groupId}:${jacoco_artifactId}:${jacoco_version}:${jacoco_goal}" }
    catch (Exception ex) { error "❌ Jacoco Maven step failed: ${ex.message}" }

    // sh 'mvn org.jacoco:jacoco-maven-plugin:0.8.7:prepare-agent'
}
