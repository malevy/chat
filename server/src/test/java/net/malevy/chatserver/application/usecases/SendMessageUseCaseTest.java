package net.malevy.chatserver.application.usecases;

import net.malevy.chatserver.application.ports.MessageBroadcaster;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendMessageUseCaseTest {

    @Mock
    private MessageBroadcaster messageBroadcaster;

    @Mock
    private WebSocketSession webSocketSession;

    private SendMessageUseCase sendMessageUseCase;

    @BeforeEach
    void setUp() {
        sendMessageUseCase = new SendMessageUseCase(messageBroadcaster);
    }

    @Nested
    @DisplayName("run method")
    class RunMethodTest {

        @Test
        @DisplayName("should get username from session and broadcast populated message")
        void shouldGetUsernameFromSessionAndBroadcastPopulatedMessage() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("username", username);
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);

            ChatMessage inputMessage = new ChatMessage();
            inputMessage.setMessage("Hello world");

            // When
            sendMessageUseCase.run(webSocketSession, inputMessage);

            // Then
            verify(webSocketSession).getAttributes();
            verify(messageBroadcaster).broadcast(any(ChatMessage.class));
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTest {

        @Test
        @DisplayName("should handle message broadcaster exception")
        void shouldHandleMessageBroadcasterException() {
            // Given
            String username = "testuser";
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("username", username);
            when(webSocketSession.getAttributes()).thenReturn(sessionAttributes);

            ChatMessage inputMessage = new ChatMessage();
            inputMessage.setMessage("Hello");

            doThrow(new RuntimeException("Broadcast error")).when(messageBroadcaster).broadcast(any(ChatMessage.class));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                sendMessageUseCase.run(webSocketSession, inputMessage);
            });
        }

        @Test
        @DisplayName("should handle ChatMessage.populateFrom exceptions")
        void shouldHandleChatMessagePopulateFromExceptions() {
            // Null message should cause exception in populateFrom
            ChatMessage inputMessage = null;

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                sendMessageUseCase.run(webSocketSession, inputMessage);
            });
        }
    }

    @Nested
    @DisplayName("Null parameter handling")
    class NullParameterHandlingTest {

        @Test
        @DisplayName("should handle null WebSocketSession")
        void shouldHandleNullWebSocketSession() {
            // Given
            ChatMessage inputMessage = new ChatMessage();
            inputMessage.setMessage("Hello");

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                sendMessageUseCase.run(null, inputMessage);
            });
        }

        @Test
        @DisplayName("should handle null ChatMessage")
        void shouldHandleNullChatMessage() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                sendMessageUseCase.run(webSocketSession, null);
            });
        }
    }

}