#!/usr/bin/env groovy

def call(String dbName, String credsId) {
    stage (dbName) {
        withCredentials([usernamePassword(credentialsId: credsId,
                          passwordVariable: 'PWRD',
                          usernameVariable: 'USER')
            ]) {
            // dump database, and make sure there is no time info in file
            sh 'sudo docker exec -i mysql mysqldump --user ${USER} --password=${PWRD}' + " ${dbName} | grep -v '^-- Dump completed on' > ${dbName}.sql"
            stash includes: dbName+'.sql', name: dbName
        }
    }
}