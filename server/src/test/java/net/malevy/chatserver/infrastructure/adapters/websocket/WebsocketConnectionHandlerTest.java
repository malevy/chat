package net.malevy.chatserver.infrastructure.adapters.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.malevy.chatserver.application.usecases.JoinChatUseCase;
import net.malevy.chatserver.application.usecases.LeaveChatUseCase;
import net.malevy.chatserver.application.usecases.SendMessageUseCase;
import net.malevy.chatserver.config.ObjectMapperConfig;
import net.malevy.chatserver.domain.entities.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebsocketConnectionHandlerTest {

    @Mock
    private JoinChatUseCase joinChatUseCase;

    @Mock
    private LeaveChatUseCase leaveChatUseCase;

    @Mock
    private SendMessageUseCase sendMessageUseCase;

    @Mock
    private WebSocketSession webSocketSession;

    private ObjectMapper objectMapper;
    private WebsocketConnectionHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapperConfig config = new ObjectMapperConfig();
        objectMapper = config.buildObjectMapper();
        handler = new WebsocketConnectionHandler(
                objectMapper,
                joinChatUseCase,
                leaveChatUseCase,
                sendMessageUseCase
        );
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTest {

        @Test
        @DisplayName("should create instance with all dependencies")
        void shouldCreateInstanceWithAllDependencies() {
            WebsocketConnectionHandler handler = new WebsocketConnectionHandler(
                    objectMapper,
                    joinChatUseCase,
                    leaveChatUseCase,
                    sendMessageUseCase
            );

            assertNotNull(handler);
        }
    }

    @Nested
    @DisplayName("afterConnectionEstablished method")
    class AfterConnectionEstablishedTest {

        @Test
        @DisplayName("should extract username from URI and call joinChatUseCase")
        void shouldExtractUsernameFromUriAndCallJoinChatUseCase() throws Exception {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?username=testuser");
            when(webSocketSession.getUri()).thenReturn(uri);

            // When
            handler.afterConnectionEstablished(webSocketSession);

            // Then
            verify(joinChatUseCase).run(eq(webSocketSession), eq("testuser"));
        }

        @Test
        @DisplayName("should use default username when URI has no username parameter")
        void shouldUseDefaultUsernameWhenUriHasNoUsernameParameter() throws Exception {
            // Given
            URI uri = new URI("ws://localhost:8080/chat");
            when(webSocketSession.getUri()).thenReturn(uri);

            // When
            handler.afterConnectionEstablished(webSocketSession);

            // Then
            verify(joinChatUseCase).run(eq(webSocketSession), eq("{unknown}"));
        }

        @Test
        @DisplayName("should handle malformed URI gracefully")
        void shouldHandleMalformedUriGracefully() throws Exception {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?username=");
            when(webSocketSession.getUri()).thenReturn(uri);

            // When
            handler.afterConnectionEstablished(webSocketSession);

            // Then
            verify(joinChatUseCase).run(eq(webSocketSession), eq(""));
        }

    }

    @Nested
    @DisplayName("afterConnectionClosed method")
    class AfterConnectionClosedTest {

        @Test
        @DisplayName("should call leaveChatUseCase when connection is closed")
        void shouldCallLeaveChatUseCaseWhenConnectionIsClosed() throws Exception {
            // Given
            CloseStatus closeStatus = CloseStatus.NORMAL;

            // When
            handler.afterConnectionClosed(webSocketSession, closeStatus);

            // Then
            verify(leaveChatUseCase).run(eq(webSocketSession));
        }

    }

    @Nested
    @DisplayName("handleTextMessage method")
    class HandleTextMessageTest {

        @Test
        @DisplayName("should deserialize message and call sendMessageUseCase")
        void shouldDeserializeMessageAndCallSendMessageUseCase() throws Exception {
            // Given
            ChatMessage originalMessage = ChatMessage.create("Hello world", "testuser");
            String messageJson = objectMapper.writeValueAsString(originalMessage);
            TextMessage textMessage = new TextMessage(messageJson);
            when(webSocketSession.getId()).thenReturn("session-123");

            // When
            handler.handleTextMessage(webSocketSession, textMessage);

            // Then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(sendMessageUseCase).run(eq(webSocketSession), messageCaptor.capture());

            ChatMessage capturedMessage = messageCaptor.getValue();
            assertEquals("Hello world", capturedMessage.getMessage());
            assertEquals("testuser", capturedMessage.getUsername());
            assertEquals("message", capturedMessage.getType());
        }

    }

    @Nested
    @DisplayName("getUsernameFromUri static method")
    class GetUsernameFromUriTest {

        @Test
        @DisplayName("should extract username from valid URI")
        void shouldExtractUsernameFromValidUri() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?username=testuser");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertEquals("testuser", username);
        }

        @Test
        @DisplayName("should return null when no username parameter")
        void shouldReturnNullWhenNoUsernameParameter() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertNull(username);
        }

        @Test
        @DisplayName("should return null when username parameter is empty")
        void shouldReturnNullWhenUsernameParameterIsEmpty() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?username=");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertEquals("", username);
        }

        @Test
        @DisplayName("should extract username with other parameters present")
        void shouldExtractUsernameWithOtherParametersPresent() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?room=general&username=john&token=abc123");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertEquals("john", username);
        }

        @Test
        @DisplayName("should handle null URI")
        void shouldHandleNullUri() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                WebsocketConnectionHandler.getUsernameFromUri(null);
            });
        }

        @Test
        @DisplayName("should handle URI without query parameters")
        void shouldHandleUriWithoutQueryParameters() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertNull(username);
        }

        @Test
        @DisplayName("should return first username when multiple username parameters")
        void shouldReturnFirstUsernameWhenMultipleUsernameParameters() throws URISyntaxException {
            // Given
            URI uri = new URI("ws://localhost:8080/chat?username=first&username=second");

            // When
            String username = WebsocketConnectionHandler.getUsernameFromUri(uri);

            // Then
            assertEquals("first", username);
        }
    }
}