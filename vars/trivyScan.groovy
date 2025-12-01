def call(Map config = [:]) {

    // --------------------------------
    // 1️⃣ Validate required parameters
    // --------------------------------
    def required = [
        "project_name",
        "component",
        "mode",
        "git_latest_commit_id",
        "output_report_format",
        "target"
    ]

    required.each { key ->
        if (!config[key]) {
            error "❌ Trivy: Missing required parameter '${key}'"
        }
    }

    def project_name         = config.project_name
    def component            = config.component
    def mode                 = config.mode
    def git_latest_commit_id = config.git_latest_commit_id
    def format               = config.output_report_format
    def target               = config.target

    // -----------------------------------
    // 2️⃣ Determine proper file extension
    // -----------------------------------
    def ext = [
        "table": "html",
        "json" : "json",
        "sarif": "sarif",
        "yaml" : "yaml"
    ][format] ?: format  // fallback to format if unknown
    
    def output_report = ""
    def outDir = "trivy-reports"
    sh "mkdir -p ${outDir}"

    if(mode.toLowerCase() == "fs" ){
        output_report = "${outDir}/${project_name}-${component}-${mode}-${git_latest_commit_id}.${ext}"   // trivy-reports/expense-backend-fs-7drt46y.html
    }
    else if (mode.toLowerCase() == "image"){
        //def safeTarget = target.replaceAll(/[:\/]/, "-")  // replaces ":"" or "/"" with "-"
        output_report = "${outDir}/${project_name}-${component}-${mode}-${git_latest_commit_id}.${ext}"   // trivy-reports/expense-backend-image-7drt46y.html
    }
    else {
        error "❌ Invalid mode: Choose 'fs' or 'image'"
    }

    // -------------------------
    // 3️⃣ Log info
    // -------------------------
    echo "🛡 Running Trivy scan"
    echo "📄 Output: '${output_report}'"
    echo "🎯 Target: '${target}'"

    // ----------------------------------------------------
    // 4️⃣ Run Trivy safely (handle any special characters)
    // ----------------------------------------------------
    sh """
            trivy fs \
            --format ${format} \
            --output ${output_report} \
            --severity MEDIUM,HIGH,CRITICAL \
            .   
    """


    echo "✅ Trivy scan completed successfully."
}
