package com.example.kui.controller;
import com.example.kui.agents.CodeAgent;
import com.example.kui.agents.IntentAgent;

import com.example.kui.dto.ChatRequest;
import com.example.kui.dto.ChatResponse;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.util.PromptUtil;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/kui")
public class CodeController {

    @Autowired
    private CodeAgent codeAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private IntentAgent intentAgent;

    @Autowired
    private IntentRecognitionNode recognitionNode;

    @Autowired
    private MainWorkflowGraph graph;

    private final WorkflowState workflowState = new WorkflowState(Map.of("messages",new ArrayList<>())) ;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) throws GraphStateException {
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

    @GetMapping("getIntent")
    public String getIntent(@RequestBody String userMessage) throws Exception {
        workflowState.messages().add(UserMessage.from(userMessage));
        Optional<WorkflowState> result=graph.graph(workflowState);
        return result.toString();
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
