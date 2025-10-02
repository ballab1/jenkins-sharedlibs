#!/usr/bin/env groovy

def call() {

    def git_branch = env.GIT_BRANCH
    def git_commit = env.GIT_COMMIT

    if (! git_branch || git_branch.length() == 0 || ! git_commit || git_commit.length() == 0) {
        println 'invalid environment'
        return null
    }

    def segments = git_branch.split('/')
    def branch = segments[segments.length - 1]
    def head = git_commit.substring(0, 8)

    return (branch == 'main') ? head : branch
}