package net.malevy.chatserver.domain.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Nested
    @DisplayName("populateFrom method")
    class PopulateFromTest {

        @Test
        @DisplayName("should populate message from source with username")
        void shouldPopulateFromSourceWithUsername() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setType("message");
            source.setMessage("Hello world");
            source.setId("test-id");
            Instant timestamp = Instant.now();
            source.setTimestamp(timestamp);
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertNotNull(result);
            assertEquals("message", result.getType());
            assertEquals("Hello world", result.getMessage());
            assertEquals("test-id", result.getId());
            assertEquals(username, result.getUsername());
            assertEquals(timestamp, result.getTimestamp());
        }

        @Test
        @DisplayName("should generate ID when source ID is null")
        void shouldGenerateIdWhenSourceIdIsNull() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setType("message");
            source.setMessage("Hello");
            source.setId(null);
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty());
        }

        @Test
        @DisplayName("should generate ID when source ID is empty")
        void shouldGenerateIdWhenSourceIdIsEmpty() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setType("message");
            source.setMessage("Hello");
            source.setId("");
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty());
        }

        @Test
        @DisplayName("should generate timestamp when source timestamp is null")
        void shouldGenerateTimestampWhenSourceTimestampIsNull() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setType("message");
            source.setMessage("Hello");
            source.setTimestamp(null);
            
            String username = "testuser";
            Instant beforeCall = Instant.now();

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertNotNull(result.getTimestamp());
            assertTrue(result.getTimestamp().isAfter(beforeCall) || result.getTimestamp().equals(beforeCall));
        }

        @Test
        @DisplayName("should throw exception when source is null")
        void shouldThrowExceptionWhenSourceIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ChatMessage.populateFrom(null, "testuser");
            });
        }

        @Test
        @DisplayName("should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setMessage("Hello");

            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ChatMessage.populateFrom(source, null);
            });
        }
    }

    @Nested
    @DisplayName("create method")
    class CreateTest {

        @Test
        @DisplayName("should create message with provided text and username")
        void shouldCreateMessageWithProvidedTextAndUsername() {
            // Given
            String messageText = "Hello world";
            String username = "testuser";
            Instant beforeCall = Instant.now();

            // When
            ChatMessage result = ChatMessage.create(messageText, username);

            // Then
            assertNotNull(result);
            assertEquals(messageText, result.getMessage());
            assertEquals(username, result.getUsername());
            assertEquals("message", result.getType());
            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty());
            assertNotNull(result.getTimestamp());
            assertTrue(result.getTimestamp().isAfter(beforeCall) || result.getTimestamp().equals(beforeCall));
        }

        @Test
        @DisplayName("should throw exception when message text is null")
        void shouldThrowExceptionWhenMessageTextIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ChatMessage.create(null, "testuser");
            });
        }

        @Test
        @DisplayName("should throw exception when username is null")
        void shouldThrowExceptionWhenUsernameIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ChatMessage.create("Hello", null);
            });
        }
    }

    @Nested
    @DisplayName("createSystemMessage method")
    class CreateSystemMessageTest {

        @Test
        @DisplayName("should create system message with provided text")
        void shouldCreateSystemMessageWithProvidedText() {
            // Given
            String messageText = "User joined the chat";
            Instant beforeCall = Instant.now();

            // When
            ChatMessage result = ChatMessage.createSystemMessage(messageText);

            // Then
            assertNotNull(result);
            assertEquals(messageText, result.getMessage());
            assertEquals("system", result.getUsername());
            assertEquals("system", result.getType());
            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty());
            assertNotNull(result.getTimestamp());
            assertTrue(result.getTimestamp().isAfter(beforeCall) || result.getTimestamp().equals(beforeCall));
        }

        @Test
        @DisplayName("should throw exception when message text is null")
        void shouldThrowExceptionWhenMessageTextIsNull() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ChatMessage.createSystemMessage(null);
            });
        }
    }

    @Nested
    @DisplayName("Basic functionality")
    class BasicFunctionalityTest {

        @Test
        @DisplayName("should create empty message with no-args constructor")
        void shouldCreateEmptyMessageWithNoArgsConstructor() {
            // When
            ChatMessage message = new ChatMessage();

            // Then
            assertNotNull(message);
            assertNull(message.getId());
            assertNull(message.getType());
            assertNull(message.getMessage());
            assertNull(message.getUsername());
            assertNull(message.getTimestamp());
            assertNull(message.getNodeId());
        }

        @Test
        @DisplayName("should set and get all properties")
        void shouldSetAndGetAllProperties() {
            // Given
            ChatMessage message = new ChatMessage();
            String id = "test-id";
            String type = "message";
            String messageText = "Hello world";
            String username = "testuser";
            Instant timestamp = Instant.now();
            String nodeId = "node-123";

            // When
            message.setId(id);
            message.setType(type);
            message.setMessage(messageText);
            message.setUsername(username);
            message.setTimestamp(timestamp);
            message.setNodeId(nodeId);

            // Then
            assertEquals(id, message.getId());
            assertEquals(type, message.getType());
            assertEquals(messageText, message.getMessage());
            assertEquals(username, message.getUsername());
            assertEquals(timestamp, message.getTimestamp());
            assertEquals(nodeId, message.getNodeId());
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCasesTest {

        @Test
        @DisplayName("should handle empty strings in populateFrom")
        void shouldHandleEmptyStringsInPopulateFrom() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setType("");
            source.setMessage("");
            source.setId("");
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertEquals("", result.getType());
            assertEquals("", result.getMessage());
            assertNotNull(result.getId());
            assertFalse(result.getId().isEmpty()); // Should generate new ID when empty
            assertEquals(username, result.getUsername());
        }

        @Test
        @DisplayName("should preserve existing valid ID in populateFrom")
        void shouldPreserveExistingValidIdInPopulateFrom() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setId("existing-id");
            source.setMessage("Hello");
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertEquals("existing-id", result.getId());
        }

        @Test
        @DisplayName("should preserve existing valid timestamp in populateFrom")
        void shouldPreserveExistingValidTimestampInPopulateFrom() {
            // Given
            ChatMessage source = new ChatMessage();
            source.setMessage("Hello");
            Instant existingTimestamp = Instant.parse("2023-01-01T00:00:00Z");
            source.setTimestamp(existingTimestamp);
            
            String username = "testuser";

            // When
            ChatMessage result = ChatMessage.populateFrom(source, username);

            // Then
            assertEquals(existingTimestamp, result.getTimestamp());
        }
    }
}