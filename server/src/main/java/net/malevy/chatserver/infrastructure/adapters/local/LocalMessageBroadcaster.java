package net.malevy.chatserver.infrastructure.adapters.local;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@Profile("!cluster")
public class LocalMessageBroadcaster implements MessageBroadcaster {

    private final SessionManager sessionManager;

    public LocalMessageBroadcaster(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void broadcast(ChatMessage message) {
        Objects.requireNonNull(message, "message cannot be null");
        sessionManager.broadcast(message);
        log.debug("publishing message id: {}", message.getId());
    }
}
