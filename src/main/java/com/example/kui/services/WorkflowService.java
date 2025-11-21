package com.example.kui.services;

import com.example.kui.common.dto.ChatRequest;
import com.example.kui.common.dto.WorkflowStreamChunk;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.graph.workflows.MainWorkflowGraph;
import com.example.kui.memory.RedisChatMemoryStore;
import com.example.kui.streaming.WorkflowStreamObserver;
import com.example.kui.streaming.WorkflowStreamRegistry;
import com.example.kui.util.ChatMessageUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.RunnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class WorkflowService {

    @Autowired
    private MainWorkflowGraph workflowGraph;
    @Autowired
    private ChatMessageUtil chatMessageUtil;
    @Autowired
    private RedisChatMemoryStore chatMemoryStore;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WorkflowStreamRegistry workflowStreamRegistry;

    private static final String TEXTUAL_NODE = "TextualNormsNode";

    public ResponseBodyEmitter stream(ChatRequest request) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);
        CompletableFuture.runAsync(() -> executeWorkflow(request, emitter));
        return emitter;
    }

    private void executeWorkflow(ChatRequest request, ResponseBodyEmitter emitter) {
        String threadId = ensureThreadId(request.threadId());
        workflowStreamRegistry.register(threadId, createTextualObserver(threadId, emitter));
        try {
            List<ChatMessage> chatHistory = buildChatHistory(threadId, request.messages());
            Map<String, Object> initialState = Map.of(
                    "messages", chatHistory,
                    "threadId", threadId
            );

            RunnableConfig config = RunnableConfig.builder()
                    .threadId(threadId)
                    .build();

            AsyncGenerator<NodeOutput<WorkflowState>> generator = workflowGraph.stream(initialState, config);
            Iterator<NodeOutput<WorkflowState>> iterator = generator.iterator();
            while (iterator.hasNext()) {
                NodeOutput<WorkflowState> output = iterator.next();
                WorkflowState state = output.state();
                if (state == null) {
                    continue;
                }
                WorkflowStreamChunk chunk = WorkflowStreamChunk.from(output, state, threadId, chatMessageUtil);
                if (TEXTUAL_NODE.equals(chunk.stage())) {
                    continue;
                }
                if (!emitChunk(emitter, chunk)) {
                    log.warn("Emitter completed, stop streaming for threadId={}", threadId);
                    break;
                }

                if (output.isEND()) {
                    break;
                }
            }
            emitter.complete();
        } catch (Exception ex) {
            log.error("Workflow streaming failed", ex);
            handleStreamingError(emitter, threadId, ex);
        } finally {
            workflowStreamRegistry.unregister(threadId);
        }
    }

    private List<ChatMessage> buildChatHistory(String threadId, ChatRequest.ChatMessage msg) {
        List<ChatMessage> chatHistory = new ArrayList<>();
        List<ChatMessage> stored = chatMemoryStore.getMessages(threadId);
        if (stored != null) {
            chatHistory.addAll(stored);
        }

        if (msg == null) {
            return chatHistory;
        }

        String role = Optional.ofNullable(msg.role()).orElse("user");
        if ("user".equalsIgnoreCase(role)) {
            chatHistory.add(UserMessage.from(msg.content()));
        } else if ("assistant".equalsIgnoreCase(role)) {
            chatHistory.add(AiMessage.from(msg.content()));
        }
        return chatHistory;
    }

    private String ensureThreadId(String threadId) {
        if (threadId == null || threadId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return threadId;
    }

    private boolean emitChunk(ResponseBodyEmitter emitter, WorkflowStreamChunk chunk) throws IOException {
        try {
            emitter.send(objectMapper.writeValueAsString(chunk) + "\n");
            return true;
        } catch (IllegalStateException ex) {
            // client disconnected or emitter already completed
            return false;
        }
    }

    private void handleStreamingError(ResponseBodyEmitter emitter, String threadId, Exception ex) {
        try {
            WorkflowStreamChunk errorChunk = WorkflowStreamChunk.error(threadId, ex.getMessage());
            emitChunk(emitter, errorChunk);
        } catch (IOException ignored) {
        } finally {
            emitter.completeWithError(ex);
        }
    }

    private WorkflowStreamObserver createTextualObserver(String threadId, ResponseBodyEmitter emitter) {
        // 返回 WorkflowStreamObserver 接口实现（Lambda 表达式）
        return (content, intent, end) -> {
            try {
                var chunk = new WorkflowStreamChunk(
                        TEXTUAL_NODE,
                        TEXTUAL_NODE,
                        content,
                        intent,
                        threadId,
                        end,
                        Instant.now().toEpochMilli(),
                        null
                );
                emitChunk(emitter, chunk);
            } catch (IOException ignored) {
            }
        };
    }
}

