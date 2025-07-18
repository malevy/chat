package net.malevy.chatserver.application.ports;

import net.malevy.chatserver.domain.entities.ChatMessage;

public interface MessageBroadcaster {
    void  broadcast(ChatMessage message);
}
