package edu.guc.mind_graf.informationrev;

import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import edu.guc.mind_graf.support.Pair;


public class SupportGraph {
    public static final String RESET = "\033[0m";
    public static final String CYAN = "\033[0;36m";
    public static final String BLUE = "\033[0;34m";
    private final HashSet<Pair<String, String>> nodes;
    private final HashMap<Pair<String, String>, HashSet<Pair<String, String>>> edges;

    public SupportGraph(InformationState state) {
        this.nodes = new HashSet<>();
        this.edges = new HashMap<>();
        HashSet<String> validSources = state.getValidSources();
        HashSet<String> validPropositions = state.getValidPropositions();
        for(String source : validSources) {
            nodes.add(new Pair<>(source, "source"));
        }
        for(String proposition : validPropositions) {
            nodes.add(new Pair<>(proposition, "proposition"));
        }
        for (String source : state.getValidSources()) {
            HashSet<String> props = new HashSet<>(state.getPropositionsBySource(source));
            Pair<String, String> sourcePair = new Pair<>(source, "source");
            HashSet<Pair<String, String>> propPairs = new HashSet<>();
            for(String prop : props) {
                propPairs.add(new Pair<>(prop, "proposition"));
            }
            edges.put(sourcePair, propPairs);
        }
        for (String proposition : state.getValidPropositions()) {
            HashSet<Pair<String, String>> sources = new HashSet<>(state.getSourcesByProposition(proposition));
            Pair<String, String> propositionPair = new Pair<>(proposition, "proposition");
            HashSet<Pair<String, String>> sourcePairs = new HashSet<>();
            for(Pair<String, String> source : sources) {
                sourcePairs.add(new Pair<>(source.getFirst(), "source"));
            }
            edges.put(propositionPair, sourcePairs);
        }
    }


    public void printGraph() {
        System.out.println("Nodes:");
        nodes.forEach(node -> {
            String color = (node.getSecond().equals("source") ? BLUE : CYAN);
            System.out.println("  - " + color + node.getFirst() + RESET);
        });

        System.out.println("\nEdges:");
        edges.forEach((from, toSet) -> {
            String fromColor = (from.getSecond().equals("source") ? BLUE : CYAN);
            System.out.print("  - " + fromColor + from.getFirst() + RESET + " → ");

            String joined = toSet.stream()
                    .map(to -> {
                        String toColor = (to.getSecond().equals("source") ? BLUE : CYAN);
                        return toColor + to.getFirst() + RESET;
                    })
                    .collect(Collectors.joining(", "));

            System.out.println(joined);
        });
    }
}
