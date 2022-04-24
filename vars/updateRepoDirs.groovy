#!/usr/bin/env groovy

import groovy.json.*

def call(String dirFileSpec) {

    def jsonFile = new File(dirFileSpec)
    if (! jsonFile.exists()) {
        println 'unable to find '+jsonFile.absolutePath+'...'
        return
    }

    println 'found '+jsonFile.absolutePath

    def slurper = new JsonSlurper()
    def json = slurper.parseText(jsonFile.text)

    json.each { nodeName, dirs ->
        def out = new File("${WORKSPACE}/${nodeName}.dirs")
        if ( out.exists() ) {
            out.delete()
        }
        dirs.each { dir ->
            out << dir + '\n'
        }
    }
}
