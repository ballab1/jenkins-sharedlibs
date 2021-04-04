#!/usr/bin/env groovy

def call(Map pipelineParams) {

    pipeline {
        agent { label pipelineParams.agent }
        options {
            buildDiscarder(logRotator(numToKeepStr: '20'))
            disableConcurrentBuilds()
            disableResume()
            timestamps()
        } 
        stages {
            stage ('Process Items') {
                steps {
                    parallelSteps(pipelineParams.items, pipelineParams.process, pipelineParams.action)
                }
            }
        }
        post {
            always {
                kafkaBuildReporter()
            }
            cleanup {
                deleteDir()
            }
        }
    }
}