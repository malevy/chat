package net.malevy.chatserver.infrastructure.adapters.cluster;

import lombok.extern.slf4j.Slf4j;
import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import net.malevy.chatserver.domain.entities.NodeIdentifier;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("cluster")
public class ClusterMessageBroadcaster implements MessageBroadcaster {

    private final NodeIdentifier nodeIdentifier;
    private final ChannelTopic channelTopic;
    private final SessionManager sessionManager;
    private final RedisTemplate<String, Object> redisTemplate;

    public ClusterMessageBroadcaster(
            NodeIdentifier nodeIdentifier,
            ChannelTopic channelTopic,
            SessionManager sessionManager,
            RedisTemplate<String, Object> redisTemplate) {
        this.nodeIdentifier = nodeIdentifier;
        this.channelTopic = channelTopic;
        this.sessionManager = sessionManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void broadcast(ChatMessage message) {
        try {
            message.setNodeId(nodeIdentifier.getId());
            sessionManager.broadcast(message);
            redisTemplate.convertAndSend(channelTopic.getTopic(), message);
            log.debug("Published message to cluster from nodeId {}", nodeIdentifier.getId());
        } catch (Exception e) {
            log.error("Error publishing message to Redis cluster", e);
        }
    }
}
