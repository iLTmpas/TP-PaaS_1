package fr.upec.episen.paas.entrance_cockpit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.CloseStatus;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class WsService {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public void register(WebSocketSession session) {
        sessions.add(session);
    }

    public void unregister(WebSocketSession session) {
        sessions.remove(session);
    }

     public void broadcast(String message) {
        System.out.println("Broadcast : " + message);
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                } else {
                    System.out.println("Session fermée : " + session.getId());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public List<WebSocketSession> getSessions() {
        return sessions;
    }
}
