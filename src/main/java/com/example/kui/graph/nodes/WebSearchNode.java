package com.example.kui.graph.nodes;

import com.example.kui.graph.state.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class WebSearchNode implements NodeAction<WorkflowState> {
    @Override
    public Map<String, Object> apply(WorkflowState state) throws Exception {

        return Map.of("messages","测试成功");
    }
}
