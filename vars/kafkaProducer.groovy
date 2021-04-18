#!/usr/bin/env groovy

@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.2.3')
@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "2.7.0")

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

def call(String topic, ArrayList<String> data)
{
    def props = [ 'bootstrap.servers': System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 's3.ubuntu.home:9092,s4.ubuntu.home:9092,s8.ubuntu.home:9092',
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
