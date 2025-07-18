package net.malevy.chatserver.config;

import net.malevy.chatserver.domain.entities.NodeIdentifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeConfig {

    @Bean
    public NodeIdentifier nodeIdentifier() {
        return new NodeIdentifier(java.util.UUID.randomUUID().toString());
    }
}
