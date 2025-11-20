package com.example.kui.graph.nodes;

import com.example.kui.agents.core.TextualNormsAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.PromptUtil;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TextualNormsNode implements NodeAction<WorkflowState> {

    @Autowired
    private TextualNormsAgent textualNormsAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        String aiMessage = String.valueOf(messages.get(messages.size()-1));
        String text = textualNormsAgent.testNorms(aiMessage,promptUtil.getPrompt(PromptKey.TEXTUAL_NORMS));
        return Map.of(
                "messages",text
        );
    }
}
