package com.example.kui.common.dto;


public record ChatRequest(
        ChatMessage messages,
        String threadId
) {
    public record ChatMessage (
        String content,
        String role
    ){}
}