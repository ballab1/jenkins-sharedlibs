#!/usr/bin/env groovy

def call() {
  String jobName = env.JOB_NAME;
  if (jobName) {
    idx = jobName.lastIndexOf('/')
    return (idx > 0) ? jobName.substring(idx + 1) : jobname
  }
}
