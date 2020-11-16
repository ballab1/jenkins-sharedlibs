#!/usr/bin/env groovy

def call(String filename) {
    File s = new File(filename)
    if (s.exists()) {
        load filename
    }
}
