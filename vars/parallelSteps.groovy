#!/usr/bin/env groovy

def call(ArrayList<String> items, Closure process, Closure action) {
    def stepsForParallel = [:]
    items.each { item_name ->
        // Into each branch we put the pipeline code we want to execute
        stepsForParallel[item_name] = process(action, item_name)
    }
//    stepsForParallel.failFast = true

    parallel stepsForParallel
}