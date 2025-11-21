package com.example.kui.graph.nodes;


import com.example.kui.agents.core.OtherAgent;
import com.example.kui.agents.core.WebSearchAgent;
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
public class OtherNode implements NodeAction<WorkflowState> {

    @Autowired
    private OtherAgent agent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private WebSearchAgent webSearchAgent;

    @Autowired
    private ChatMessageUtil chatMessageUtil;

    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {
        List<ChatMessage> messages = state.messages();
        String threadId = state.threadId()
                .orElseThrow(() -> new IllegalStateException("threadId missing"));
        String userMessage = messages.get(messages.size() - 1).toString();

//        String searchQuery = truncateForWebSearch(userMessage);
//        String webSearch = "  网络检索结果： " + webSearchAgent.chat(threadId, searchQuery);
        String aiMessage = agent.chat(threadId,userMessage,promptUtil.getPrompt(PromptKey.AI_CHAT));
        return Map.of(
                "messages", List.of(AiMessage.from(aiMessage))
        );
    }

    private String truncateForWebSearch(String message) {
        if (message == null) {
            return "";
        }
        int maxLength = 380; // Tavily limit is 400, keep some buffer for prompt wrappers
        if (message.length() <= maxLength) {
            return message;
        }
        log.warn("Web search query too long ({} chars). Truncating to {}.", message.length(), maxLength);
        return message.substring(0, maxLength);
    }
}
