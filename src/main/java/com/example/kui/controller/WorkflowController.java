package com.example.kui.controller;

import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.WorkflowStreamChunk;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.memory.RedisChatMemoryStore;
import com.example.kui.services.WorkflowService;
import com.example.kui.util.ChatMessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor
public class WorkflowController {


    @Autowired
    private WorkflowService workflowService;

    @PostMapping(
            value = "/api/workflow",
            produces = MediaType.APPLICATION_NDJSON_VALUE
    )
    public ResponseBodyEmitter runWorkflowStream(@RequestBody ChatRequest request) {
        if (request == null || request.messages() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "messages cannot be null");
        }
        return workflowService.stream(request);
    }
}
