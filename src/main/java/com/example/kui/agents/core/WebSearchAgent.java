package com.example.kui.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemory = "chatMemory",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "webContentRetriever"
)
public interface WebSearchAgent {
    @SystemMessage("根据检索结果回答")
    String chat(@MemoryId String threadId, @UserMessage String message);

    @SystemMessage("根据检索结果回答")
    String chat(@UserMessage String message);
}
