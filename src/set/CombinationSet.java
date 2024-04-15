package set;

import java.util.ArrayList;
import java.util.List;

import nodes.Node;

public class CombinationSet {

    public static List<NodeSet> generateAntecedentCombinations(NodeSet Ants, int i) {
        return generateCombinations(Ants, i);
    }

    private static List<NodeSet> generateCombinations(NodeSet Ants, int i) {
        List<NodeSet> combinations = new ArrayList<>();
        generateCombinationsHelper(Ants, i, 0, new NodeSet(), combinations);
        return combinations;
    }

    private static void generateCombinationsHelper(NodeSet Ants, int i, int index, NodeSet currentCombination, List<NodeSet> combinations) {
        if (currentCombination.size() == i) {
            NodeSet newCombination = new NodeSet();
            for (Node node : currentCombination) {
                newCombination.add(node);
            }
            combinations.add(newCombination);
            return;
        }
        if (index >= Ants.size()) {
            return;
        }

        // Include the current node
        Node currentNode = Ants.get(index);
        currentCombination.add(currentNode);
        generateCombinationsHelper(Ants, i, index + 1, currentCombination, combinations);

        // Exclude the current node
        currentCombination.remove(currentNode);
        generateCombinationsHelper(Ants, i, index + 1, currentCombination, combinations);
    }
}
