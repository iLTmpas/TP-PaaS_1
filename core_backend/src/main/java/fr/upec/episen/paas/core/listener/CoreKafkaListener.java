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
    public void consume(String message) {
        try {
            System.out.println("Message Kafka reçu : " + message);

            Long badgeId = parseBadgeId(message);
            if (badgeId != null) {
                coreOperationalService.processEntrance(badgeId);
            } else {
                System.out.println("Message ignoré : format invalide");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message : " + message);
            e.printStackTrace();
        }
    }

    private Long parseBadgeId(String message) {
        try {
            // Format JSON {"badgeId":X}
            if (message.contains("\"badgeId\"")) {
                return Long.valueOf(message.replaceAll(".*\"badgeId\":(\\d+).*", "$1"));
            }
            // Format texte "Accès refusé badge=X"
            if (message.matches(".*badge=(\\d+).*")) {
                return Long.valueOf(message.replaceAll(".*badge=(\\d+).*", "$1"));
            }
            // Aucun format reconnu
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
