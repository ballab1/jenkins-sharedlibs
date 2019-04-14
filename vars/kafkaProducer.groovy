#!/usr/bin/env groovy

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "1.0.0")
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

def call(ArrayList<String> data)
{
    def props = [ 'bootstrap.servers': System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: '10.1.3.6:9092,10.1.3.11:9092',
                  'key.serializer': 'org.apache.kafka.common.serialization.StringSerializer',
                  'value.serializer': 'org.apache.kafka.common.serialization.StringSerializer' ]

    def producer = new KafkaProducer(props)

    def messageSender = { String topic, String message ->
        String key = new Random().nextLong()
        producer.send(
            new ProducerRecord<String, String>(topic, key, message)
            { RecordMetadata metadata, Exception e ->
                println "The offset of the record we just sent is: ${metadata.offset()}"
            } as Callback
        )
    }
    data.each { arg ->
        println "sending message $arg"
        messageSender('TutorialTopic', arg)
    }
    producer.close()
}