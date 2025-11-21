package com.example.kui.graph.nodes;

import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.streaming.WorkflowStreamRegistry;
import com.example.kui.util.ChatMessageUtil;
import com.example.kui.util.PromptUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TextualNormsNode implements NodeAction<WorkflowState> {
    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private ChatMessageUtil chatMessageUtil;

    @Autowired
    private WorkflowStreamRegistry workflowStreamRegistry;

    @Autowired
    @Qualifier("openAiStreamingChatModel")
    private StreamingChatModel streamingChatModel;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        ChatMessage lastMessage = messages.get(messages.size() - 1);
        String aiMessage = Optional.ofNullable(chatMessageUtil.extractTextFromMessage(lastMessage))
                .orElse(lastMessage.toString());
        String threadId = state.threadId().orElseThrow(() -> new IllegalStateException("threadId missing"));
        String prompt = promptUtil.getPrompt(PromptKey.TEXTUAL_NORMS);
        String intent = state.intentRecognition().orElse(null);

        String text = streamAndCollect(threadId, aiMessage, intent, prompt);

        return Map.of(
                "messages",List.of(AiMessage.from(text))
        );
    }

    //调用流式大模型（streamingChatModel），并注册响应处理器（核心逻辑）
    private String streamAndCollect(String threadId, String aiMessage, String intent, String prompt) throws Exception {
        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(List.of(
                        SystemMessage.from(prompt),
                        UserMessage.from(aiMessage)
                ))
                .build();

        streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
            private final StringBuilder buffer = new StringBuilder();
            /**
             * 回调方法1：接收大模型的"部分响应"
             * 触发时机：大模型生成一段内容后立即调用，可能触发多次
             */
            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse == null || partialResponse.isEmpty()) {
                    return;
                }
                buffer.append(partialResponse);
                workflowStreamRegistry.get(threadId)
                        .ifPresent(observer -> observer.onTextualUpdate(buffer.toString(), intent, false));
            }
            /**
             * 回调方法2：接收大模型的"完整响应"（所有增量片段均已返回，流式传输结束）
             * 触发时机：大模型生成全部内容后调用，仅触发一次
             */
            @Override
            public void onCompleteResponse(ChatResponse response) {
                workflowStreamRegistry.get(threadId)
                        .ifPresent(observer -> observer.onTextualUpdate(buffer.toString(), intent, true));
                resultFuture.complete(buffer.toString());
            }
            /**
             * 回调方法3：处理大模型调用异常（如网络中断、模型报错、参数错误等）
             * 触发时机：大模型调用过程中发生异常时调用
             * @param error 异常对象（包含错误原因）
             */
            @Override
            public void onError(Throwable error) {
                resultFuture.completeExceptionally(error);
            }
        });

        // 阻塞等待异步结果：直到onCompleteResponse或onError被调用
        // 若正常完成：返回buffer拼接的完整结果；若异常：抛出对应的异常
        return resultFuture.get();
    }
}
