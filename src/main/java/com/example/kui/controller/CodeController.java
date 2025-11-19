package com.example.kui.controller;
import com.example.kui.agents.core.CodeAgent;
import com.example.kui.agents.core.IntentAgent;

import com.example.kui.agents.core.WebSearchAgent;
import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.ChatResponse;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.services.GraphExecutionService;
import com.example.kui.util.PromptUtil;

import dev.langchain4j.data.message.UserMessage;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private GraphExecutionService graphExecutionService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WebSearchAgent webSearchAgent;

    @Autowired
    private MainWorkflowGraph graph;

    private final WorkflowState workflowState = new WorkflowState(Map.of("messages",new ArrayList<>())) ;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) throws GraphStateException {
        return graphExecutionService.chat(request);
    }

    @PostMapping("liuyan")
    public void liuyan(@RequestBody String message){
        String Id = "留言";
        System.out.println("留言："+message);
        redisTemplate.opsForList().leftPush(Id,message);
    }

    @GetMapping("getIntent")
    public String getIntent(@RequestBody String userMessage) throws Exception {
        workflowState.messages().add(UserMessage.from(userMessage));
        Optional<WorkflowState> result=graph.graph(workflowState);
        return result.toString();
    }

    @PostMapping("/search")
    public String webSearch(@RequestBody String userMessage) {
        return webSearchAgent.chat(userMessage);
    }
}
