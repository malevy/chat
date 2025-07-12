package net.malevy.chatserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.malevy.chatserver.WebsocketConnectionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ObjectMapper mapper;

    public WebSocketConfig(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebsocketConnectionHandler(mapper), "/chat").setAllowedOrigins("*");
    }


}
