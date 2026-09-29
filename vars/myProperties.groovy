#!/usr/bin/env groovy

def call()
{
    if (fileExists('build.sh')) {
        sh './build.sh -e'
    }
    else {
        sh 'build.sh -e'
    }
    Properties propvals = new Properties()
    def props = readProperties file: '.env'
    props.each { k,v ->
        def val = v.toString()
        def chr = val.substring(0, 1)
        if (chr == "'" || chr == '"') {
          if (val.length() == 2) {
              val = ''
          }
          else {
             val = val.substring(1, val.length()-2)
          }
        }
        propvals[k] = val
    }
    return propvals
}
