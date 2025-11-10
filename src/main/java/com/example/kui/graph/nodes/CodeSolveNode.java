package com.example.kui.graph.nodes;

import com.example.kui.agents.CodeAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.ExecutorUtil;
import com.example.kui.util.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class CodeDebugNode implements NodeAction<WorkflowState> {

    @Autowired
    private CodeAgent codeAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private ExecutorUtil executorUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        String userMessage = state.messages().get(state.messages().size()-1);

        // 使用共享线程池，无需手动关闭
        ExecutorService executorService = executorUtil.getSharedExecutor();

        CompletableFuture<String> testCasesFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.testCases(userMessage,promptUtil.getPrompt(PromptKey.TEST_CASES));
            }catch (Exception e){
                log.error("生成测试用例时发生错误",e);
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture<String> chatFuture = CompletableFuture.supplyAsync(()->{
            try{
                return codeAgent.chat(userMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
            }catch (Exception e){
                log.error("解题错误",e);
                throw new RuntimeException(e);
            }
        },executorService);

        CompletableFuture.allOf(testCasesFuture,chatFuture).join();
        String testMessage = testCasesFuture.get();
        String aiMessage = chatFuture.get();
        String verifyMessage = codeAgent.codeVerify(aiMessage,testMessage,promptUtil.getPrompt(PromptKey.CODE_VERIFY));

        return Map.of(
                WorkflowState.MESSAGES_KEY,List.of(aiMessage,verifyMessage)
        );
    }
}
