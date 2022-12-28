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

    Map hosts = [ "raspberry": "pi.ubuntu.home",
                  "ubuntu-s1": "s1.ubuntu.home",
                  "ubuntu-s2": "s2.ubuntu.home",
                  "ubuntu-s3": "s3.ubuntu.home",
                  "ubuntu-s4": "s4.ubuntu.home",
                  "ubuntu-s5": "s5.ubuntu.home",
                  "ubuntu-s6": "s6.ubuntu.home",
                  "ubuntu-s7": "s7.ubuntu.home",
                  "ubuntu-s8": "s8.ubuntu.home"
                ]

    return prefix + hosts[nodeName]
}
