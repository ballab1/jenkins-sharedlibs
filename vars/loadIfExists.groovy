#!/usr/bin/env groovy

def call(String groovySrc) {

    if (fileExists(groovySrc)) {
        load groovySrc
    } 
    return 0
}