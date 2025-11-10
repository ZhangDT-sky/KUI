package com.example.kui.dto;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public record ChatRequest(
        List<ChatMessage> messages,
        String threadId
) {
    public record ChatMessage (
        String content,
        String role
    ){}
}