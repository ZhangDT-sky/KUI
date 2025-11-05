package com.example.kui.graph.nodes;

import com.example.kui.agents.IntentAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.util.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class IntentRecognitionNode implements NodeAction<AgentState> {

    @Autowired
    private IntentAgent intentAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Override
    public Map<String, Object> apply(AgentState state) {
        String intent = String.valueOf(intentAgent.chat(state.toString(),promptUtil.getPrompt(PromptKey.INTENT_RECOGNIZE)));
        log.debug("当前意图识别内容:{}",intent);
        return Map.of(
                "message", state.toString(),
                "intent",intent
        );
    }
}
