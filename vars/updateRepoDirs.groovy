@Grab('org.yaml:snakeyaml:2.2')
import org.yaml.snakeyaml.Yaml

import groovy.json.*

def call(String dirFileSpec) {

    if (! fileExists(dirFileSpec)) {
        println 'updateRepoDirs: unable to find ' + dirFileSpec.absolutePath + '...'
        return
    }

    try {
        def file_text = readFile(dirFileSpec)

        println 'found ' + dirFileSpec.absolutePath

        // Parse YAML
        def yaml = new Yaml()
        def json = yaml.load(file_text)

        json.each { nodeName, dirs ->
            def out = new File("${WORKSPACE}/${nodeName}.dirs")
            if ( out.exists() ) {
               out.delete()
            }
            dirs.each { dir ->
               out << dir + '\n'
            }
        }
    }
    catch(e) {
        println 'updateRepoDirs: unable to parse ' + dirFileSpec.absolutePath + '...\n' + "    reason: ${e.getMessage()}"
    }
}
