#!/usr/bin/env groovy

import groovy.json.*

def call(String dirFileSpec) {

    def jsonFile = new File("${WORKSPACE}/dependants/${dirFileSpec}.json")
    if (! jsonFile.exists()) {
        println 'unable to find '+jsonFile.absolutePath+'...'
        sh '''
              pwd
              ls -al
           '''
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
