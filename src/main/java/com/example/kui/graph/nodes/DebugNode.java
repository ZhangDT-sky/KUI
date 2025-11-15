package com.example.kui.graph.nodes;

import com.example.kui.agents.DebugAgent;
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
import java.util.stream.Collectors;

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
        List<ChatMessage> allMessages = state.messages().stream()
                .skip(Math.max(0,state.messages().size()-5))
                .filter(msg -> msg instanceof ChatMessage)
                .map(chatMessageUtil::escapeMessageContent)
                .filter(msg -> msg != null)
                .toList();
        String threadId = state.threadId()
                .orElseThrow(()->new IllegalStateException("threadId missing"));
        String debugMessage = debugAgent.debug(threadId,allMessages,promptUtil.getPrompt(PromptKey.CODE_DEBUG));
        return Map.of(
                "messages",debugMessage
        );
    }
}
