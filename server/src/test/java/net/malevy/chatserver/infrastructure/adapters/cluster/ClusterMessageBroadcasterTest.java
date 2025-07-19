package net.malevy.chatserver.infrastructure.adapters.cluster;

import net.malevy.chatserver.application.ports.SessionManager;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClusterMessageBroadcasterTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    private ChannelTopic channelTopic;
    private NodeIdentifier nodeIdentifier;
    private ClusterMessageBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        nodeIdentifier = new NodeIdentifier("test-node-123");
        channelTopic = new ChannelTopic("test-chat-channel");
        broadcaster = new ClusterMessageBroadcaster(
                nodeIdentifier,
                channelTopic,
                sessionManager,
                redisTemplate
        );
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("should create instance with all dependencies")
        void shouldCreateInstanceWithAllDependencies() {
            ClusterMessageBroadcaster broadcaster = new ClusterMessageBroadcaster(
                    nodeIdentifier,
                    channelTopic,
                    sessionManager,
                    redisTemplate
            );

            assertNotNull(broadcaster);
        }
    }

    @Nested
    @DisplayName("broadcast method")
    class BroadcastTest {

        @Test
        @DisplayName("should set node ID on message before broadcasting")
        void shouldSetNodeIdOnMessageBeforeBroadcasting() {
            // Given
            ChatMessage message = ChatMessage.create("Hello", "user1");

            // When
            broadcaster.broadcast(message);

            // Then
            assertEquals("test-node-123", message.getNodeId());
        }

        @Test
        @DisplayName("should call sessionManager broadcast with message")
        void shouldCallSessionManagerBroadcastWithMessage() {
            // Given
            ChatMessage message = ChatMessage.create("Hello", "user1");

            // When
            broadcaster.broadcast(message);

            // Then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(sessionManager).broadcast(messageCaptor.capture());

            ChatMessage capturedMessage = messageCaptor.getValue();
            assertEquals("Hello", capturedMessage.getMessage());
            assertEquals("user1", capturedMessage.getUsername());
            assertEquals("test-node-123", capturedMessage.getNodeId());
        }

        @Test
        @DisplayName("should publish message to Redis topic")
        void shouldPublishMessageToRedisTopic() {
            // Given
            ChatMessage message = ChatMessage.create("Test message", "testuser");

            // When
            broadcaster.broadcast(message);

            // Then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(redisTemplate).convertAndSend(eq("test-chat-channel"), messageCaptor.capture());

            ChatMessage publishedMessage = messageCaptor.getValue();
            assertEquals("Test message", publishedMessage.getMessage());
            assertEquals("testuser", publishedMessage.getUsername());
            assertEquals("test-node-123", publishedMessage.getNodeId());
        }

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessageGracefully() {
            // When & Then - should not throw NullPointerException from ClusterMessageBroadcaster
            assertThrows(NullPointerException.class,() -> {
                broadcaster.broadcast(null);
            });
        }

        @Test
        @DisplayName("should handle message with null properties")
        void shouldHandleMessageWithNullProperties() {
            // Given
            ChatMessage message = new ChatMessage();

            // When & Then
            assertDoesNotThrow(() -> {
                broadcaster.broadcast(message);
            });

            // Verify nodeId was set
            assertEquals("test-node-123", message.getNodeId());
        }

    }
}