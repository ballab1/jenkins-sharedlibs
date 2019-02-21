#!/usr/bin/env groovy

package org.ballab1.pipeline

import ie.googlielmo.publishtokafka.PublishToKafkaBuilder



ArrayList<String> nodes = ['ubuntu-s1', 'ubuntu-s2', 'ubuntu-s3', 'ubuntu-s4']
def stepsForParallel = [:]
def script

def process(def script, Closure body) {
  this.script = script
  [ prepareStage(body), performUpdateStage(), reportMetricsStage() ]
}

def transformIntoStep(String nodeName, Closure body) {
  return {
    script.node(nodeName) {
      script.timestamps {
        script.ws {
          script.checkout scm
          body(nodeName)
        }
      }
    }
  }
}

def prepareStage(Closure body) {
  return {
    script.stage ('Prepare') {
      script.node('master') {
        nodes.each { nodeName ->
          // Into each branch we put the pipeline code we want to execute
          stepsForParallel[nodeName] = transformIntoStep(nodeName, body)
        }
      }
    }
  }
}
d
ef performUpdateStage() {
  return {
    script.stage ('Perform Update') {
      script.parallel stepsForParallel
      script.failFast: true
    }
  }
}


def reportMetricsStage() {
  return {
    script.stage ('Report Metrics') {
      script.node('master') {
        new PublishToKafkaBuilder('10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
                                  '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
                                  '1', 'jenkins_data', true, true, 0, '', 0, 0, 0, 0, 0, true)
//                 excludePlugin: false,
//                 changeBuildStatus: false
//                 ])
//        new PublishToKafkaBuilder([
//                 bootstrapServers: '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
//                 metadataBrokerList: '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
//                 acks: '1',
//                 topic: 'jenkins_data'
//                 excludePlugin: false,
//                 changeBuildStatus: false
//               ])
      }
    }
  }
}

return this
