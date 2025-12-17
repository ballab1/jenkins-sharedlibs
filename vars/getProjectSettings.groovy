@Grab('org.yaml:snakeyaml:2.2')
import org.yaml.snakeyaml.Yaml

//--------------------------------------------------------------------------------
// The getProjectSettings.groovy file defines a function to load and parse a YAML file,
// returning the settings as a map object
//--------------------------------------------------------------------------------

def call(String settings = 'project.settings') {

    def settingsFile = settings
    // Load YAML file
    try {
        if (! fileExists(settingsFile) && fileExists('ci/' + settingsFile)) {
            settingsFile = 'ci/' + settingsFile
        }
        def file_text = readFile(settingsFile)
        // Parse YAML
        def yaml = new Yaml()
        return yaml.load(file_text)
    }
    catch(e) {
        println "getProjectSettings: Possible issue parsing project settings file: '${settingsFile}'\n    reason: ${e.getMessage()}"
    }
    manager.addErrorBadge('Unable to local project settings')
    manager.buildFailure()
    return null
}
