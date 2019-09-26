#!/usr/bin/env groovy

def call(Map pipelineParams) {

    pipeline {
        agent { label pipelineParams.agent }

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
        }
    }
}