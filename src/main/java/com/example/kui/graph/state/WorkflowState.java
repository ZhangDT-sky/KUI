package com.example.kui.graph.state;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class WorkflowState extends AgentState {
    public static final String MESSAGES_KEY = "messages";
    public static final String INTENT_RECOGNITION_KEY = "intentRecognition";
    public static final Map<String, Channel<?>> SCHEMA = Map.of(
            MESSAGES_KEY, Channels.appender(ArrayList::new),
            INTENT_RECOGNITION_KEY, Channels.base(() -> "")
    );

    public WorkflowState(Map<String, Object> initData) {
        super(initData);
    }

    public List<String> messages(){
        return this.<List<String>> value("messages")
                .orElse(List.of());
    }

    public String intentRecognition(){
        return this.<String> value("intentRecognition")
                .orElse("");
    }

}
