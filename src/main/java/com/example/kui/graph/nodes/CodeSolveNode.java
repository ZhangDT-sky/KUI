package com.example.kui.graph.nodes;

import com.example.kui.agents.CodeAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.ExecutorUtil;
import com.example.kui.util.PromptUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> allMessages = state.messages();
        String userMessage = allMessages.stream()
                .filter(msg -> msg instanceof dev.langchain4j.data.message.UserMessage)
                .map(msg -> ((dev.langchain4j.data.message.UserMessage) msg).toString())
                .reduce((first, second) -> second)  // 获取最后一条
                .orElseThrow(() -> new RuntimeException("No user message found"));
       String threadId = state.threadId()
            .orElseThrow(() -> new IllegalStateException("threadId missing"));
        // 使用共享线程池，无需手动关闭
        ExecutorService executorService = executorUtil.getSharedExecutor();

        CompletableFuture<String> testCasesFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.testCases(threadId,userMessage,promptUtil.getPrompt(PromptKey.TEST_CASES));
            }catch (Exception e){
                log.error("生成测试用例时发生错误",e);
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture<String> chatFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.chat(threadId,userMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
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
