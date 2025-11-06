package com.example.kui.agents;

import com.example.kui.common.enums.PromptKey;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemory = "chatMemory"
)
public interface CodeAgent {

    @SystemMessage("{{systemPrompt}}")
    String chat(@UserMessage String message, @V("systemPrompt")  String prompt);
}
