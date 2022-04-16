#!/usr/bin/env groovy

def call(def includeMaster = null) {
    def nodes = []
    if (includeMaster) {
        nodes += 'built-in'
    }
    Jenkins.instance.nodes.each {
        if (it.toComputer().isOnline() && !(it.name =~ 'kubernetes.*')) {
            nodes += it.name
        }
    }
    nodes
}