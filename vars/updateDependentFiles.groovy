
// The dependencyInfo.groovy file generates a hash-based signature of a project environment,
// incorporating elements like project settings, Git state, Docker images, and build arguments.

def call(String settingsFile = 'ci/project.settings') {
    def project = getProjectSettings(settingsFile)

    int count = 0
    if (project?.containsKey('dependencies')){
        project.dependencies.each { param, dependencies ->
            updateDependencies(param, dependencies)
            count += dependencies.collectMany { it.files }.size()
        }
    }
    println "Updated ${count} dependent files"
}

//--------------------------------------------------------------------------------
private String getLatest(String repo = '') {

    def cmd = repo?.trim() ? "git ls-remote ${repo} HEAD" : 'git rev-parse HEAD'
    def commitHash = execute(cmd)
    return commitHash?.take(10)
}

//--------------------------------------------------------------------------------
private void updateDependencies(String param, List dependencies) {
    def orgParm = env[param]
    dependencies.reverseEach { dependency ->
        if (!orgParm) updateEnvironmentParam(param, dependency.repo)
        updateFiles(param, dependency.files)
    }
}

//--------------------------------------------------------------------------------
private void updateEnvironmentParam(String param, String repo) {
    def latest = getLatest(repo)
    env[param] = latest
}

//--------------------------------------------------------------------------------
private void updateFiles(String param, List files) {
    files.each { file ->
        try {
            println "updating '${file}'"
            def templateFile = new File("${file}.template")
            def outputFile = new File(file)
            def substituted = templateFile.text.replaceAll("\\${param}", System.getenv(param))
            outputFile.text = substituted
        }
        catch(e) {
            println "Error updating '${file}'\nreason: ${e.msg}"
        }
    }
}
//--------------------------------------------------------------------------------
