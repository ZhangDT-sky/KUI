package com.example.kui.graph.nodes;

import com.example.kui.agents.core.DebugAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.graph.state.WorkflowState;
import com.example.kui.util.ChatMessageUtil;
import com.example.kui.util.PromptUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DebugNode implements NodeAction<WorkflowState> {
    @Autowired
    private DebugAgent debugAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private ChatMessageUtil chatMessageUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        String userMessage = messages.get(messages.size() - 1).toString();
        String escapedUserMessage = chatMessageUtil.escapeStringContent(userMessage);
        String threadId = state.threadId()
                .orElseThrow(()->new IllegalStateException("threadId missing"));
        String debugMessage = debugAgent.debug(threadId,escapedUserMessage,promptUtil.getPrompt(PromptKey.CODE_DEBUG));
        return Map.of(
                "messages", List.of(AiMessage.from(debugMessage))
        );
    }
}
