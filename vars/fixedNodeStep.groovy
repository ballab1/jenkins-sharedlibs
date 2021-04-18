#!/usr/bin/env groovy

def call(Closure body, String item_name) {
  return {
    stage (item_name) {
      timestamps {
        ws {
          checkout scm
          body(item_name)
        }
      }
    }
  }
}