package com.example.kui.config;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public ChatMemory chatMemory(){
        MessageWindowChatMemory memory=MessageWindowChatMemory.builder()
                .maxMessages(20).build();
        return memory;
    }

}
