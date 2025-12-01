def call(Map config = [:]) {

    def required = ["maven_skip_tests"]
    required.each { key ->
        if (!config[key]) {
            error "❌ MAVEN: Missing required parameter '${key}'"
        }
    }

    // Normalize skipTests
    def maven_skip_tests = (config.maven_skip_tests in [true, 'true', "true"]) ? 'true' : 'false'
    def maven_goals = config.maven_goals ?: 'clean package'

    echo "Running Maven: ${maven_goals} -DskipTests=${maven_skip_tests}"

    try { sh "mvn ${maven_goals} -DskipTests=${maven_skip_tests}" } 
    catch (Exception ex) {
        echo "Maven build failed: ${ex.message}"
        error "❌ Maven Build failed"
    }
}
