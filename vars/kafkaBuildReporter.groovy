#!/usr/bin/env groovy

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "1.0.0")
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

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

    String topic = currentBuild.projectName.toLowerCase()
    topic = topic.replaceAll("'", '')
    topic = topic.replaceAll('\\s', '_')
    topic += '_build_report'

    def data = [ 'absolute_url' : currentBuild.absoluteUrl,
                 'build_cause' : currentBuild.buildCauses,
                 'build_number' : currentBuild.number,
                 'display_name' : currentBuild.displayName,
                 'duration' : currentBuild.duration,
                 'duration_string' : currentBuild.durationString,
                 'id' : currentBuild.id,
                 'project_name' : currentBuild.projectName,
                 'result' : currentBuild.result,
                 'start_time' : currentBuild.startTimeInMillis
               ]
    def message = toJSON( data )
    String key = new Random().nextLong();

    def producer = new KafkaProducer(props)
    producer.send(
        new ProducerRecord<String, String>(topic, key, message)
    )
    producer.close()
}
