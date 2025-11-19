package com.example.kui.agents.tools;


import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CommentTool {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String COMMENT_HASH_KEY = "comment";

    @Tool("保存用户留言，需传入用户名（username）和留言内容（comment），基于当前会话隔离")
    public void comment(
            @ToolMemoryId String memoryId,
            @P("留言所属的用户名，1-5字，不可重复（当前会话内）") String username,
            @P("用户的具体留言内容，不能为空") String comment) {
        redisTemplate.opsForHash().put(COMMENT_HASH_KEY, username, comment);
    }

    @Tool("获取当前会话已存在的用户名列表，用于检查新用户名是否重复")
    public Set<String> getExistingUsernames(@ToolMemoryId String memoryId) {
        Set<Object> keys = redisTemplate.opsForHash().keys(COMMENT_HASH_KEY);
        if (keys == null || keys.isEmpty()) {
            return Collections.emptySet();
        }
        return keys.stream().map(Object::toString).collect(Collectors.toSet());
    }

}
