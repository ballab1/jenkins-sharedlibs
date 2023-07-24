#!/usr/bin/env groovy

def call(List zoo_nodes) {
    sh ':> zookeeper.report'
    zoo_nodes.each() { node ->
         sh '''
           {
              echo 'hostname: ''' + node + ''' '
              echo stat | nc ''' + node + ''' 2181
              echo
           }  | tee -a zookeeper.report
         '''
    }
    archiveArtifacts allowEmptyArchive: true, artifacts: "zookeeper.report"
}
