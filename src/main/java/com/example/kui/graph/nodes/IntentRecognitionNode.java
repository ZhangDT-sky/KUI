package com.example.kui.graph.nodes;

import com.example.kui.agents.core.IntentAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.ChatMessageUtil;
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
public class IntentRecognitionNode implements NodeAction<WorkflowState> {

    @Autowired
    private IntentAgent intentAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private ChatMessageUtil chatMessageUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        String userMessage = messages.get(messages.size()-1).toString();
        String escapedMessages = chatMessageUtil.escapeStringContent(userMessage);
        String threadId = state.threadId()
                .orElseThrow(() -> new IllegalStateException("threadId missing"));
        String intent = intentAgent.chat(threadId, escapedMessages, promptUtil.getPrompt(PromptKey.INTENT_RECOGNIZE));
        return Map.of(WorkflowState.INTENT_RECOGNITION_KEY, intent);
    }
}
