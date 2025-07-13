package net.malevy.chatserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
    private final SessionManager sessionManager;
    private final BroadcastService broadcastService;

    public WebsocketConnectionHandler(ObjectMapper mapper, SessionManager sessionManager, BroadcastService broadcastService) {
        this.mapper = mapper;
        this.sessionManager = sessionManager;
        this.broadcastService = broadcastService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        String username = Objects.requireNonNullElse(getUsernameFromUri(session.getUri()), "{unknown}");
        session.getAttributes().put("username", username);
        log.info("{} ({}) connected", username, session.getId());
        sessionManager.addSession(session);

        // Send system message to all clients about user joining
        broadcastService.broadcastMessage(ChatMessage.createSystemMessage(username + " joined the chat"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        final var username = session.getAttributes().get("username");
        sessionManager.removeSession(session);
        log.info("{} ({}) disconnected", username, session.getId());
        
        // Send system message to remaining clients about user leaving
        broadcastService.broadcastMessage(ChatMessage.createSystemMessage(username + " left the chat"));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage receivedMessage) throws Exception {
        super.handleTextMessage(session, receivedMessage);
        log.info("received {} bytes from {}", receivedMessage.getPayloadLength(), session.getId());

        final ChatMessage received = mapper.readValue(receivedMessage.getPayload(), ChatMessage.class);
        final var message = ChatMessage.populateFrom(received, (String) session.getAttributes().get("username"));
        broadcastService.broadcastMessage(message);
    }

    private String getUsernameFromUri(URI uri) {
        final var components = UriComponentsBuilder.fromUri(uri).build();
        if (!components.getQueryParams().containsKey("username")) return null;
        return components.getQueryParams().get("username").getFirst();
    }
}
