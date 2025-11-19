package com.example.kui.agents.core;

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
        chatMemoryProvider = "chatMemoryProvider"
)
public interface CodeAgent {

    @SystemMessage("{{systemPrompt}}")
    String chat(@MemoryId String memoryId, @UserMessage String message, @V("systemPrompt")  String prompt);

    @SystemMessage("{{systemPrompt}}")
    String testCases(@MemoryId String memoryId,@UserMessage String message, @V("systemPrompt")  String prompt);

    @SystemMessage("{{systemPrompt}}")
    String codeVerify(@MemoryId String memoryId,@UserMessage String message, @UserMessage String test,  @V("systemPrompt")  String prompt);
    
}
