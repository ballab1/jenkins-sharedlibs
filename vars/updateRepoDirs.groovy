import groovy.json.*

def call(String repoName) {

    def jsonFile = new File("${WORKSPACE}/dependants/${repoName}.json")
    if (! jsonFile.exists()) {
        println 'unable to find '+jsonFile.absolutePath+'...'
        return
    }

    manager.addInfoBadge(reponame)
    currentBuild.displayName = currentBuild.displayName + ' : ' + reponame
    println 'found '+jsonFile.absolutePath

    def slurper = new JsonSlurper()
    def json = slurper.parseText(jsonFile.text)

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
