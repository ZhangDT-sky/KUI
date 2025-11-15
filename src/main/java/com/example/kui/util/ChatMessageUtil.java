package com.example.kui.util;

import dev.langchain4j.data.message.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;

@Service
public class ChatMessageUtil {

    /**
     * Escapes curly braces in message content to prevent LangChain4j template parser errors.
     * Replaces {{ with {{{{ and }} with }}}}
     */
    public ChatMessage escapeMessageContent(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        String content = extractTextFromMessage(message);

        // If we can't extract content, return original message to avoid issues
        if (content == null || content.isEmpty()) {
            return message;
        }

        // Escape double curly braces to prevent template variable parsing
        // In LangChain4j templates, {{ becomes {{{{ to escape it
        String escapedContent = content.replace("{{", "{{{{").replace("}}", "}}}}");

        // Recreate message with escaped content
        try {
            if (message instanceof UserMessage) {
                return UserMessage.from(escapedContent);
            } else if (message instanceof AiMessage) {
                return AiMessage.from(escapedContent);
            } else {
                // For unknown message types, return original message
                return message;
            }
        } catch (Exception e) {
            // If message creation fails, return original message
            return message;
        }
    }

    /**
     * Extracts text content from a ChatMessage using reflection and various fallback methods
     */
    public String extractTextFromMessage(ChatMessage message) {
        if (message == null) {
            return null;
        }
        
        // Try to get contents() method which returns List<Content>
        try {
            Method contentsMethod = message.getClass().getMethod("contents");
            @SuppressWarnings("unchecked")
            List<Content> contents = (List<Content>) contentsMethod.invoke(message);
            if (contents != null && !contents.isEmpty()) {
                Content firstContent = contents.get(0);
                if (firstContent instanceof TextContent textContent) {
                    String text = textContent.text();
                    if (text != null && !text.isEmpty()) {
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            // Method doesn't exist or failed, try other approaches
        }

        // Fallback: Try to extract from string representation
        String extracted = extractContentFromString(message.toString());
        // Validate extracted content - should not be null or empty, and should not look like an object reference
        if (extracted != null && !extracted.isEmpty() && !extracted.contains("@") && extracted.length() < 1000) {
            return extracted;
        }
        
        // Last resort: return null to use original message
        return null;
    }

    /**
     * Helper method to extract text content from message string representation
     */
    public String extractContentFromString(String msgStr) {
        if (msgStr == null || msgStr.isEmpty()) {
            return null;
        }
        
        // Try to find content in various formats
        // Look for patterns like: text="..." or text='...' or just the content itself
        if (msgStr.contains("text=\"")) {
            int start = msgStr.indexOf("text=\"") + 6;
            int end = msgStr.indexOf("\"", start);
            if (end > start && end < msgStr.length()) {
                String extracted = msgStr.substring(start, end);
                // Unescape any escaped quotes
                extracted = extracted.replace("\\\"", "\"");
                return extracted;
            }
        } else if (msgStr.contains("text='")) {
            int start = msgStr.indexOf("text='") + 6;
            int end = msgStr.indexOf("'", start);
            if (end > start && end < msgStr.length()) {
                String extracted = msgStr.substring(start, end);
                // Unescape any escaped quotes
                extracted = extracted.replace("\\'", "'");
                return extracted;
            }
        } else if (msgStr.contains("text=")) {
            int start = msgStr.indexOf("text=") + 5;
            // Find the end - could be comma, closing brace, or end of string
            int end = msgStr.length();
            for (int i = start; i < msgStr.length(); i++) {
                char c = msgStr.charAt(i);
                if (c == ',' || c == '}') {
                    end = i;
                    break;
                }
            }
            if (end > start && end <= msgStr.length()) {
                String extracted = msgStr.substring(start, end).trim();
                // Remove quotes if present
                if (extracted.startsWith("\"") && extracted.endsWith("\"")) {
                    extracted = extracted.substring(1, extracted.length() - 1);
                    // Unescape any escaped quotes
                    extracted = extracted.replace("\\\"", "\"");
                } else if (extracted.startsWith("'") && extracted.endsWith("'")) {
                    extracted = extracted.substring(1, extracted.length() - 1);
                    // Unescape any escaped quotes
                    extracted = extracted.replace("\\'", "'");
                }
                return extracted;
            }
        }
        return null;
    }
}
