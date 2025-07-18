package net.malevy.chatserver.application.usecases;

import net.malevy.chatserver.application.ports.MessageBroadcaster;
import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JoinChatUseCaseTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private MessageBroadcaster messageBroadcaster;

    @Mock
    private WebSocketSession webSocketSession;

    private JoinChatUseCase joinChatUseCase;

    @BeforeEach
    void setUp() {
        joinChatUseCase = new JoinChatUseCase(sessionManager, messageBroadcaster);
    }

    @Nested
    @DisplayName("run method")
    class RunMethodTest {

        @Test
        @DisplayName("should add session to session manager")
        void shouldAddSessionToSessionManager() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            // When
            joinChatUseCase.run(webSocketSession, username);

            // Then
            verify(sessionManager).addSession(webSocketSession);
        }

        @Test
        @DisplayName("should set username in session attributes")
        void shouldSetUsernameInSessionAttributes() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            // When
            joinChatUseCase.run(webSocketSession, username);

            // Then
            assertEquals(username, sessionAttributes.get("username"));
        }

        @Test
        @DisplayName("should broadcast system message about user joining")
        void shouldBroadcastSystemMessageAboutUserJoining() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

            // When
            joinChatUseCase.run(webSocketSession, username);

            // Then
            verify(messageBroadcaster).broadcast(messageCaptor.capture());
            
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);
            assertEquals("system", capturedMessage.getType());
            assertEquals("system", capturedMessage.getUsername());
            assertEquals(username + " joined the chat", capturedMessage.getMessage());
            assertNotNull(capturedMessage.getId());
            assertNotNull(capturedMessage.getTimestamp());
        }

    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("should handle session manager exception")
        void shouldHandleSessionManagerException() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();

            doThrow(new RuntimeException("Session manager error")).when(sessionManager).addSession(webSocketSession);

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                joinChatUseCase.run(webSocketSession, username);
            });
        }

        @Test
        @DisplayName("should handle message broadcaster exception")
        void shouldHandleMessageBroadcasterException() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);

            doThrow(new RuntimeException("Broadcast error")).when(messageBroadcaster).broadcast(any(ChatMessage.class));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                joinChatUseCase.run(webSocketSession, username);
            });
            
            // Verify session was still added before the exception
            verify(sessionManager).addSession(webSocketSession);
        }
    }

    @Nested
    @DisplayName("Null parameter handling")
    class NullParameterHandlingTest {

        @Test
        @DisplayName("should handle null WebSocketSession")
        void shouldHandleNullWebSocketSession() {
            // Given
            String username = "testuser";

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                joinChatUseCase.run(null, username);
            });
        }

        @Test
        @DisplayName("should handle null username")
        void shouldHandleNullUsername() {
            // Given
            Map<String, Object> sessionAttributes = new HashMap<>();
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

            // When
            joinChatUseCase.run(webSocketSession, null);

            // Then
            verify(sessionManager).addSession(webSocketSession);
            assertNull(sessionAttributes.get("username"));
            
            verify(messageBroadcaster).broadcast(messageCaptor.capture());
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertEquals("null joined the chat", capturedMessage.getMessage());
        }
    }
}