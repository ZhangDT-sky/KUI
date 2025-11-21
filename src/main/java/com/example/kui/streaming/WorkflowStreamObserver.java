package com.example.kui.streaming;
/**
 * 接收流式更新的消息回调方法
 *  content 实时推送的消息内容（如大模型返回的部分响应、完整响应）
 *  intent 消息意图标识（如“文本规范化”“工具调用”，用于区分消息类型）
 *  end 是否为最终消息（true=流式传输结束，false=中间增量消息）
 */
public interface WorkflowStreamObserver {
    void onTextualUpdate(String content, String intent, boolean end);
}

