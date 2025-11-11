package com.example.kui.common.dto;

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