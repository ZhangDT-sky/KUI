package com.example.kui.util;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ExecutorUtil {
    private final ExecutorService sharedExecutor = Executors.newCachedThreadPool();

    public ExecutorService init(int count){
        return Executors.newFixedThreadPool(count);
    }

    public ExecutorService getSharedExecutor() {
        return sharedExecutor;
    }

    public void shutdown(ExecutorService executorService) {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
