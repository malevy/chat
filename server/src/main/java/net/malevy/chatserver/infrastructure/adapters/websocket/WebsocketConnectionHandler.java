package net.malevy.chatserver.infrastructure.adapters.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.usecases.JoinChatUseCase;
import net.malevy.chatserver.application.usecases.LeaveChatUseCase;
import net.malevy.chatserver.application.usecases.SendMessageUseCase;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@Slf4j
@Component
public class WebsocketConnectionHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;
    private final JoinChatUseCase joinChatUseCase;
    private final LeaveChatUseCase leaveChatUseCase;
    private final SendMessageUseCase sendMessageUseCase;

    public WebsocketConnectionHandler(
            ObjectMapper mapper,
            JoinChatUseCase joinChatUseCase,
            LeaveChatUseCase leaveChatUseCase,
            SendMessageUseCase sendMessageUseCase
    ) {
        this.mapper = mapper;
        this.joinChatUseCase = joinChatUseCase;
        this.leaveChatUseCase = leaveChatUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String username = Objects.requireNonNullElse(getUsernameFromUri(session.getUri()), "{unknown}");
        joinChatUseCase.run(session,  username);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        leaveChatUseCase.run(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage receivedMessage) throws Exception {
        super.handleTextMessage(session, receivedMessage);

        log.info("received {} bytes from {}", receivedMessage.getPayloadLength(), session.getId());

        final ChatMessage received = mapper.readValue(receivedMessage.getPayload(), ChatMessage.class);
        sendMessageUseCase.run(session,  received);
    }

    public static String getUsernameFromUri(URI uri) {
        Objects.requireNonNull(uri, "must provide a valid URI");
        final var components = UriComponentsBuilder.fromUri(uri).build();
        if (!components.getQueryParams().containsKey("username")) return null;
        return components.getQueryParams().get("username").getFirst();
    }
}
