def call() {
    try { return sh(script: "git rev-parse --short HEAD", returnStdout: true).trim() } 
    catch (e) { return "no-sha" }
}
