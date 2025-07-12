package net.malevy.chatserver;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ChatMessage {
    private String id;
    private String type;
    private String message;
    private Instant timestamp;

    public static ChatMessage populateFrom(ChatMessage source) {
        ChatMessage message = new ChatMessage();
        message.type = source.type;
        message.message = source.message;
        message.timestamp = source.timestamp;
        message.id = source.id;
        if (!StringUtils.hasText(message.id)) {
            message.id = UUID.randomUUID().toString();
        }
        if (message.timestamp == null) {
            message.timestamp = Instant.now();
        }
        return message;
    }
}
