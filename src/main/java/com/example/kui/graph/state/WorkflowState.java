package com.example.kui.graph.state;

import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class WorkflowState extends MessagesState<ChatMessage> {
    public static final String INTENT_RECOGNITION_KEY = "intentRecognition";
    public static final String THREAD_ID = "threadId";

    public WorkflowState(Map<String, Object> initData) {
        super(initData);
    }
    public WorkflowState(){
        super(Map.of());
    }
    public Optional<String> intentRecognition(){
        return value(INTENT_RECOGNITION_KEY);
    }
    public Optional<String> threadId(){return value(THREAD_ID);}

}
