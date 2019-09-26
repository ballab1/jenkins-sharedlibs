#!/usr/bin/env groovy

import hudson.model.Result

def call(int wait = 10) {
    wait = wait > 10 ? wait : 10
    def build = manager.build
    def project = build.project
    if (build.result != Result.SUCCESS)   {
        build.keepLog(true)
    }
    println 'rescheduling ' + project.displayName
    project.scheduleBuild(wait, new Cause.RemoteCause('immediate','reschedule'))
}
