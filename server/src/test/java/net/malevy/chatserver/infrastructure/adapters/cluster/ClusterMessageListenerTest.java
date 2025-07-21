package net.malevy.chatserver.infrastructure.adapters.cluster;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.config.ObjectMapperConfig;
import net.malevy.chatserver.domain.entities.ChatMessage;
import net.malevy.chatserver.domain.entities.NodeIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClusterMessageListenerTest {

    @Mock
    private RedisMessageListenerContainer listenerContainer;

    @Mock
    private SessionManager sessionManager;

    private ObjectMapper objectMapper;
    private NodeIdentifier nodeIdentifier;
    private ChannelTopic chatTopic;
    private ClusterMessageListener listener;

    @BeforeEach
    void setUp() {
        ObjectMapperConfig config = new ObjectMapperConfig();
        objectMapper = config.buildObjectMapper();
        nodeIdentifier = new NodeIdentifier("current-node-123");
        chatTopic = new ChannelTopic("test-chat-topic");
        
        listener = new ClusterMessageListener(
                listenerContainer,
                objectMapper,
                sessionManager,
                nodeIdentifier,
                chatTopic
        );
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("should create instance with all dependencies")
        void shouldCreateInstanceWithAllDependencies() {
            ClusterMessageListener listener = new ClusterMessageListener(
                    listenerContainer,
                    objectMapper,
                    sessionManager,
                    nodeIdentifier,
                    chatTopic
            );

            assertNotNull(listener);
        }
    }

    @Nested
    @DisplayName("onMessage method")
    class OnMessageTest {

        @Test
        @DisplayName("should deserialize message and broadcast to session manager")
        void shouldDeserializeMessageAndBroadcastToSessionManager() throws Exception {
            // Given
            ChatMessage originalMessage = ChatMessage.create("Hello cluster", "remote-user");
            originalMessage.setNodeId("remote-node-456");
            String messageJson = objectMapper.writeValueAsString(originalMessage);
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), messageJson.getBytes());

            // When
            listener.onMessage(redisMessage, null);

            // Then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(sessionManager).broadcast(messageCaptor.capture());

            ChatMessage capturedMessage = messageCaptor.getValue();
            assertEquals("Hello cluster", capturedMessage.getMessage());
            assertEquals("remote-user", capturedMessage.getUsername());
            assertEquals("remote-node-456", capturedMessage.getNodeId());
        }

        @Test
        @DisplayName("should skip messages from same node to avoid infinite loop")
        void shouldSkipMessagesFromSameNodeToAvoidInfiniteLoop() throws Exception {
            // Given
            ChatMessage sameNodeMessage = ChatMessage.create("Local message", "local-user");
            sameNodeMessage.setNodeId("current-node-123"); // Same as nodeIdentifier
            String messageJson = objectMapper.writeValueAsString(sameNodeMessage);
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), messageJson.getBytes());

            // When
            listener.onMessage(redisMessage, null);

            // Then
            verify(sessionManager, never()).broadcast(any());
        }

        @Test
        @DisplayName("should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() {
            // Given
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), "invalid json".getBytes());

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                listener.onMessage(redisMessage, null);
            });

            // Verify sessionManager was not called
            verify(sessionManager, never()).broadcast(any());
        }

        @Test
        @DisplayName("should handle empty message body")
        void shouldHandleEmptyMessageBody() {
            // Given
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), new byte[0]);

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                listener.onMessage(redisMessage, null);
            });

            // Verify sessionManager was not called
            verify(sessionManager, never()).broadcast(any());
        }

        @Test
        @DisplayName("should handle message with null nodeId")
        void shouldHandleMessageWithNullNodeId() throws Exception {
            // Given
            ChatMessage messageWithNullNodeId = ChatMessage.create("Test", "user");
            messageWithNullNodeId.setNodeId(null);
            String messageJson = objectMapper.writeValueAsString(messageWithNullNodeId);
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), messageJson.getBytes());

            // When
            listener.onMessage(redisMessage, null);

            // Then - should broadcast since null != current node id
            verify(sessionManager).broadcast(any(ChatMessage.class));
        }

        @Test
        @DisplayName("should handle sessionManager exceptions gracefully")
        void shouldHandleSessionManagerExceptionsGracefully() throws Exception {
            // Given
            ChatMessage remoteMessage = ChatMessage.create("Test", "user");
            remoteMessage.setNodeId("remote-node");
            String messageJson = objectMapper.writeValueAsString(remoteMessage);
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), messageJson.getBytes());
            doThrow(new RuntimeException("SessionManager error")).when(sessionManager).broadcast(any());

            // When & Then - should not throw exception
            assertDoesNotThrow(() -> {
                listener.onMessage(redisMessage, null);
            });
        }

        @Test
        @DisplayName("should handle messages with empty nodeId string")
        void shouldHandleMessagesWithEmptyNodeIdString() throws Exception {
            // Given
            ChatMessage messageWithEmptyNodeId = ChatMessage.create("Test", "user");
            messageWithEmptyNodeId.setNodeId("");
            String messageJson = objectMapper.writeValueAsString(messageWithEmptyNodeId);
            DefaultMessage redisMessage = new DefaultMessage("test-channel".getBytes(), messageJson.getBytes());

            // When
            listener.onMessage(redisMessage, null);

            // Then - should broadcast since "" != "current-node-123"
            verify(sessionManager).broadcast(any(ChatMessage.class));
        }

    }
}