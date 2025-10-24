@Grab('org.yaml:snakeyaml:2.2')
import org.yaml.snakeyaml.Yaml
import java.io.File
import hudson.FilePath

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
        def file = new File(settingsFile)
//        def yamlFile = new FilePath(file)
//        if (! yamlFile.exists()) {
//            throw new FileNotFoundException("YAML file '${settings}' not found.")
//        }
//        if (yamlFile.isDirectory()) {
//            throw new Exception("Invalid YAML file '${settings}'. Directory provided.")
//        }
        // Parse YAML
        def yaml = new Yaml()
//        return yaml.load(yamlFile.readToString())
        return yaml.load(file.text)
    }
    catch(e) {
        println "getProjectSettings: Possible issue parsing project settings file: '${settingsFile}'\n    reason: ${e.getMessage()}"
    }
    manager.addErrorBadge('Unable to local project settings')
    manager.buildFailure()
    return null
}
