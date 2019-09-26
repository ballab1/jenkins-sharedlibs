#!/usr/bin/env groovy

def call(ArrayList<String> hosts) {

    dir(WORKSPACE) {

        sh 'rm *.inf *.dirs ||:'
        sh ':> results.txt'
        hosts.each { host ->
            unstash host
            sh '[ ! -e "' + host + '.inf" ] || (cat "' + host + '.inf" >> results.txt)'
        }
        archiveArtifacts allowEmptyArchive: true, artifacts: 'results.txt,*.inf'
    }
}
