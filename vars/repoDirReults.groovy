import groovy.json.*

def call() {

    def out = new File(WORKSPACE + '/results.txt')
    if ( out.exists() ) {
        out.delete()
    }

    def hosts = [ 'ubuntu-s1', 'ubuntu-s2', 'ubuntu-s3', 'ubuntu-s4', 'ubuntu-s5', 'ubuntu-s6' ]
    hosts.each { host ->
        unstash host
        def f = new File(WORKSPACE + '/' + host + '.inf')
        out << f.text
    }

    archiveArtifacts allowEmptyArchive: true, artifacts: 'results.txt'
}
