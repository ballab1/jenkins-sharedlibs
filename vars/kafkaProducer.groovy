#!/usr/bin/env groovy

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "1.0.0")
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

def call(String topic, ArrayList<String> data)
{
    def props = [ 'bootstrap.servers': System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 'ubuntu-s3:9092,ubuntu-s4:9092,ubuntu-s1:9092',
                  'key.serializer': 'org.apache.kafka.common.serialization.StringSerializer',
                  'value.serializer': 'org.apache.kafka.common.serialization.StringSerializer' ]

    def producer = new KafkaProducer(props)

    data.each { message ->
        String key = new Random().nextLong();
        producer.send(
            new ProducerRecord<String, String>(topic, key, message)
        )
    }
    producer.close()
}
