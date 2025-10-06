

//--------------------------------------------------------------------------------
// The updateDependentFiles.groovy reads a project.settings file to determine
// a projects dependencies. It then updates the project files defined
//--------------------------------------------------------------------------------

def updateDependentFiles(String settingsFile = 'project.settings') {
    def project = getProjectSettings(settingsFile)

    int count = 0
    if (project?.containsKey('dependencies')){
        project.dependencies.each { param, dependencies ->
            println "param: dependencies ->  ${param}: ${dependencies}"
            updateDependencies(param, dependencies)
            count += dependencies.collectMany { it.files }.size()
        }
    }
    println "Updated ${count} dependent files"
}

//--------------------------------------------------------------------------------
private void updateDependencies(String param, List dependencies) {
    def orgParm = System.env[param]
    println "    orgParm: ${orgParm}"
    dependencies.reverseEach { dependency ->
        println "    repo: ${dependency.repo}, files: ${dependency.files}"
        if (!orgParm) updateEnvironmentParam(param, dependency.repo)
        updateFiles(param, dependency.files)
    }
}

//--------------------------------------------------------------------------------
private void updateEnvironmentParam(String param, String repo) {
    def latest = getLatest(repo)
    System.setProperty(param, latest)
}

//--------------------------------------------------------------------------------
private void updateFiles(String param, List files) {
    files.each { file ->
        try {
            def outputFile, templateFile
            templateFile = new File("${file}.template")
            if (templateFile.exists()) {
                outputFile = new File(file)
            }
            else {
                templateFile = new File("ci/${file}.template")
                if (templateFile.exists()) {
                    file = 'ci/' + file
                    outputFile = new File("${file}")
                }
                else {
                    throw new FileNotFoundException("template file '${file}' not found.")
                }
            }
            println "    updating '${file}'"

            def substVal = System.getProperty(param)
            if (substVal) {
                def substText = templateFile.text
                def subStr = '\\$\\{'+param+'\\}'
                if (substText =~ subStr) {
                    substText = substText.replaceAll(subStr, substVal)
                }
                subStr = '\\$'+param
                if (substText =~ subStr) {
                    substText = substText.replaceAll(subStr, substVal)
                }
                outputFile << substText
           }
        }
        catch(e) {
            println "Error updating '${file}', reason: ${e.msg}"
        }
    }
}
