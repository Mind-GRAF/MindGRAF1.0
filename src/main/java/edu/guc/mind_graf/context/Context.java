package edu.guc.mind_graf.context;

import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.revision.Revision;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class Context {

    private final HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> hypotheses;
    private final String name;
    private Set<Integer, BitSet> AttitudesBitset; //TODO: wael init this


    public Context(String name, Set<String, Integer> attitudeNames) {
        this.name = name;
        this.hypotheses = new HashMap<>();
        Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[attitudeNames.size()];
        for (int i = 0; i < attitudeNames.size(); i++) {
            hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
        }
        this.hypotheses.put(0, hyps);
    }

    public Integer getPropositionAttitude(Integer prop) {

        // loop through all the Integer keys of attitudesBitset
        for (Integer key : this.AttitudesBitset.getSet().keySet()) {
            // If the propositionNode is in the attitude then return the name of the
            // attitude
            if (this.AttitudesBitset.get(key).get(prop)) {
                return key;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");
    }

    public String getName() {
        return name;
    }

    public void addHypothesisToContext(int attitudeNumber, PropositionNode node) {
        this.hypotheses.get(0)[attitudeNumber].getFirst().add(node);
        node.getSupport().setHyp(attitudeNumber);
    }

    public void addHypothesisToContext(int level, int attitudeNumber, PropositionNode node) {
        if (this.hypotheses.get(level) == null) {
            Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[this.hypotheses.get(0).length];
            for (int i = 0; i < this.hypotheses.get(0).length; i++) {
                hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
            }
            this.hypotheses.put(level, hyps);
        }
        this.hypotheses.get(level)[attitudeNumber].getFirst().add(node);
        node.getSupport().setHyp(attitudeNumber);
    }

    public ArrayList<Integer> getLevels() {
        return new ArrayList<>(this.hypotheses.keySet());
    }

    public void removeHypothesisFromContext(int level, int attitudeId, PropositionNode node) {
        Pair<PropositionNodeSet, PropositionNodeSet>[] pairArr = this.hypotheses.get(level);
        if (pairArr != null) {
            pairArr[attitudeId].getFirst().remove(node);
        }
    }

    public boolean isHypothesis(int level, int attitudeId, PropositionNode node) {
        Pair<PropositionNodeSet, PropositionNodeSet>[] pairArr = this.hypotheses.get(level);
        if (pairArr != null) {
            return pairArr[attitudeId].getFirst().contains(node);
        }
        return false;
    }

    public Pair<PropositionNodeSet, PropositionNodeSet> getAttitudeProps(int level, int attitudeID) {
        return this.hypotheses.get(level)[attitudeID];
    }

    /**
     * checks the validity of a node set as a support in a context and an attitude
     */
    public boolean isValidSupport(int level, int attitudeNumber, PropositionNodeSet nodes) {
        for (PropositionNode node : nodes) {
            if (!node.supported(this.getName(), attitudeNumber, level)) {
                return false;
            }
        }
        return true;
    }

    public void completelyRemoveNodeFromContext(int level, int attitudeNumber, PropositionNode node, boolean manual) {
        if (this.isHypothesis(level, attitudeNumber, node)) {
            this.removeHypothesisFromContext(level, attitudeNumber, node);
        }
        if (node.supported(this.getName(), attitudeNumber, level)) {
            if (manual) {
                this.manuallyRemoveInferredNodeFromContext(level, attitudeNumber, node);
            } else {
                automaticallyRemoveInferredNodeFromContext(level,attitudeNumber,node);
            }
        }
    }

    public void automaticallyRemoveInferredNodeFromContext(int level, int attitudeNumber, PropositionNode node) {
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> assumptionSupport : node.getSupport().getAssumptionSupport().get(level).get(attitudeNumber)) {
            for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support : assumptionSupport.getFirst().entrySet()) {
                PropositionNodeSet supportOriginSet = support.getValue().getFirst();
                if (!isValidSupport(level, attitudeNumber, supportOriginSet)) {
                    continue;
                }
                //TODO: wael add a method to get the level of a node in support
                if (supportOriginSet.size() == 1) {
                    this.completelyRemoveNodeFromContext(level, attitudeNumber, (PropositionNode) supportOriginSet.getNodes().iterator().next(), false);
                } else {
                    int indexOfNodeToRemove = getSmallestIndexFromStream(supportOriginSet.getNodes().stream().map(supportNode -> (PropositionNode) supportNode).mapToInt(PropositionNode::getGradeFromParent));
                    this.completelyRemoveNodeFromContext(level,attitudeNumber, (PropositionNode) supportOriginSet.getNodes().stream().skip(indexOfNodeToRemove-1).findFirst().get(),false);
                }
            }
        }
    }

    public static int getSmallestIndexFromStream(IntStream stream) {
        int minIndex = Integer.MAX_VALUE;
        int minValue = Integer.MAX_VALUE;
        int[] arr = stream.toArray();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < minValue) {
                minIndex = i;
                minValue = arr[i];
            }
        }
        return minIndex;
    }

    public void manuallyRemoveInferredNodeFromContext(int level, int attitudeNumber, PropositionNode node) {
        Revision.print("Starting removal of node: " + node.printShortData() + " from attitude: " + ContextController.getAttitudeName(attitudeNumber) + " from Context: " + this.getName());
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> assumptionSupport : node.getSupport().getAssumptionSupport().get(level).get(attitudeNumber)) {
            for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support : assumptionSupport.getFirst().entrySet()) {
                if (!isValidSupport(level, attitudeNumber, support.getValue().getFirst())) {
                    continue;
                }
                Revision.print("choose node to remove from this support in attitude: " + support.getKey());
                ArrayList<Node> supportNodes = new ArrayList<>(support.getValue().getFirst().getNodes());
                for (int i = 0; i < supportNodes.size(); i++) {
                    PropositionNode nodeInSupport = (PropositionNode) supportNodes.get(i);
                    Revision.print((i + 1) + ". Node: " + nodeInSupport.printShortData());
                }
                PropositionNode nodeToRemove = (PropositionNode) supportNodes.get(Revision.readInt() - 1);
                this.completelyRemoveNodeFromContext(level, attitudeNumber, nodeToRemove, true);
            }
        }
        Revision.print("successfully removed Node: " + node + " from attitude: " + ContextController.getAttitudeName(attitudeNumber) + " from Context: " + this.getName());
    }
}
