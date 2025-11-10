package com.example.kui.graph.workflows;
import com.example.kui.graph.nodes.CodeSolveNode;
import com.example.kui.graph.nodes.IntentRecognitionNode;
import com.example.kui.graph.state.WorkflowState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.async.AsyncGenerator;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import static org.bsc.langgraph4j.StateGraph.START;

@Slf4j
@Configuration
public class MainWorkflowGraph {
    @Autowired
    private IntentRecognitionNode intentRecognitionNode;

    @Autowired
    private CodeSolveNode codeSolveNode;

    // 将编译后的图缓存起来，避免每次调用都重新编译
    private CompiledGraph<WorkflowState> compiledGraph;
    
    private CompiledGraph<WorkflowState> getCompiledGraph() throws GraphStateException {
        if (compiledGraph == null) {
            StateGraph<WorkflowState> work = new StateGraph<>(WorkflowState.SCHEMA, WorkflowState::new)
                    .addNode("IntentRecognitionNode", node_async(
                            intentRecognitionNode
                    ))
                    .addNode("CodeSolveNode",node_async(codeSolveNode))
                    .addEdge(START,"IntentRecognitionNode")
                    .addConditionalEdges("IntentRecognitionNode",
                            edge_async(state->{
                                String recognizedIntent = state.intentRecognition();
                                System.out.println("===============test==============");
                                System.out.println(recognizedIntent);
                                if (recognizedIntent.equals("PROBLEM_SOLVING")) {
                                    return "CodeSolveNode";
                                }
                                else{
                                    return END;
                                }
                            }),
                            Map.of(
                            "CodeSolveNode", "CodeSolveNode",
                                END, END
                            )
                    )
                    .addEdge("CodeSolveNode",END);
//                    .addEdge("IntentRecognitionNode",END);
            var checkPointSaver = new MemorySaver();
            var config = CompileConfig.builder()
                    .checkpointSaver(checkPointSaver)
                    .build();
            compiledGraph = work.compile(config);
        }
        return compiledGraph;
    }
    
    public Optional<WorkflowState> graph(WorkflowState state) throws GraphStateException {
        AsyncGenerator<NodeOutput<WorkflowState>> generator = getCompiledGraph().stream(state.data());

        WorkflowState finalState = null;
        Iterator<NodeOutput<WorkflowState>> iterator = generator.iterator();

        // 遍历所有节点输出
        while (iterator.hasNext()) {
            NodeOutput<WorkflowState> nodeOutput = iterator.next();
            WorkflowState nodeState = nodeOutput.state();

            if (nodeState != null) {
                finalState = nodeState;
                log.debug("Processed node: {}, state: {}", nodeOutput.node(), nodeState);
            }

            // 如果到达 END 节点，可以提前结束（可选）
            if (nodeOutput.isEND()) {
                log.debug("Reached END node");
                break;
            }
        }

        return Optional.ofNullable(finalState);

    }
    public Optional<WorkflowState> graph(Map<String, Object> initialState, RunnableConfig config)
            throws GraphStateException {
        var state = getCompiledGraph().invoke(initialState, config);
        return Optional.ofNullable(state.get());
    }
}