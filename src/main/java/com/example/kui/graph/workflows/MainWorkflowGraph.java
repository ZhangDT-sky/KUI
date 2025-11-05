package com.example.kui.graph.workflows;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;

@Slf4j
@Configuration
public class MainWorkflowGraph {
    @Autowired
    private IntentRecognitionNode intentRecognitionNode;
    public Optional<AgentState> graph(AgentState state) throws GraphStateException {
        StateGraph<AgentState> work = new StateGraph<>(AgentState::new)
                .addNode("IntentRecognitionNode", node_async(
                        intentRecognitionNode
                ))
                .addEdge(START,"IntentRecognitionNode")
                .addEdge("IntentRecognitionNode",END);

        CompiledGraph<AgentState> app = work.compile();
        Optional<AgentState> res = app.invoke(state.data());
        return res;
    }



}
