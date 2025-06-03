package edu.guc.mind_graf.informationrev;

import java.util.HashMap;
import java.util.HashSet;


public class SupportGraph {

    private final HashSet<String> nodes;
    private final HashMap<String, HashSet<String>> edges;

    public SupportGraph(InformationState state) {
        this.nodes = new HashSet<>();
        this.edges = new HashMap<>();
        nodes.addAll(state.getValidSources());
        nodes.addAll(state.getValidPropositions());
        for (String source : state.getValidSources()) {
            edges.put(source, new HashSet<>(state.getPropositionsBySource(source)));
        }
        for (String prop : state.getValidPropositions()) {
            edges.put(prop, new HashSet<>(state.getSourcesByProposition(prop)));
        }
    }


    public void printGraph() {
        System.out.println("Nodes:");
        nodes.forEach(node -> System.out.println("  - " + node));
        System.out.println("\nEdges:");
        edges.forEach((from, toSet) -> {
            if (!toSet.isEmpty()) {
                System.out.print("  " + from + " → ");
                System.out.println(String.join(", ", toSet));
            }
        });
    }
}