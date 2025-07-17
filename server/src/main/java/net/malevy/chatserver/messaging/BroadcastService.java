package net.malevy.chatserver.messaging;

import net.malevy.chatserver.models.ChatMessage;

public interface BroadcastService {
    void broadcastMessage(ChatMessage message);

}