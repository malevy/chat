package net.malevy.chatserver.application.usecases;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public class LeaveChatUseCase {
    private final SessionManager sessionManager;
    private final MessageBroadcaster messageBroadcaster;

    public LeaveChatUseCase(SessionManager sessionManager, MessageBroadcaster messageBroadcaster) {
        this.sessionManager = sessionManager;
        this.messageBroadcaster = messageBroadcaster;
    }

    public void run(WebSocketSession session) {
        this.sessionManager.removeSession(session);
        final String username = (String) session.getAttributes().get("username");
        messageBroadcaster.broadcast(ChatMessage.createSystemMessage(username + " left the chat"));
        log.info("{} ({}) disconnected", username, session.getId());
    }

}
