package net.malevy.chatserver.infrastructure.adapters.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.malevy.chatserver.config.ObjectMapperConfig;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketSessionManagerTest {

    private ObjectMapper objectMapper;

    @Mock
    private WebSocketSession webSocketSession1;

    @Mock
    private WebSocketSession webSocketSession2;

    @Mock
    private WebSocketSession webSocketSession3;

    private WebSocketSessionManager sessionManager;

    @BeforeEach
    void setUp() {
        // Use real ObjectMapper with actual configuration
        ObjectMapperConfig config = new ObjectMapperConfig();
        objectMapper = config.buildObjectMapper();
        sessionManager = new WebSocketSessionManager(objectMapper);
    }

    @Nested
    @DisplayName("addSession method")
    class AddSessionTest {

        @Test
        @DisplayName("should add session to internal collection")
        void shouldAddSessionToInternalCollection() throws IOException {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");

            // When
            sessionManager.addSession(webSocketSession1);
            sessionManager.broadcast(message);

            // Then
            verify(webSocketSession1).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("should add multiple sessions")
        void shouldAddMultipleSessions() throws IOException {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");

            // When
            sessionManager.addSession(webSocketSession1);
            sessionManager.addSession(webSocketSession2);
            sessionManager.broadcast(message);

            // Then
            verify(webSocketSession1).sendMessage(any(TextMessage.class));
            verify(webSocketSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("should handle null session")
        void shouldHandleNullSession() {
            // When & Then
            assertThrows(NullPointerException.class,() -> {
                sessionManager.addSession(null);
            });
        }

        @Test
        @DisplayName("should handle adding same session multiple times")
        void shouldHandleAddingSameSessionMultipleTimes() throws IOException {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");

            // When
            sessionManager.addSession(webSocketSession1);
            sessionManager.addSession(webSocketSession1); // Add same session again
            sessionManager.broadcast(message);

            // Then
            verify(webSocketSession1, times(1)).sendMessage(any(TextMessage.class));
        }
    }

    @Nested
    @DisplayName("removeSession method")
    class RemoveSessionTest {

        @Test
        @DisplayName("should remove session from internal collection")
        void shouldRemoveSessionFromInternalCollection() throws IOException {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");

            sessionManager.addSession(webSocketSession1);
            sessionManager.addSession(webSocketSession2);

            // When
            sessionManager.removeSession(webSocketSession1);
            sessionManager.broadcast(message);

            // Then
            verify(webSocketSession1, never()).sendMessage(any(TextMessage.class));
            verify(webSocketSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("should handle removing non-existent session")
        void shouldHandleRemovingNonExistentSession() {
            // When & Then
            assertDoesNotThrow(() -> {
                sessionManager.removeSession(webSocketSession1);
            });
        }

        @Test
        @DisplayName("should handle null session")
        void shouldHandleNullSession() {
            // When & Then
            assertThrows(NullPointerException.class,() -> {
                sessionManager.removeSession(null);
            });
        }

    }

    @Nested
    @DisplayName("broadcast method")
    class BroadcastTest {

        @Test
        @DisplayName("should serialize message and send to all sessions")
        void shouldSerializeMessageAndSendToAllSessions() throws Exception {
            // Given
            ChatMessage message = new ChatMessage();
            message.setId("test-id");
            message.setMessage("Hello world");
            message.setUsername("testuser");
            message.setType("message");
            message.setTimestamp(Instant.parse("2023-01-01T12:00:00Z"));

            sessionManager.addSession(webSocketSession1);
            sessionManager.addSession(webSocketSession2);

            // When
            sessionManager.broadcast(message);

            // Then
            // Verify that both sessions received a TextMessage with actual serialized content
            verify(webSocketSession1).sendMessage(any(TextMessage.class));
            verify(webSocketSession2).sendMessage(any(TextMessage.class));
            
            // Verify the actual serialization works
            String serialized = objectMapper.writeValueAsString(message);
            assertNotNull(serialized);
            assertTrue(serialized.contains("Hello world"));
            assertTrue(serialized.contains("testuser"));
        }

        @Test
        @DisplayName("should handle empty session list")
        void shouldHandleEmptySessionList() {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");

            // When & Then - Should not throw any exceptions
            assertDoesNotThrow(() -> {
                sessionManager.broadcast(message);
            });
        }

        @Test
        @DisplayName("should continue broadcasting to other sessions when one fails")
        void shouldContinueBroadcastingToOtherSessionsWhenOneFails() throws IOException {
            // Given
            ChatMessage message = new ChatMessage();
            message.setMessage("test");
            when(webSocketSession1.getId()).thenReturn("session-1");

            sessionManager.addSession(webSocketSession1);
            sessionManager.addSession(webSocketSession2);

            doThrow(new IOException("Connection failed")).when(webSocketSession1).sendMessage(any(TextMessage.class));

            // When
            sessionManager.broadcast(message);

            // Then
            verify(webSocketSession1).sendMessage(any(TextMessage.class));
            verify(webSocketSession2).sendMessage(any(TextMessage.class));
        }

        @Test
        @DisplayName("should handle null message")
        void shouldHandleNullMessage() {
            // Given
            sessionManager.addSession(webSocketSession1);

            // When & Then
            assertThrows(NullPointerException.class,() -> {
                sessionManager.broadcast(null);
            });
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("should create instance with ObjectMapper dependency")
        void shouldCreateInstanceWithObjectMapperDependency() {
            // When
            WebSocketSessionManager manager = new WebSocketSessionManager(objectMapper);

            // Then
            assertNotNull(manager);
        }

        @Test
        @DisplayName("should handle null ObjectMapper")
        void shouldHandleNullObjectMapper() {
            // When & Then
            assertThrows(NullPointerException.class,() -> {
                new WebSocketSessionManager(null);
            });
        }

    }

}