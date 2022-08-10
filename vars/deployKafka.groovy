#!/usr/bin/env groovy

def call(String target, String container_tag) {
    dir(target) {
         sh '''
           ./bin/updateBin.sh
           org="$(pwd)"
           vers="${org}/workspace.$(basename "${org}")/.versions"
           if [ -d "$vers" ]; then
             cd "$vers"
             git pull
             cd "$org"
           fi
           CONTAINER_TAG=''' + container_tag + ''' ./deploy
      '''
    }
}
