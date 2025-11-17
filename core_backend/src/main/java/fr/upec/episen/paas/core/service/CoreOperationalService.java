package fr.upec.episen.paas.core.service;

import fr.upec.episen.paas.core.entity.Employe;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CoreOperationalService {

    private final KafkaProducer<String, String> kafkaProducer;
    private final RestTemplate restTemplate;

    public CoreOperationalService(KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        this.restTemplate = new RestTemplate();
    }

    public String processEntrance(Long badgeId) {
        String url = "http://172.31.250.176:8083/cache/" + badgeId;

        ResponseEntity<Employe> response = restTemplate.getForEntity(url, Employe.class);
        Employe employe = response.getBody();

        if (employe == null || !employe.isValide()) {
            kafkaProducer.send(new ProducerRecord<>("attempt_logs", "Accès refusé badge : " + badgeId));
            return "ACCESS DENIED";
        }

        kafkaProducer.send(new ProducerRecord<>("entrance_logs",
                "Entrée autorisée pour : " + employe.getNom()));
        return "ACCESS GRANTED";
    }
}
