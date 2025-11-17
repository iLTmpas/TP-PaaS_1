package fr.upec.episen.paas.entrance_cockpit.handler;

import fr.upec.episen.paas.entrance_cockpit.service.WsService;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WsHandler extends TextWebSocketHandler {

    private final WsService wsService;

    // Injection via constructeur
    public WsHandler(WsService wsService) {
        this.wsService = wsService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        wsService.register(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        wsService.unregister(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        session.sendMessage(new TextMessage("Reçu: " + msg.getPayload()));
    }
}
