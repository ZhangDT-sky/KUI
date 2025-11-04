package com.example.kui.controller;
import com.example.kui.agents.CodeAgent;
import com.example.kui.agents.IntentAgent;
import com.example.kui.common.enums.PromptKey;
import com.example.kui.util.PromptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/kui")
public class CodeController {

    @Autowired
    private CodeAgent codeAgent;

    @Autowired
    private PromptUtil promptUtil;

    @Autowired
    private IntentAgent intentAgent;

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody String userMessage){
        return codeAgent.chat(userMessage,promptUtil.getPrompt(PromptKey.CODE_SOLVE));
    }

    @GetMapping("getIntent")
    public Flux<String> getIntent(@RequestBody String userMessage){
        return intentAgent.chat(userMessage,promptUtil.getPrompt(PromptKey.INTENT_RECOGNIZE));
    }
}
