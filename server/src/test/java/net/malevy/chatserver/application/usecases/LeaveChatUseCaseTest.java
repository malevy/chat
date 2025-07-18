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
class LeaveChatUseCaseTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private MessageBroadcaster messageBroadcaster;

    @Mock
    private WebSocketSession webSocketSession;

    private LeaveChatUseCase leaveChatUseCase;

    @BeforeEach
    void setUp() {
        leaveChatUseCase = new LeaveChatUseCase(sessionManager, messageBroadcaster);
    }

    @Nested
    @DisplayName("run method")
    class RunMethodTest {

        @Test
        @DisplayName("should remove session from session manager")
        void shouldRemoveSessionFromSessionManager() {
            // Given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("username", "testuser");
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            // When
            leaveChatUseCase.run(webSocketSession);

            // Then
            verify(sessionManager).removeSession(webSocketSession);
        }

        @Test
        @DisplayName("should broadcast system message about user leaving")
        void shouldBroadcastSystemMessageAboutUserLeaving() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("username", username);
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

            // When
            leaveChatUseCase.run(webSocketSession);

            // Then
            verify(messageBroadcaster).broadcast(messageCaptor.capture());
            
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertNotNull(capturedMessage);
            assertEquals("system", capturedMessage.getType());
            assertEquals("system", capturedMessage.getUsername());
            assertEquals(username + " left the chat", capturedMessage.getMessage());
            assertNotNull(capturedMessage.getId());
            assertNotNull(capturedMessage.getTimestamp());
        }

        @Test
        @DisplayName("should handle missing username in session attributes")
        void shouldHandleMissingUsernameInSessionAttributes() {
            // Given
            Map<String, Object> sessionAttributes = new HashMap<>();
            // No username key in attributes
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);
            when(webSocketSession.getId()).thenReturn("session-123");

            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);

            // When
            leaveChatUseCase.run(webSocketSession);

            // Then
            verify(sessionManager).removeSession(webSocketSession);
            
            verify(messageBroadcaster).broadcast(messageCaptor.capture());
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertEquals("null left the chat", capturedMessage.getMessage());
        }

    }


    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("should handle session manager exception")
        void shouldHandleSessionManagerException() {
            // Given
            doThrow(new RuntimeException("Session manager error")).when(sessionManager).removeSession(webSocketSession);

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                leaveChatUseCase.run(webSocketSession);
            });
        }

        @Test
        @DisplayName("should handle message broadcaster exception")
        void shouldHandleMessageBroadcasterException() {
            // Given
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("username", "testuser");
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);

            doThrow(new RuntimeException("Broadcast error")).when(messageBroadcaster).broadcast(any(ChatMessage.class));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                leaveChatUseCase.run(webSocketSession);
            });
            
            // Verify session was still removed before the exception
            verify(sessionManager).removeSession(webSocketSession);
        }

    }

    @Nested
    @DisplayName("Null parameter handling")
    class NullParameterHandlingTest {

        @Test
        @DisplayName("should handle null WebSocketSession")
        void shouldHandleNullWebSocketSession() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                leaveChatUseCase.run(null);
            });
        }

        @Test
        @DisplayName("should handle null session attributes")
        void shouldHandleNullSessionAttributes() {
            // Given
            when(webSocketSession.getAttributes()).thenReturn(null);

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                leaveChatUseCase.run(webSocketSession);
            });
            
            // Verify session was still removed before the exception
            verify(sessionManager).removeSession(webSocketSession);
        }
    }
}