#!/usr/bin/env groovy

def call(def includeMaster = null) {
    def nodes = []
    if (includeMaster) {
        nodes += 'master'
    }
    Jenkins.instance.nodes.each {
        if (it.toComputer().isOnline()) {
            nodes += it.name
        }
    }
    nodes
}