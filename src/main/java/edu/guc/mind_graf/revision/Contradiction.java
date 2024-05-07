package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.Set;

import java.util.Map;

public class Contradiction {
    private final PropositionNode node;
    private final Set<Integer,PropositionNode> contradictions;

    public Contradiction(PropositionNode node) {
        this.node = node;
        this.contradictions = new Set<>();
    }

    public PropositionNode getNode() {
        return node;
    }

    public Set<Integer, PropositionNode> getContradictions() {
        return contradictions;
    }

    @Override
    public String toString() {
        return "{ " +
                "node = " + node.getName() +
                ", contradicts = { " + this.printContractionsSet() +
                " }}";
    }

    private String printContractionsSet() {
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Integer,PropositionNode> entry :this.contradictions.getSet().entrySet()){
            sb.append("node: ").append(entry.getValue().getName()).append(", at attitude: ").append(entry.getKey());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contradiction that = (Contradiction) o;
        return this.node.getId() == that.node.getId() && this.contradictions.equals(that.contradictions);
    }
}
