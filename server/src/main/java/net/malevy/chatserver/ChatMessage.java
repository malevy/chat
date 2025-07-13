package net.malevy.chatserver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.Instant;
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
        ChatMessage message = new ChatMessage();
        message.type = source.type;
        message.message = source.message;
        message.timestamp = source.timestamp;
        message.id = source.id;
        message.username = username;
        if (!StringUtils.hasText(message.id)) {
            message.id = UUID.randomUUID().toString();
        }
        if (message.timestamp == null) {
            message.timestamp = Instant.now();
        }
        return message;
    }
    
    public static ChatMessage createSystemMessage(String messageText) {
        ChatMessage message = new ChatMessage();
        message.id = UUID.randomUUID().toString();
        message.type = "system";
        message.message = messageText;
        message.username = "System";
        message.timestamp = Instant.now();
        return message;
    }
}
