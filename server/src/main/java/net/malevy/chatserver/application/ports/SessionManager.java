package net.malevy.chatserver.application.ports;

import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.web.socket.WebSocketSession;

public interface SessionManager {
    void addSession(WebSocketSession session);
    void removeSession(WebSocketSession session);
    void broadcast(ChatMessage message);
}
