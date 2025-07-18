package net.malevy.chatserver.domain.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage {
    private String id;
    private String type;
    private String message;
    private Instant timestamp;
    private String username;
    private String nodeId;

    public static ChatMessage populateFrom(ChatMessage source, String username) {
        Objects.requireNonNull(source, "source");
        ChatMessage message = new ChatMessage();
        message.type = source.type;
        message.message = source.message;
        message.timestamp = source.timestamp;
        message.id = source.id;
        message.username = Objects.requireNonNull(username, "username");

        if (!StringUtils.hasText(message.id)) {
            message.id = UUID.randomUUID().toString();
        }
        if (message.timestamp == null) {
            message.timestamp = Instant.now();
        }
        return message;
    }

    public static ChatMessage create(String messageText, String username) {
        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID().toString();
        message.type = "message";
        message.message = Objects.requireNonNull(messageText,  "messageText");
        message.username = Objects.requireNonNull(username, "username") ;
        message.timestamp = Instant.now();
        return message;
    }

    public static ChatMessage createSystemMessage(String messageText) {
        ChatMessage message = create(messageText, "system");
        message.type = "system";
        return message;
    }
}
