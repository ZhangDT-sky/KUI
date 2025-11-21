package com.example.kui.streaming;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorkflowStreamRegistry {

    private final Map<String, WorkflowStreamObserver> observers = new ConcurrentHashMap<>();

    public void register(String threadId, WorkflowStreamObserver observer) {
        observers.put(threadId, observer);
    }

    public void unregister(String threadId) {
        observers.remove(threadId);
    }

    public Optional<WorkflowStreamObserver> get(String threadId) {
        return Optional.ofNullable(observers.get(threadId));
    }
}


