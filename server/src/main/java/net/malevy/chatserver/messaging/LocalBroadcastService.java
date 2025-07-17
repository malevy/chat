package net.malevy.chatserver.messaging;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.models.ChatMessage;
import net.malevy.chatserver.websockets.SessionManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("!cluster")
public class LocalBroadcastService implements BroadcastService {
    
    private final SessionManager sessionManager;

    public LocalBroadcastService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void broadcastMessage(ChatMessage message) {
        // Only broadcast to local sessions - no clustering
        sessionManager.broadcastToLocalSessions(message);
        log.debug("Broadcasted message locally: {}", message.getMessage());
    }
}