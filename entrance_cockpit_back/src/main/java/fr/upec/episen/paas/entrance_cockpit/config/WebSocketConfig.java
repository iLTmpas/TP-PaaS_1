package fr.upec.episen.paas.entrance_cockpit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import fr.upec.episen.paas.entrance_cockpit.handler.WsHandler;
import fr.upec.episen.paas.entrance_cockpit.service.WsService;


@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WsService wsService;

    public WebSocketConfig(WsService wsService) {
        this.wsService = wsService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WsHandler(wsService), "/ws").setAllowedOrigins("*");
    }
}
