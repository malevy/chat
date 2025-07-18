package net.malevy.chatserver.application.usecases;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class JoinChatUseCase {
    private final SessionManager sessionManager;
    private final MessageBroadcaster messageBroadcaster;

    public JoinChatUseCase(SessionManager sessionManager, MessageBroadcaster messageBroadcaster) {
        this.sessionManager = sessionManager;
        this.messageBroadcaster = messageBroadcaster;
    }

    public void run(WebSocketSession session, String username) {
        this.sessionManager.addSession(session);
        session.getAttributes().put("username", username);
        messageBroadcaster.broadcast(ChatMessage.createSystemMessage(username + " joined the chat"));
        log.info("{} ({}) connected", username, session.getId());
    }
}
