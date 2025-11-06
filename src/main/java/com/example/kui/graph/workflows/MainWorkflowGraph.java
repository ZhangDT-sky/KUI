package com.example.kui.graph.workflows;
import com.example.kui.graph.nodes.CodeDebugNode;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;

@Slf4j
@Configuration
public class MainWorkflowGraph {
    @Autowired
    private IntentRecognitionNode intentRecognitionNode;

    @Autowired
    private CodeDebugNode  codeDebugNode;

    // 将编译后的图缓存起来，避免每次调用都重新编译
    private CompiledGraph<WorkflowState> compiledGraph;
    
    private CompiledGraph<WorkflowState> getCompiledGraph() throws GraphStateException {
        if (compiledGraph == null) {
            StateGraph<WorkflowState> work = new StateGraph<>(WorkflowState.SCHEMA, WorkflowState::new)
//                    .addNode("IntentRecognitionNode", node_async(
//                            intentRecognitionNode
//                    ))
                    .addNode("CodeDebugNode",node_async(codeDebugNode))
                    .addEdge(START,"CodeDebugNode")
                    .addEdge("CodeDebugNode",END);
            compiledGraph = work.compile();
        }
        return compiledGraph;
    }
    
    public Optional<WorkflowState> graph(WorkflowState state) throws GraphStateException {
//        Optional<WorkflowState> res = getCompiledGraph().invoke(state.data());
        System.out.println();
        AsyncGenerator<NodeOutput<WorkflowState>> ans = getCompiledGraph().stream(state.data());
        ans.forEach(WorkflowState->{

        });
        return null;
    }
}