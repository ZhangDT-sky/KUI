package com.example.kui.common.dto;

import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.ChatMessageUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.NodeOutput;

import java.time.Instant;
import java.util.Optional;

public record WorkflowStreamChunk(
        String node,
        String stage,
        String content,
        String intent,
        String threadId,
        boolean end,
        long timestamp,
        String error
) {

    public static WorkflowStreamChunk from(NodeOutput<WorkflowState> output,
                                           WorkflowState state,
                                           String threadId,
                                           ChatMessageUtil chatMessageUtil) {
        Optional<ChatMessage> lastMessage = state.lastMessage();
        String content = lastMessage
                .map(message -> extractContent(message, chatMessageUtil))
                .orElse(null);

        return new WorkflowStreamChunk(
                output.node(),
                output.node(),
                content,
                state.intentRecognition().orElse(null),
                threadId,
                output.isEND(),
                Instant.now().toEpochMilli(),
                null
        );
    }

    public static WorkflowStreamChunk error(String threadId, String errorMessage) {
        return new WorkflowStreamChunk(
                "error",
                "error",
                null,
                null,
                threadId,
                true,
                Instant.now().toEpochMilli(),
                errorMessage
        );
    }

    private static String extractContent(ChatMessage message, ChatMessageUtil chatMessageUtil) {
        if (message instanceof AiMessage aiMessage) {
            return aiMessage.text();
        }
        if (message instanceof ToolExecutionResultMessage toolMessage) {
            return toolMessage.text();
        }
        return chatMessageUtil.extractTextFromMessage(message);
    }
}

