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
        def yamlFile = new File(settingsFile)
        if (! yamlFile.exists()) {
            throw new FileNotFoundException("YAML file '${settings}' not found.")
        }
        if (yamlFile.isDirectory()) {
            throw new Exception("Invalid YAML file '${settings}'. Directory provided.")
        }

        // Parse YAML
        def yaml = new Yaml()
        return yaml.load(yamlFile.text)
    }
    catch(e) {
        println "unable to open project settings file: '${settings}'\nreason: ${e.getMessage()}"
        return null
    }
    println "Possible issue parsing project settings file: '${settingsFile}'"
    return null
}
