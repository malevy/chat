package net.malevy.chatserver.infrastructure.adapters.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class WebSocketSessionManager implements SessionManager {
    
    private final ObjectMapper mapper;
    private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    public WebSocketSessionManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    @Override
    public void broadcast(ChatMessage message) {
        try {
            TextMessage textMessage = new TextMessage(mapper.writeValueAsString(message));
            for (WebSocketSession session : sessions) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Failed to send message to {}", session.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to serialize message", e);
        }
    }

}