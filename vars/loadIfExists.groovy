#!/usr/bin/env groovy

def call(String groovySrc) {

    if (fileExists(groovySrc)) {
      try {
	def lines = readFile(groovySrc).split('\n')
        addBadge(icon: lines[0], text: lines[1])
        currentBuild.result = lines[2]
      }
      catch(Exception e) {
        println(e)
      }
    }
    return 0
}