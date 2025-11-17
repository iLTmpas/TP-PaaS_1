package fr.upec.episen.paas.core.listener;

import fr.upec.episen.paas.core.service.CoreOperationalService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
@Service
public class CoreKafkaListener {

    private final CoreOperationalService coreOperationalService;

    public CoreKafkaListener(CoreOperationalService coreOperationalService) {
        this.coreOperationalService = coreOperationalService;
    }

    @KafkaListener(topics = "attempt_logs", groupId = "core-group")
    public void consume(String message) throws Exception {
        System.out.println("Message Kafka reçu : " + message);
        // parser le JSON pour badgeId
        Long badgeId = parseBadgeId(message);
        coreOperationalService.processEntrance(badgeId);
    }

    private Long parseBadgeId(String json) {
        return Long.valueOf(json.replaceAll(".*\"badgeId\":(\\d+).*", "$1"));
    }
}
