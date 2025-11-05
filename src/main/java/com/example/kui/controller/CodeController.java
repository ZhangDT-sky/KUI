package com.example.kui.controller;
import com.example.kui.agents.CodeAgent;
import com.example.kui.agents.IntentAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.util.PromptUtil;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


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

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody String userMessage){
        return codeAgent.chat(userMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
    }

    @GetMapping("getIntent")
    public String getIntent(@RequestBody String userMessage) throws Exception {
        Map<String,Object> map = new HashMap<>();
        map.put("message",userMessage);
        AgentState agentState = new AgentState(map);
        Optional<AgentState> result = graph.graph(agentState);
        return result.toString();
    }
}
