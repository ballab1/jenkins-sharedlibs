#!/usr/bin/env groovy

import hudson.FilePath
import hudson.model.Result

def call(String stashName, String reportName) {
    def build = manager.build
    if (build.result == Result.SUCCESS) {

        unstash stashName
        def report = new File(reportName)
        String results = report.readToString()

        def summary = manager.createSummary('empty.gif')
        summary.appendText(results, false)
    }
}