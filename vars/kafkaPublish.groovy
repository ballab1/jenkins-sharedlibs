
import ie.googlielmo.publishtokafka.PublishToKafkaBuilder

def call(String topic = 'jenkins_data') {
         def kafka_servers = System.getenv('KAFKA_BOOTSTRAP_SERVERS') ?: 'ubuntu-s3:9092,ubuntu-s4:9092,ubuntu-s1:9092'
         new PublishToKafkaBuilder(kafka_servers,
                                   kafka_servers,
                                   '1', topic, true, true, 0, '', 0, 0, 0, 0, 0, true)
 //                 excludePlugin: false,
 //                 changeBuildStatus: false
 //                 ])
 //        new PublishToKafkaBuilder([
 //                 bootstrapServers: '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
 //                 metadataBrokerList: '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
 //                 acks: '1',
 //                 topic: 'jenkins_data'
 //                 excludePlugin: false,
 //                 changeBuildStatus: false
 //               ])
}