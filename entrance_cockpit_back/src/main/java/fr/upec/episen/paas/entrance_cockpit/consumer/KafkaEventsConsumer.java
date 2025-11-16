package fr.upec.episen.paas.entrance_cockpit.kafka;

import fr.upec.episen.paas.entrance_cockpit.service.WsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaEventsConsumer {

    private final WsService wsService;

    public KafkaEventsConsumer(WsService wsService) {
        this.wsService = wsService;
    }

    @KafkaListener(topics = "attempt_logs")
    public void onAttemptLog(String message) {
        System.out.println("Attempt Log reçu : " + message);

        // ESCAPE du message pour être sûr que c'est un JSON valide
        String safePayload = message.replace("\"", "\\\"");

        String json = "{\"type\":\"attempt_log\", \"payload\":\"" + safePayload + "\"}";
        wsService.broadcast(json);
    }

    @KafkaListener(topics = "entrance_logs")
    public void onEntranceLog(String message) {
        System.out.println("Entrance Log reçu : " + message);
        wsService.broadcast("{\"type\":\"entrance_log\", \"payload\":\"" + message + "\"}");
    }


}
