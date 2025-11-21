package com.example.kui.streaming;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowStreamRegistry {

    private final Map<String, WorkflowStreamObserver> observers = new ConcurrentHashMap<>();

    /**
     * 注册观察者：将 threadId 与观察者绑定（会话开始时调用）
     * @param threadId 会话唯一标识（如每次请求生成的UUID）
     * @param observer 该会话对应的消息接收者（如WebSocketObserver）
     */
    public void register(String threadId, WorkflowStreamObserver observer) {
        observers.put(threadId, observer);
    }

    /**
     * 注销观察者：移除 threadId 与观察者的绑定（会话结束/断开时调用）
     * @param threadId 会话唯一标识
     */
    public void unregister(String threadId) {
        observers.remove(threadId);
    }

    /**
     * 根据 threadId 查询对应的观察者（用于消息推送时定位接收者）
     * @param threadId 会话唯一标识
     * @return 包装后的观察者（Optional避免空指针）
     */
    public Optional<WorkflowStreamObserver> get(String threadId) {
        return Optional.ofNullable(observers.get(threadId));
    }
}


