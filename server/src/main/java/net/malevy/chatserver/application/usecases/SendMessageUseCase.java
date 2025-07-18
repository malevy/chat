package net.malevy.chatserver.application.usecases;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

@Component
@Slf4j
public class SendMessageUseCase {
    private final MessageBroadcaster messageBroadcaster;

    public SendMessageUseCase(MessageBroadcaster messageBroadcaster) {
        this.messageBroadcaster = Objects.requireNonNull(messageBroadcaster, "messageBroadcaster");
    }

    public void run(WebSocketSession session, ChatMessage message) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(message, "message");
        final var decoratedMessage = ChatMessage.populateFrom(message, (String) session.getAttributes().get("username"));
        messageBroadcaster.broadcast(decoratedMessage);
    }
}
