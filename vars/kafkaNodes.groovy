#!/usr/bin/env groovy

def call() {
    def env = System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 's3.ubuntu.home:9092,s7.ubuntu.home:9092,s8.ubuntu.home:9092',
    def nodes = []
    env.split(',').each{ it ->
        def parts = it.split(':')
        nodes += parts[0]
    }
    return nodes
}