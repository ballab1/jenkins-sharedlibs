#!/usr/bin/env groovy

def call(String who, String where) {
  Jenkins.instance.nodes.each {
    if (it.toComputer().isOnline()) {
      sh "ssh ${who}@${it.name} 'rm -rf ${where}/workspace/*'"
    }
  }
}