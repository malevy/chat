package net.malevy.chatserver.infrastructure.adapters.local;

import net.malevy.chatserver.application.ports.SessionManager;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocalMessageBroadcasterTest {

    @Mock
    private SessionManager sessionManager;

    private LocalMessageBroadcaster localMessageBroadcaster;

    @BeforeEach
    void setUp() {
        localMessageBroadcaster = new LocalMessageBroadcaster(sessionManager);
    }

    @Nested
    @DisplayName("broadcast method")
    class BroadcastMethodTest {

        @Test
        @DisplayName("should delegate to sessionManager.broadcast()")
        void shouldDelegateToSessionManagerBroadcast() {
            // Given
            ChatMessage message = new ChatMessage();
            message.setId("test-id");
            message.setMessage("Hello world");

            // When
            localMessageBroadcaster.broadcast(message);

            // Then
            verify(sessionManager).broadcast(message);
        }

        @Test
        @DisplayName("should validate message is not null before delegating")
        void shouldValidateMessageIsNotNullBeforeDelegating() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                localMessageBroadcaster.broadcast(null);
            });

            // Verify sessionManager.broadcast was never called
            verifyNoInteractions(sessionManager);
        }

        @Test
        @DisplayName("should pass the exact same message instance to sessionManager")
        void shouldPassTheExactSameMessageInstanceToSessionManager() {
            // Given
            ChatMessage message = new ChatMessage();
            message.setId("test-id");
            message.setMessage("Test message");

            // When
            localMessageBroadcaster.broadcast(message);

            // Then
            verify(sessionManager).broadcast(same(message));
        }

        @Test
        @DisplayName("should handle sessionManager exceptions")
        void shouldHandleSessionManagerExceptions() {
            // Given
            ChatMessage message = new ChatMessage();
            message.setId("test-id");
            message.setMessage("Hello");

            doThrow(new RuntimeException("SessionManager error")).when(sessionManager).broadcast(message);

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                localMessageBroadcaster.broadcast(message);
            });

            // Verify the call was still made to sessionManager
            verify(sessionManager).broadcast(message);
        }

    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("should create instance with sessionManager dependency")
        void shouldCreateInstanceWithSessionManagerDependency() {
            // When
            LocalMessageBroadcaster broadcaster = new LocalMessageBroadcaster(sessionManager);

            // Then
            assertNotNull(broadcaster);
        }

    }

}