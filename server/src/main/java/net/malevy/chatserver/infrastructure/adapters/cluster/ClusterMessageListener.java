package net.malevy.chatserver.infrastructure.adapters.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import net.malevy.chatserver.domain.entities.NodeIdentifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("cluster")
public
class ClusterMessageListener {
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final NodeIdentifier nodeIdentifier;
    private final ChannelTopic chatTopic;

    public ClusterMessageListener(RedisMessageListenerContainer listenerContainer,
                                  ObjectMapper objectMapper,
                                  SessionManager sessionManager,
                                  NodeIdentifier nodeIdentifier,
                                  ChannelTopic chatTopic) {
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.nodeIdentifier = nodeIdentifier;
        this.chatTopic = chatTopic;
    }

    @PostConstruct
    public void init() {
        // Subscribe to Redis messages
        listenerContainer.addMessageListener(this::onMessage, chatTopic);
    }

    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);

            // Skip messages from this node to avoid infinite loop
            if (nodeIdentifier.id().equals(chatMessage.getNodeId())) {
                log.debug("Skipping message from same node: {}", nodeIdentifier.id());
                return;
            }

            // Broadcast to local WebSocket sessions only
            sessionManager.broadcast(chatMessage);

        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }

}
