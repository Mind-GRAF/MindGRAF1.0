package edu.guc.mind_graf.revision;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class Revision {
    public static ArrayList<PropositionNodeSet> minimalNoGoods = new ArrayList<>();

    public static void ensureConsistency(Context c, int level, int attitudeNumberOfAddedNode, PropositionNode nodeToAdd) {
        ArrayList<Contradiction> contradictions = checkContradiction(c, level, attitudeNumberOfAddedNode, nodeToAdd);
        if (contradictions != null && !contradictions.isEmpty()) {
            if (ContextController.automaticHandlingEnabled()) {
                automaticContradictionHandling(c, level, attitudeNumberOfAddedNode, contradictions);
            } else {
                manualContradictionHandling(c, level, attitudeNumberOfAddedNode, contradictions);
            }
        }
    }

    public static ArrayList<Contradiction> checkContradiction(Context c, int level, int attitudeNumberOfAddedNode, PropositionNode node) {
        System.out.println("checking contradictions");
        PropositionNode nodeCompliment = (PropositionNode) node.getNegation();
        if (nodeCompliment == null) {
            //node complement is not in the network so a contradiction can never happen
            return null;
        }
        System.out.println("found negation" + nodeCompliment);
        ArrayList<Contradiction> contradictions = new ArrayList<>();
        ArrayList<ArrayList<Integer>> filteredConsistentAttitudes = filterAttitudes(ContextController.getConsistentAttitudes(), attitudeNumberOfAddedNode);
        if (ContextController.isCacheEnabled()) {
            ArrayList<PropositionNodeSet> filteredNoGoods = filterNoGoods(node);
            for (ArrayList<Integer> entry : filteredConsistentAttitudes) {
                Contradiction cont = new Contradiction(node);
                for (int attitudeNumber : entry) {
                    if(complimentFoundInCache(c,level,attitudeNumber,filteredNoGoods)){
                        cont.getContradictions().add(attitudeNumber, nodeCompliment);
                        continue;
                    }
                    if (nodeCompliment.supported(c.getName(), attitudeNumber, level)) {
                        cont.getContradictions().add(attitudeNumber, nodeCompliment);
                        //TODO: wael add node to cache
                    }
                }
                if (!cont.getContradictions().isEmpty() && !contradictions.contains(cont)) {
                    contradictions.add(cont);
                }
            }
            System.out.println("Found Contradictions: " + contradictions);
        } else {
            for (ArrayList<Integer> entry : filteredConsistentAttitudes) {
                Contradiction cont = new Contradiction(node);
                for (int attitudeNumber : entry) {
                    if (nodeCompliment.supported(c.getName(), attitudeNumber, level)) {
                        cont.getContradictions().add(attitudeNumber, nodeCompliment);
                    }
                }
                if (!cont.getContradictions().isEmpty() && !contradictions.contains(cont)) {
                    contradictions.add(cont);
                }
            }
            System.out.println("Found Contradictions: " + contradictions);
            return contradictions;
        }
        return contradictions;
    }

    public static void manualContradictionHandling(Context c, int level, int attitudeNumber, ArrayList<Contradiction> contradictions) {
        print("Contradiction Detected");
        print("Select How to handle this contradiction");
        print("\t1. Remove node:" + contradictions.getFirst().toString());
        print("\t2. Remove contradicting nodes");
        print(contradictions.toString());
        int decision = readInt();
        handleDecision(c, level, attitudeNumber, contradictions, decision == 1, true);
        print("completed contradiction handling");
    }

    public static void automaticContradictionHandling(Context c, int level, int attitudeNumber, ArrayList<Contradiction> contradictions) {
        boolean nodeIsHyp = c.isHypothesis(attitudeNumber, level, contradictions.getFirst().getNode());
        boolean contradictingIsHyp = containsHyp(c, level, contradictions);

        if (nodeIsHyp && contradictingIsHyp) {
            print("Found a contradiction that can't be automatically handled, reverting to manual handling");
            manualContradictionHandling(c, level, attitudeNumber, contradictions);
        }
        if (nodeIsHyp && !contradictingIsHyp) {
            for (Contradiction cont : contradictions) {
                for (Map.Entry<Integer, PropositionNode> entry : cont.getContradictions().getSet().entrySet()) {
                    c.completelyRemoveNodeFromContext(level, entry.getKey(), entry.getValue(), false);
                }
            }
        }
        if (!nodeIsHyp && contradictingIsHyp) {
            c.completelyRemoveNodeFromContext(level, attitudeNumber, contradictions.getFirst().getNode(), false);
        } else {
            //Actual Automatic handling
            int gradeOfNode = getGradeOfNode(c, level, attitudeNumber, contradictions.getFirst().getNode());
            ArrayList<Integer> gradesOfContradictions = new ArrayList<>();
            for (Contradiction cont : contradictions) {
                ArrayList<Integer> gradesOfConsistentAttitudes = new ArrayList<>();
                for (Map.Entry<Integer, PropositionNode> entry : cont.getContradictions().getSet().entrySet()) {
                    gradesOfConsistentAttitudes.add(getGradeOfNode(c, level, entry.getKey(), entry.getValue()));
                }
                //This merges on the level of 2 contradicting nodes in the same consistent attitudes list
                gradesOfContradictions.add(gradesOfConsistentAttitudes.stream().mapToInt(i -> i).reduce(ContextController.getMergeFunction()).orElse(0));
            }
            //This merges between grades of consistent attitudes to get the final grade
            int gradeOfContradictions = gradesOfContradictions.stream().mapToInt(i -> i).reduce(ContextController.getMergeFunction()).orElse(0);

            handleDecision(c, level, attitudeNumber, contradictions, gradeOfNode <= gradeOfContradictions, false);
        }
    }

    public static void handleDecision(Context c, int level, int attitudeNumber, ArrayList<Contradiction> contradictions, boolean removeNode, boolean manual) {
        if (removeNode) {
            c.completelyRemoveNodeFromContext(level, attitudeNumber, contradictions.getFirst().getNode(), manual);
        } else {
            for (Contradiction cont : contradictions) {
                for (Map.Entry<Integer, PropositionNode> entry : cont.getContradictions().getSet().entrySet()) {
                    c.completelyRemoveNodeFromContext(level, entry.getKey(), entry.getValue(), manual);
                }
            }
        }
    }

    private static boolean containsHyp(Context c, int level, ArrayList<Contradiction> contradictions) {
        for (Contradiction contradiction : contradictions) {
            for (Map.Entry<Integer, PropositionNode> entry : contradiction.getContradictions().getSet().entrySet()) {
                if (c.isHypothesis(entry.getKey(), level, entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getGradeOfNode(Context c, int level, int attitudeId, PropositionNode node) {
        ArrayList<Integer> grades = new ArrayList<>();
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> assumptionSupport : node.getSupport().getAssumptionSupport().get(level).get(attitudeId)) {
            for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support : assumptionSupport.getFirst().entrySet()) {
                if (c.isInvalidSupport(level, attitudeId, support.getValue().getFirst())) {
                    continue;
                }
                //maps the support to a stream of integers representing the grade of every node in the support then merges them using ContextController.mergeGrades()
                grades.add(support.getValue().getFirst().getNodes().stream().mapToInt(suportNode -> ((PropositionNode) suportNode).getGradeFromParent(c, level, attitudeId)).reduce(ContextController.getMergeFunction()).orElse(0));
            }
        }
        //merges grades of every support to return the final grade of this node
        return grades.stream().mapToInt(i -> i).reduce(ContextController.getMergeFunction()).orElse(0);
    }

    public static ArrayList<ArrayList<Integer>> filterAttitudes(ArrayList<ArrayList<Integer>> consistentAttitudes, int attitudeNumber) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (ArrayList<Integer> entry : consistentAttitudes) {
            if (entry.contains(attitudeNumber)) {
                result.add(entry);
            }
        }
        return result;
    }


    public static ArrayList<PropositionNodeSet> filterNoGoods(PropositionNode node) {
        List<PropositionNodeSet> filteredList = minimalNoGoods.stream().filter(list -> list.contains(node)).toList();
        filteredList.forEach(list -> list.remove(node));
        return new ArrayList<>(filteredList);
    }

    public static boolean complimentFoundInCache(Context c, int level, int attitudeId, ArrayList<PropositionNodeSet> cache) {
        for (PropositionNodeSet cacheEntry : cache) {
            boolean areAllHypotheses = cacheEntry.getNodes().stream().allMatch(node -> c.isHypothesis(level, attitudeId, (PropositionNode) node));
            if(areAllHypotheses){
                return true;
            }
        }
        return false;
    }
    ////////////////////////////////////////////////////

    public static void print(String s) {
        System.out.println(s);
    }

    public static int readInt() {
        Scanner sc = new Scanner(System.in);
        int x = sc.nextInt();
        sc.close();
        return x;
    }
}
