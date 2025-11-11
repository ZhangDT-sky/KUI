package com.example.kui.services;

import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.ChatResponse;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class GraphExecutionService {

    @Autowired
    private MainWorkflowGraph graph;

    public ChatResponse chat(ChatRequest request) throws GraphStateException {
        String threadId = request.threadId();
        if (threadId == null || threadId.isEmpty()) {
            threadId = UUID.randomUUID().toString();
        }
        List<ChatMessage> messages = convertToLangchain4j(request.messages());
        Map<String,Object> initialState = Map.of(
                "messages",messages
        );
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        Optional<WorkflowState> result = graph.graph(initialState,config);
        if (result.isEmpty()) {
            throw new RuntimeException("Workflow execution failed");
        }

        WorkflowState finalState = result.get();

        AiMessage lastMessage = finalState.lastMessage()
                .map(AiMessage.class::cast)
                .orElseThrow(() -> new RuntimeException("No AI message found"));

        return new ChatResponse(
                lastMessage.text(),
                threadId
        );
    }
    private List<ChatMessage> convertToLangchain4j(List<ChatRequest.ChatMessage> requestMessages) {
        List<ChatMessage> chatHistory = new ArrayList<>();

        for (ChatRequest.ChatMessage msg : requestMessages) {
            if ("user".equals(msg.role())) {
                chatHistory.add(UserMessage.from(msg.content()));
            } else if ("assistant".equals(msg.role())) {
                chatHistory.add(AiMessage.from(msg.content()));
            }
        }
        return chatHistory;
    }

}
