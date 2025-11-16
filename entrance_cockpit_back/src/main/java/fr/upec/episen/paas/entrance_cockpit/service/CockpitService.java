package fr.upec.episen.paas.entrance_cockpit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.List;

@Service
public class CockpitService {

    private final WsService wsService; // Service qui gère les sessions WebSocket
    private final RestTemplate restTemplate;

    public CockpitService(WsService wsService) {
        this.wsService = wsService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Autorise un badge via Core Backend et notifie tous les clients WebSocket.
     */
    public void authorizeBadgeAndNotifyFront(Long badgeId) {
        try {
            // 1️⃣ Appel POST vers Core Backend
            String url = "http://core-backend:8085/api/core/authorize/" + badgeId;
            String result = restTemplate.postForObject(url, null, String.class);
            System.out.println("URL Core Backend : " + url);

            // 2️⃣ Envoie du résultat à toutes les sessions WebSocket
            List<WebSocketSession> sessions = wsService.getSessions(); // récupère les sessions connectées
            System.out.println("Sessions WebSocket ouvertes : " + sessions.size());
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("Badge " + badgeId + ": " + result));
                    System.out.println("Message envoyé au front : " + result);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
