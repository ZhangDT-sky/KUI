package com.example.kui.common.dto;

public record ChatResponse(
        String message,
        String intent,
        String threadId
) {}