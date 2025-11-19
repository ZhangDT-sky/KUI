package com.example.kui.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemory = "chatMemory",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever"
)
public interface KnowledgeAgent {

    @SystemMessage("{{systemPrompt}}")
    String chat(@MemoryId String memoryId, @UserMessage String message, @V("systemPrompt") String prompt);
}
