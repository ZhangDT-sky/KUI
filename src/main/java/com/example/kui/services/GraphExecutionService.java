package com.example.kui.services;

import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.ChatResponse;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.memory.RedisChatMemoryStore;
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

    @Autowired
    private RedisChatMemoryStore chatMemoryStore;

    public ChatResponse chat(ChatRequest request) throws GraphStateException {
        String threadId = request.threadId();
        if (threadId == null || threadId.isEmpty()) {
            threadId = UUID.randomUUID().toString();
        }

        System.out.println("==================");
        System.out.println("threadId = " + threadId);
        System.out.println("==================");

        List<ChatMessage> messages = convertToLangchain4j(threadId,request.messages());
        Map<String,Object> initialState = Map.of(
                "messages",messages,
                "threadId",threadId
        );
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        Optional<WorkflowState> result = graph.graph(initialState,config);

        WorkflowState finalState = result.get();

        if (result.isEmpty()) {
            throw new RuntimeException("Workflow execution failed");
        }
        System.out.println(finalState.lastMessage().get());
        AiMessage lastMessage =AiMessage.from(String.valueOf(finalState.lastMessage()));
//        messages.add(lastMessage);
//        chatMemoryStore.updateMessages(threadId,messages);
        return new ChatResponse(
                lastMessage.text(),
                finalState.intentRecognition().orElse("OTHER"),
                threadId
        );
    }
    private List<ChatMessage> convertToLangchain4j(String threadId, ChatRequest.ChatMessage msg) {
        System.out.println(chatMemoryStore.getMessages(threadId));
        List<ChatMessage> chatHistory = new ArrayList<>(chatMemoryStore.getMessages(threadId));
        if (msg.role()==null || "user".equals(msg.role())) {
            chatHistory.add(UserMessage.from(msg.content()));
        } else if ("assistant".equals(msg.role())) {
            chatHistory.add(AiMessage.from(msg.content()));
        }
        return chatHistory;
    }

}
