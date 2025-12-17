

//--------------------------------------------------------------------------------
// The updateDependentFiles.groovy reads a project.settings file to determine
// a projects dependencies. It then updates the project files defined
//--------------------------------------------------------------------------------

def call(String settingsFile = 'project.settings') {
    def project = getProjectSettings(settingsFile)

    int count = 0
    if (project?.containsKey('dependencies')) {
        project.dependencies.each { param, dependencies ->
            println 'updateDependentFiles - build result: ' + currentBuild.currentResult
            if (currentBuild.currentResult == 'SUCCESS') {
                println "updateDependentFiles:updateDependentFiles - param: dependencies ->  ${param}: ${dependencies}"
                count += updateDependencies(param, dependencies)
            }
        }
    }
    println 'updateDependentFiles - build result: ' + currentBuild.currentResult
    println "updateDependentFiles - Updated ${count} dependent files"
}

//--------------------------------------------------------------------------------
private int updateDependencies(String param, List dependencies) {
    int count = 0
    def orgParm = System.env[param]
    dependencies.reverseEach { dependency ->
        if (currentBuild.currentResult == 'SUCCESS') {
            println "    updateDependentFiles:updateDependencies - repo: ${dependency.repo}, files: ${dependency.files}"

            String cmd = dependency.repo?.trim() ? "git ls-remote ${dependency.repo} HEAD" : 'git rev-parse HEAD'
            if (sh returnStatus: true, script: cmd)
               count++

            if (!orgParm) {
                String cmd = dependency.repo?.trim() ? "git ls-remote ${dependency.repo} HEAD" : 'git rev-parse HEAD'
		def commitHash = sh returnStdout: false, script: cmd
                orgParm = commitHash?.take(8)
            }
            println "    updateDependentFiles:updateDependencies - orgParm: ${orgParm}"
            env["${param}"] = orgParm

            String sedExpr='s|\\\\$\\\\{?' + param + '\\\\}?|' + orgParm + '|g'
            dependency.files.each { file ->
               println "    updateDependentFiles:updateDependencies - updating '${file}'"
               String templateFile = file + '.template'
               String cmd = '[ -f "' + templateFile + '" ] && sed -E -e ' + sedExpr + ' "' + templateFile + '" > "' + file + '"'
               sh returnStdout: false, script: cmd
            }
        }
    }
    println "    updateDependentFiles:updateDependencies - count: ${count}"
    return count
}

//--------------------------------------------------------------------------------
@NonCPS
private int updateFiles(String param, String val, List files) {
    int count = 0
    String sedExpr='s|\\\\$\\\\{?' + param + '\\\\}?|' + val + '|g'
    files.each { file ->
       println "    updateDependentFiles:updateFiles - updating '${file}'"
       String templateFile = file + '.template'
       String cmd = '[ -f "' + templateFile + '" ] && sed -E -e ' + sedExpr + ' "' + templateFile + '" > "' + file + '"'
       sh returnStatus: false, returnStdout: false, script: cmd
       count++
       println "    updateDependentFiles:updateFiles - count: ${count}"
    }
    println "    updateDependentFiles:updateFiles - count: ${count}"
    return count
}

//--------------------------------------------------------------------------------
//@NonCPS
private int updateFilesX(String param, String val, List files) {
    int count = 0
    files.each { file ->
        def outputFile, templateFile
        try {
            templateFile = file + '.template'
            println "    updateDependentFiles:updateFiles - templateFile '${templateFile}'"
            if (fileExists(templateFile)) {
                outputFile = file
                println "    updateDependentFiles:updateFiles - outputFile '${file}'"
            }
            else {
                templateFile = 'ci/' + file + '.template'
                println "    updateDependentFiles:updateFiles - templateFile '${templateFile}'"
                if (fileExists(templateFile)) {
                    outputFile ='ci/' + file
                    println "    updateDependentFiles:updateFiles - outputFile 'ci/${file}'"
                }
                else {
                    throw new FileNotFoundException("template file '${file}' not found.")
                }
            }
            println "    updateDependentFiles:updateFiles - updating '${file}'"

            def substVal = System.getProperty(param)
            if (substVal) {
                def substText = readFile(templateFile)
                def subStr = '\\$\\{' + param + '\\}'
                if (substText =~ subStr) {
                    substText = substText.replaceAll(subStr, substVal)
                }
                subStr = '\\$' + param
                if (substText =~ subStr) {
                    substText = substText.replaceAll(subStr, substVal)
                }
                writeFile(outputFile, substText)
                count++
           }
        }
        catch(e) {
            println "updateDependentFiles:updateFiles - Error updating '${file}', reason: ${e.getMessage()}"
            manager.addWarningBadge('Error updating dependencies')
	    manager.buildUnstable()
        }
    }
    return count
}
