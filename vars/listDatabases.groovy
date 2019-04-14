
def call(String credsId) {
    stage ('list dbasbases') {
        withCredentials([usernamePassword(credentialsId: credsId,
                          passwordVariable: 'PWRD',
                          usernameVariable: 'USER')
            ]) {
            script {
                def cmd = "sudo docker exec -i mysql mysql --user ${USER} --password=${PWRD} -N -B -e 'show databases;' | grep -vE 'information_schema|sys|performance_schema|display_data|mysql:'"
                // list databases
                def result = sh returnStdout: true, script: cmd
                result.readLines()
            }
        }
    }
}