#!/usr/bin/env groovy

@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.5.18')
@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "4.0.0")

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

import java.time.Instant
import java.util.Date


def call()
{
    echo 'Publish to Kafka'

    def props = [ 'bootstrap.servers': System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 's3.ubuntu.home:9092,s7.ubuntu.home:9092,s8.ubuntu.home:9092',
                  'key.serializer': 'org.apache.kafka.common.serialization.StringSerializer',
                  'value.serializer': 'org.apache.kafka.common.serialization.StringSerializer' ]

    String topic = 'jenkins-build-report'

    def unixTimeStamp = (long) ( currentBuild.startTimeInMillis / 1000 )
    String zuluTime = Date.from(Instant.ofEpochSecond(unixTimeStamp)).format('YYYY-MM-dd HH:mm:ss') + 'Z'
    zuluTime.replace(' ', 'T')

    def data = [ 'record-id ' : UUID.randomUUID().toString(),
                 'timestamp' : zuluTime,
                 'build_url' : currentBuild.absoluteUrl,
                 'build_number' : currentBuild.number,
                 'build_cause' : currentBuild.buildCauses,
                 'display_name' : currentBuild.displayName,
                 'duration' : currentBuild.duration,
                 'project_name' : currentBuild.projectName,
                 'result' : currentBuild.result,
                 'id' : currentBuild.id,
                 'duration_string' : currentBuild.durationString - ~/ and counting/
               ]
    def message = toJSON( data )
    String key = new Random().nextLong();

    def producer = new KafkaProducer(props)
    producer.send(
        new ProducerRecord<String, String>(topic, key, message)
    )
    producer.close()
}
