#!/usr/bin/env groovy

def call(String wdir, String cmd) {

  dir(wdir){
    sh './updateBin.sh'
    sh cmd
  }
}
