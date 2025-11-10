package com.example.kui.dto;

import java.util.List;

public record ChatRequest(
        List<String> messages,
        String threadId
) {}