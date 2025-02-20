#!/usr/bin/env groovy

def call(String groovySrc) {

    if (fileExists(groovySrc)) {
      try {
	def lines = readFile(groovySrc).split('\n')
        if (lines.size() >= 2)
           addBadge(icon: lines[0], text: lines[1])
        if (lines.size() >= 3)
           currentBuild.result = lines[2]
      }
      catch(Exception e) {
        println(e)
      }
    }
    return 0
}