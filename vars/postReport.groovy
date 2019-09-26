#!/usr/bin/env groovy

import hudson.FilePath
import hudson.model.Result

def call(String reportName) {
    def build = manager.build
    if (build.result == Result.SUCCESS) {

        def file = new FilePath(new File(reportName))
        String results = file.readToString()

        def summary = manager.createSummary('empty.gif')
        summary.appendText(results, false)
    }
}