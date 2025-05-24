package edu.guc.mind_graf.compression;

import java.util.HashMap;


public class BaseSupportGraph {
    private String context;
    private BipartiteGraph bipartiteGraph;
    private HashMap<HypNode, Integer> nodesMap;
    public BaseSupportGraph(String context) {
        this.context = context;
        GraphBuilder builder =  new GraphBuilder(context);
        this.bipartiteGraph = builder.buildBaseSupportGraph();
        this.nodesMap = builder.hypothesesNodesRev; 
    }
    public  HashMap<HypNode, Integer> getNodesMap(){
        return nodesMap;
    }
    public BipartiteGraph getGraph() {
        return bipartiteGraph;
    }

}
