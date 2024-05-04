package edu.guc.mind_graf.set;

import java.util.ArrayList;
import java.util.List;

import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;

public class CombinationSet {

    public static List<NodeSet> generateCombinations(NodeSet set, int n) {
        List<NodeSet> combinations = new ArrayList<>();
        if (n == 0) {
            return combinations;
        }
        if (n == 1) {
            for (Node node : set.getValues()) {
                NodeSet combination = new NodeSet();
                combination.add(node);
                combinations.add(combination);
            }
            return combinations;
        }
        List<Node> setValues = new ArrayList<Node>(set.getValues());
        for (int i = 0; i < setValues.size(); i++) {
            NodeSet remainingSet = new NodeSet(set);
            Node node = setValues.get(i);
            remainingSet.remove(node);
            List<NodeSet> subCombinations = generateCombinations(remainingSet, n - 1);
            for (NodeSet subCombination : subCombinations) {
                NodeSet combination = new NodeSet();
                combination.add(node);
                combination.addAll(subCombination);
                combinations.add(combination);
            }
        }
        //removing duplicates to get Unique combinations
        List<NodeSet> uniqueCombinations = new ArrayList<>();
        for (NodeSet combination : combinations) {
            boolean isUnique = true;
            for (NodeSet uniqueCombination : uniqueCombinations) {
                if (combination.equals(uniqueCombination)) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                uniqueCombinations.add(combination);
            }
        }
        return uniqueCombinations;
    }
    
    public static void main(String[] args) throws NoSuchTypeException {
        NodeSet Ants = new NodeSet();
        Network network = new Network();
        Node node1 = Network.createNode("A", "propositionnode");
        Node node2 = Network.createNode("B", "propositionnode");
        Node node3 = Network.createNode("C", "propositionnode");
        Node node4 = Network.createVariableNode("X", "propositionnode");

        System.out.println("Node 1: " + node1);
        System.out.println("Node 2 Name: " + node2.getName());
        System.out.println("Node 3 SyntacticType: " + node3.getSyntacticType());
        System.out.println("Node 4 free Vars : " + node4.getFreeVariables());
        System.out.println("Ants: " + Ants);
        System.out.println("Ants isFinal: " + Ants.isFinal());
        
        // Add some nodes to the Ants set after ensuring they are not null
        Ants.add(node1);
        Ants.add(node2);
        Ants.add(node3);
        Ants.add(node4);
        System.out.println("Ants: " + Ants);
        
        List<NodeSet> combinations = generateCombinations(Ants, 2);
        for (NodeSet combination : combinations) {
            System.out.println(combination);
        }
    }    
}