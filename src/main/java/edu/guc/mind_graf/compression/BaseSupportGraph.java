package edu.guc.mind_graf.compression;

import edu.guc.mind_graf.nodes.PropositionNode;

public class BaseSupportGraph {
    private String context;
    private BipartiteGraph bipartiteGraph;
    public BaseSupportGraph(String context) {
        this.context = context;
        this.bipartiteGraph = new GraphBuilder(context).buildBaseSupportGraph();
    }

    public BipartiteGraph getGraph() {
        return bipartiteGraph;
    }

}
