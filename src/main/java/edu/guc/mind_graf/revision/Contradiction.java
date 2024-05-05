package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.Set;

import java.util.HashMap;

public class Contradiction {
    private final PropositionNode node;
    private final Set<Integer,PropositionNode> Contradictions;

    public Contradiction(PropositionNode node) {
        this.node = node;
        this.Contradictions = new Set<>();
    }

    public PropositionNode getNode() {
        return node;
    }

    public Set<Integer, PropositionNode> getContradictions() {
        return Contradictions;
    }
}
