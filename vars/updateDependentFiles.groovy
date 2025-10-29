

//--------------------------------------------------------------------------------
// The updateDependentFiles.groovy reads a project.settings file to determine
// a projects dependencies. It then updates the project files defined
//--------------------------------------------------------------------------------

//@NonCPS
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
//@NonCPS
private int updateDependencies(String param, List dependencies) {
    int count = 0
    def orgParm = System.env[param]
    dependencies.reverseEach { dependency ->
        if (currentBuild.currentResult == 'SUCCESS') {
            println "    updateDependentFiles:updateDependencies - repo: ${dependency.repo}, files: ${dependency.files}"
            if (!orgParm)
                orgParm = updateEnvironmentParam(param, dependency.repo)
            count += updateFiles(param, orgParm, dependency.files)
        }
    }
    return count
}

//--------------------------------------------------------------------------------
//@NonCPS
private String updateEnvironmentParam(String param, String repo) {
    def latest = getLatest(repo)
    env["${param}"] = latest
//    def ev = env["${param}"]
//    println "    updateDependentFiles:updateEnvironmentParam - ${param}: '${ev}'"
    return latest
}

//--------------------------------------------------------------------------------
@NonCPS
private int updateFiles(String param, String val, List files) {
    int count = 0
    files.each { file ->
       sh """
           set -x
           [ ! -f "${file}.template" ] && exit 1
           echo 'updating "${file}"'
	   sed -e 's|\\\$\{?${param}\}?|${val}|g' "${file}.template" > "${file}"
       """
       count++
    }
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
