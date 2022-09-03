#!/usr/bin/env groovy

def call(String groovySrc) {

    if (fileExists(groovySrc)) {
      try {
        load groovySrc
      }
      catch(Exception e) {
        println(e)
      }
    } 
    return 0
}