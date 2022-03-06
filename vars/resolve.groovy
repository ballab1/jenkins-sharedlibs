#!/usr/bin/env groovy

import hudson.model.Computer.ListPossibleNames

def call(String prefix, String nodeName) {
    def node = Jenkins.instance.getNode(nodeName)
    def c = node.computer.getChannel()
    if (c) {
        c.call(new ListPossibleNames()).each { it ->
            if (it.substring(0,5) == '10.3.') {
               return prefix + it
            }
        }
    }
    return prefix + nodeName
}
