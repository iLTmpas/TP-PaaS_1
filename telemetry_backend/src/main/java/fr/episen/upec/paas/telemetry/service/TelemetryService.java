package fr.episen.upec.paas.telemetry.service;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class TelemetryService {

    private final KafkaProducer<String, String> kafkaProducer;
    private final MqttClient mqttClient;

    public TelemetryService() throws Exception {
        String kafkaServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS"); // ex: "172.31.249.132:9092"
        String mqttBrokerUrl = System.getenv("MQTT_BROKER_URL");       
        /* ----------- Kafka Producer (backend → logs) ----------- */
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", kafkaServers);
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        kafkaProducer = new KafkaProducer<>(kafkaProps);

        /* ----------- MQTT Client (IoT ↔ backend) ----------- */
        mqttClient = new MqttClient(mqttBrokerUrl, "telemetry-backend");

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("MQTT disconnected");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = message.toString();
                System.out.println("Telemetry received: " + payload);

                // Cette partie gère "Telemetry topic → Attempts logs topic"
                kafkaProducer.send(new ProducerRecord<>("attempt_logs", payload));
                System.out.println("Pushed to Kafka: attempt_logs");
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });

        mqttClient.connect();
        mqttClient.subscribe("lock/telemetry");

        System.out.println("MQTT connected and listening to lock/telemetry");
    }


    /* ----------- Méthode Control Lock (backend → IoT) ----------- */
    public void sendLockCommand(String command) throws MqttException {
        mqttClient.publish("lock/control", new MqttMessage(command.getBytes()));
        System.out.println("Command sent to lock: " + command);
    }
}
