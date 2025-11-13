package com.example.kui.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Repository
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<ChatMessage> getMessages(Object threadId) {
        String message = redisTemplate.opsForValue().get(threadId.toString());
        List<ChatMessage> messages = ChatMessageDeserializer.messagesFromJson(message);
        return messages;
    }

    public void updateMessages(Object threadId, List<ChatMessage> list) {
        String message = ChatMessageSerializer.messagesToJson(list);
        redisTemplate.opsForValue().set(threadId.toString(), message, Duration.ofDays(2));
    }

    public void deleteMessages(Object threadId) {
        redisTemplate.delete(threadId.toString());
    }
}
