package net.malevy.chatserver.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.models.ChatMessage;
import net.malevy.chatserver.websockets.SessionManager;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.connection.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile("cluster")
public
class ClusterBroadcastService implements BroadcastService {

    private static final String CHAT_CHANNEL = "chat:messages";
    private final String nodeId = java.util.UUID.randomUUID().toString();

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;
    private final ChannelTopic chatTopic;

    public ClusterBroadcastService(RedisTemplate<String, Object> redisTemplate,
                                   RedisMessageListenerContainer listenerContainer,
                                   ObjectMapper objectMapper,
                                   SessionManager sessionManager) {
        this.redisTemplate = redisTemplate;
        this.listenerContainer = listenerContainer;
        this.objectMapper = objectMapper;
        this.sessionManager = sessionManager;
        this.chatTopic = new ChannelTopic(CHAT_CHANNEL);
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
            if (nodeId.equals(chatMessage.getNodeId())) {
                log.debug("Skipping message from same node: {}", nodeId);
                return;
            }

            // Broadcast to local WebSocket sessions only
            sessionManager.broadcastToLocalSessions(chatMessage);

        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }

    @Override
    public void broadcastMessage(ChatMessage message) {
        // Broadcast to local sessions
        sessionManager.broadcastToLocalSessions(message);

        // Set nodeId and publish to Redis cluster for other nodes
        try {
            message.setNodeId(nodeId);
            redisTemplate.convertAndSend(CHAT_CHANNEL, message);
            log.debug("Published message to Redis cluster with nodeId {}: {}", nodeId, message);
        } catch (Exception e) {
            log.error("Error publishing message to Redis cluster", e);
        }
    }
}
