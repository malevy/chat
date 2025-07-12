package net.malevy.chatserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class WebsocketConnectionHandler extends TextWebSocketHandler {

    private ObjectMapper mapper = null;

    final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    public WebsocketConnectionHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);
        String username = Objects.requireNonNullElse(getUsernameFromUri(session.getUri()), "{unknown}");
        session.getAttributes().put("username", username);
        log.info("{} ({}) connected", username, session.getId());
        
        // Send system message to all clients about user joining
        broadcastMessage(ChatMessage.createSystemMessage(username + " joined the chat"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        final var username = session.getAttributes().get("username");
        sessions.remove(session);
        log.info("{} ({}) disconnected", username, session.getId());
        
        // Send system message to remaining clients about user leaving
        broadcastMessage(ChatMessage.createSystemMessage(username + " left the chat"));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage receivedMessage) throws Exception {
        super.handleTextMessage(session, receivedMessage);
        log.info("received {} bytes from {}", receivedMessage.getPayloadLength(), session.getId());

        final ChatMessage received = mapper.readValue(receivedMessage.getPayload(), ChatMessage.class);
        final var message = ChatMessage.populateFrom(received, (String) session.getAttributes().get("username"));
        broadcastMessage(message);
    }

    private String getUsernameFromUri(URI uri) {
        final var components = UriComponentsBuilder.fromUri(uri).build();
        if (!components.getQueryParams().containsKey("username")) return null;
        return components.getQueryParams().get("username").getFirst();
    }
    
    private void broadcastMessage(ChatMessage message) {
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
