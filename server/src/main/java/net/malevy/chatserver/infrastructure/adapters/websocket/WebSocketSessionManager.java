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
import java.util.Objects;

@Slf4j
@Component
public class WebSocketSessionManager implements SessionManager {
    
    private final ObjectMapper mapper;
    private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    public WebSocketSessionManager(ObjectMapper mapper) {

        this.mapper = Objects.requireNonNull(mapper, "mapper cannot be null") ;
    }

    @Override
    public void addSession(WebSocketSession session) {
        Objects.requireNonNull(session, "session cannot be null");
        if (sessions.contains(session)) return;
        sessions.add(session);
    }

    @Override
    public void removeSession(WebSocketSession session) {
        Objects.requireNonNull(session, "session cannot be null");
        sessions.remove(session);
    }

    @Override
    public void broadcast(ChatMessage message) {
        Objects.requireNonNull(message, "message cannot be null");
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