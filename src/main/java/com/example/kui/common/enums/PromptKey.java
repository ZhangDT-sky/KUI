package com.example.kui.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromptKey {
    CODE_SOLVE("prompt/code-agent.yml","prompts.solve-problem.system"),
    INTENT_RECOGNIZE("prompt/intent-agent.yml", "prompts.recognize-intent.system");

    private final String file;
    private final String key;
}
