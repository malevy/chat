package net.malevy.chatserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class SessionManager {
    
    private final ObjectMapper mapper;
    private final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    public SessionManager(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void addSession(WebSocketSession session) {
        sessions.add(session);
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public void broadcastMessage(ChatMessage message) {
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

    public int getSessionCount() {
        return sessions.size();
    }
}