package com.example.kui.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromptKey {
    CODE_SOLVE("prompt/code-agent.yml","prompts.solve-problem.system"),
    TEST_CASES("prompt/code-agent.yml","prompts.test-cases.system"),
    CODE_VERIFY("prompt/code-agent.yml","prompts.code-verify.system"),
    AI_CHAT("prompt/other-agent.yml","prompts.ai-chat.system"),
    INTENT_RECOGNIZE("prompt/intent-agent.yml", "prompts.recognize-intent.system"),
    KNOWLEDGE_RETRIEVAL("prompt/knowledge-agent.yml","prompts.knowledge-retrieval.system");

    private final String file;
    private final String key;
}
