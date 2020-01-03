#!/usr/bin/env groovy

def call() {
    def env = System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 'ubuntu-s3:9092,ubuntu-s4:9092',
    def nodes = []
    env.split(',').each{ it ->
        def parts = it.split(':')
        nodes += parts[0]
    }
    return nodes
}