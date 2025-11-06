package com.example.kui.graph.nodes;

import com.example.kui.agents.CodeAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CodeDebugNode implements NodeAction<WorkflowState> {

    @Autowired
    private CodeAgent codeAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        String userMessage = state.messages().get(state.messages().size()-1);
        String aiMessage = codeAgent.chat(userMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
        return Map.of(
                WorkflowState.MESSAGES_KEY,aiMessage
        );
    }
}
