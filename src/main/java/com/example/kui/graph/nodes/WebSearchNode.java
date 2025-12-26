package com.example.kui.graph.nodes;

import com.example.kui.agents.core.WebSearchAgent;
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
public class WebSearchNode implements NodeAction<WorkflowState> {

    @Autowired
    private PromptUtil promptUtil;
    @Autowired
    private WebSearchAgent webSearchAgent;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        String threadId = state.threadId()
                .orElseThrow(() -> new IllegalStateException("threadId missing"));
        String userMessage = messages.get(messages.size() - 1).toString();
        String searchMessage = webSearchAgent.chat(threadId, userMessage);
        return Map.of("messages","网络搜索结果："+searchMessage);
    }
}
