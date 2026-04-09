package com.example.kui.controller;
import com.example.kui.agents.core.*;
import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.ChatResponse;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.services.GraphExecutionService;
import com.example.kui.util.PromptUtil;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Slf4j
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

    private static final String COMMENT_HASH_KEY = "comment";

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) throws GraphStateException {
        return graphExecutionService.chat(request);
    }

    @PostMapping("liuyan")
    public void liuyan(@RequestBody String message){
        String Id = "留言";
        log.info("收到留言：{}", message);
        redisTemplate.opsForList().leftPush(Id,message);
    }

    @PostMapping("getIntent")
    public String getIntent(@RequestBody String userMessage) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("messages", new ArrayList<>(List.of(UserMessage.from(userMessage))));
        data.put("threadId", UUID.randomUUID().toString());
        
        WorkflowState state = new WorkflowState(data);
        Optional<WorkflowState> result = graph.graph(state);
        return result.map(s -> s.intentRecognition().orElse("OTHER"))
                .orElse("UNKNOWN");
    }

    @PostMapping("/search")
    public String webSearch(@RequestBody String userMessage) {
        return webSearchAgent.chat(userMessage);
    }

    @GetMapping("/comment")
    public Map<Object, Object> commentList(){
        Map<Object, Object> map = redisTemplate.opsForHash().entries(COMMENT_HASH_KEY);
        log.debug("获取评论列表: {}", map);
        return map;
    }

}
