
import ie.googlielmo.publishtokafka.PublishToKafkaBuilder

def call(String topic = 'jenkins_data') {
         new PublishToKafkaBuilder('10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
                                   '10.1.3.6:9092,10.1.3.10:9092,10.1.3.11:9092',
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