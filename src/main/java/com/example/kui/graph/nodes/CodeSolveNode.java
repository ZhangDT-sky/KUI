package com.example.kui.graph.nodes;

import com.example.kui.agents.CodeAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.memory.RedisChatMemoryStore;
import com.example.kui.util.ChatMessageUtil;
import com.example.kui.util.ExecutorUtil;
import com.example.kui.util.PromptUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class CodeSolveNode implements NodeAction<WorkflowState> {

    @Autowired
    private CodeAgent codeAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private ExecutorUtil executorUtil;

    @Autowired
    private ChatMessageUtil chatMessageUtil;

    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> allMessages = state.messages();
        String userMessage = allMessages.get(allMessages.size()-1).toString();
        String escapedUserMessage = chatMessageUtil.escapeStringContent(userMessage);
        String threadId = state.threadId()
            .orElseThrow(() -> new IllegalStateException("threadId missing"));

        List<ChatMessage> historyMessages = new ArrayList<>(allMessages.subList(0, allMessages.size() - 1));

        // 为两个调用准备独立的 memoryId
        String testMemoryId = threadId + "-test";
        String chatMemoryId = threadId + "-chat";

        // 将历史消息复制到两个独立的 memory 中
        if (!historyMessages.isEmpty()) {
            redisChatMemoryStore.updateMessages(testMemoryId, new ArrayList<>(historyMessages));
            redisChatMemoryStore.updateMessages(chatMemoryId, new ArrayList<>(historyMessages));
        }

        // 使用共享线程池，无需手动关闭
        ExecutorService executorService = executorUtil.getSharedExecutor();

        CompletableFuture<String> testCasesFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.testCases(testMemoryId,escapedUserMessage,promptUtil.getPrompt(PromptKey.TEST_CASES));
            }catch (Exception e){
                log.error("生成测试用例时发生错误",e);
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture<String> chatFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.chat(chatMemoryId,escapedUserMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
            }catch (Exception e){
                log.error("解题错误",e);
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture.allOf(testCasesFuture,chatFuture).join();
        String testMessage = testCasesFuture.get();
        String aiMessage = chatFuture.get();
        String verifyMessage = codeAgent.codeVerify(threadId,aiMessage,testMessage,promptUtil.getPrompt(PromptKey.CODE_VERIFY));
        // 返回 AiMessage 列表
        // MessagesState 会自动追加到现有消息列表
        List<AiMessage> responseMessages = List.of(
                AiMessage.from(verifyMessage),
                AiMessage.from(aiMessage)
        );

        return Map.of("messages", responseMessages);
    }
}
