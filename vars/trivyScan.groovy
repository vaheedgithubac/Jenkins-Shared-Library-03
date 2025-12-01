def call(Map config = [:]) {

    // --------------------------------
    // 1️⃣ Validate required parameters
    // --------------------------------
    def required = [
        "MODE",
        "TARGET",
        "PROJECT_NAME",
        "COMPONENT",
        "MY_GIT_LATEST_COMMIT_ID",
        "OUPUT_REPORT_FORMAT"
    ]

    required.each { key ->
        if (!config[key]) {
            error "❌ TRIVY ${config.MODE.toUpperCase()?.trim()} SCAN: Missing required parameter '${key}'"
        }
    }

    def mode                 = config.MODE
    def target               = config.TARGET
    def project_name         = config.PROJECT_NAME
    def component            = config.COMPONENT
    def git_latest_commit_id = config.MY_GIT_LATEST_COMMIT_ID
    def output_report_format = config.OUPUT_REPORT_FORMAT

    // -----------------------------------
    // 2️⃣ Determine proper file extension
    // -----------------------------------
    def ext = [
        "table": "html",
        "json" : "json",
        "sarif": "sarif",
        "yaml" : "yaml"
    ][output_report_format] ?: format  // fallback to 'format' if unknown
    
    def output_report = ""
    def outDir        = "trivy-reports"
    sh "mkdir -p ${outDir}"

    if (mode.toLowerCase()?.trim() == "fs" ) {
        output_report = "${outDir}/${project_name}-${component}-${mode}-${git_latest_commit_id}.${ext}"   // trivy-reports/expense-backend-fs-7drt46y.html
    }
    else if (mode.toLowerCase()?.trim() == "image") {
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
            trivy {mode} \
            --format ${format} \
            --output ${output_report} \
            --severity MEDIUM,HIGH,CRITICAL \
            .   
    """
    echo "✅ Trivy scan completed successfully. Report stored at: '${env.WORKSPACE}/${output_report}'"
}
