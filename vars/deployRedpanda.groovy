#!/usr/bin/env groovy

def call(String target) {
    dir(target) {
         sh '''
           ./bin/updateBin.sh
           container_tag="$(sudo jq -r '.[]|select(.name == "REDPANDA: core (rpk)").version' /mnt/k8s/versions/versions.json)"
           sed -i -E -e "s|(^\s+image:.*/redpanda):.*$|\1:${container_tag}|g" docker-compose.yml
           docker-compose down
           docker-compose up
      '''
    }
}
