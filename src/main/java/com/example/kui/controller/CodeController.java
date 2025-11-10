package com.example.kui.controller;
import com.example.kui.agents.CodeAgent;
import com.example.kui.agents.IntentAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.dto.ChatRequest;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.util.PromptUtil;

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

    private final WorkflowState workflowState = new WorkflowState(Map.of(WorkflowState.MESSAGES_KEY,new ArrayList<>())) ;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) throws GraphStateException {
        String threadId = request.threadId();
        if (threadId == null || threadId.isEmpty()) {
            threadId = UUID.randomUUID().toString();
        }
        Map<String,Object> initialState = Map.of(
                WorkflowState.MESSAGES_KEY,request.messages()
        );
        RunnableConfig config = RunnableConfig.builder()
                .threadId(threadId)
                .build();
        Optional<WorkflowState> result = graph.graph(initialState,config);
        return result.toString();
    }

    @GetMapping("getIntent")
    public String getIntent(@RequestBody String userMessage) throws Exception {
        workflowState.messages().add(userMessage);
        Optional<WorkflowState> result=graph.graph(workflowState);
//        return result.toString();
        return result.toString();
    }

}
