#!/usr/bin/env groovy

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "1.0.0")
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

import java.time.Instant
import java.util.Date
import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata


def call()
{
    echo 'Publish to Kafka'

    def props = [ 'bootstrap.servers': System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 'ubuntu-s3:9092,ubuntu-s4:9092,ubuntu-s1:9092',
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
