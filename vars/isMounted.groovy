#!/usr/bin/env groovy

def call(String mountDir) {

    boolean retval = true
    def lines = "mount | awk '{print \$3}'".execute().text
    def response = "ls ${mountDir}".execute().text

    retval
}