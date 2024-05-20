package edu.guc.mind_graf.context;

import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.parser.CLI;
import edu.guc.mind_graf.parser.MindGRAF_Parser;
import edu.guc.mind_graf.revision.Revision;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Pair;

import java.util.*;
import java.util.stream.IntStream;

public class Context {

    private final HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> hypotheses;
    private final String name;

    private static int clonedContextsCount;
    private static final ArrayList<Context> assumedContexts = new ArrayList<>();

    public Context(String name, Set<String, Integer> attitudeNames) {
        this.name = name;
        this.hypotheses = new HashMap<>();
        Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[attitudeNames.size()];
        for (int i = 0; i < attitudeNames.size(); i++) {
            hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
        }
        this.hypotheses.put(0, hyps);
    }

    public Integer getPropositionAttitude(Integer nodeId) {
        PropositionNode p = (PropositionNode) Network.getNodeById(nodeId);
        for (int i = 0; i < this.hypotheses.get(0).length; i++) {
            if (this.hypotheses.get(0)[i].getFirst().contains(p)) {
                return i;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");

        // DEPRECATED CODE:
        // // loop through all the Integer keys of attitudesBitset
        // for (Integer key : this.AttitudesBitset.getSet().keySet()) {
        // // If the propositionNode is in the attitude then return the name of the
        // // attitude
        // if (this.AttitudesBitset.get(key).get(nodeId)) {
        // return key;
        // }
        // }
        // throw new RuntimeException("PropositionNode is not in any attitude");
    }

    public String getName() {
        return name;
    }

    public void addHypothesisToContext(int level, int attitudeId, PropositionNode node) {

        if (this.hypotheses.get(level) == null) {
            Pair<PropositionNodeSet, PropositionNodeSet>[] hyps = new Pair[this.hypotheses.get(0).length];
            for (int i = 0; i < this.hypotheses.get(0).length; i++) {
                hyps[i] = (new Pair<>(new PropositionNodeSet(), new PropositionNodeSet()));
            }
            this.hypotheses.put(level, hyps);
        }
        this.hypotheses.get(level)[attitudeId].getFirst().add(node);
        node.getSupport().setHyp(attitudeId);

    }

    public ArrayList<Integer> getLevels() {
        return new ArrayList<>(this.hypotheses.keySet());
    }

    public void removeHypothesisFromContext(int level, int attitudeId, PropositionNode node) {
        Pair<PropositionNodeSet, PropositionNodeSet>[] pairArr = this.hypotheses.get(level);
        if (pairArr != null) {
            pairArr[attitudeId].getFirst().remove(node);
        }
        //TODO:wael remove from support
    }

    public boolean isHypothesis(int level, int attitudeId, PropositionNode node) {
        System.out.println("in isHypothesis");
        for(int i=0;i<=level;i++) {
            Pair<PropositionNodeSet, PropositionNodeSet>[] pairArr = this.hypotheses.get(i);
            if (pairArr != null) {
                boolean found = pairArr[attitudeId].getFirst().contains(node) || pairArr[attitudeId].getSecond().contains(node);
                if(found){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOriginHypothesis(int level, int attitudeId, PropositionNode node) {
        System.out.println("in isOriginHypothesis");
        for(int i=0;i<=level;i++) {
            Pair<PropositionNodeSet, PropositionNodeSet>[] pairArr = this.hypotheses.get(i);
            if (pairArr != null) {
                return pairArr[attitudeId].getFirst().contains(node);
            }
        }
        return false;
    }

    public Pair<PropositionNodeSet, PropositionNodeSet> getAttitudeProps(int level, int attitudeID) {
        return this.hypotheses.get(level)[attitudeID];
    }

    /**
     * checks the validity of a node set as a support in a level and an attitude
     */
    public boolean isInvalidSupport(int level, int attitudeId, PropositionNodeSet nodes) {
        for (PropositionNode node : nodes) {
            if (!node.supported(this.getName(), attitudeId, level)) {
                return true;
            }
        }
        return false;
    }

    public void completelyRemoveNodeFromContext(int level, int attitudeId, PropositionNode node, boolean manualMode) {
        if (this.isHypothesis(level, attitudeId, node)) {
            this.removeHypothesisFromContext(level, attitudeId, node);
        }
        if (node.supported(this.getName(), attitudeId, level)) {
            if (manualMode) {
                this.manuallyRemoveInferredNodeFromContext(level, attitudeId, node);
            } else {
                automaticallyRemoveInferredNodeFromContext(level, attitudeId, node);
            }
        }
    }

    public void automaticallyRemoveInferredNodeFromContext(int level, int attitudeId, PropositionNode node) {
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> assumptionSupport : node.getSupport().getAssumptionSupport().get(level).get(attitudeId)) {
            for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support : assumptionSupport.getFirst().entrySet()) {
                if (this.isInvalidSupport(level, attitudeId, support.getValue().getSecond())) {
                    continue;
                }
                support.getValue().getSecond().remove(node);
            }
        }
    }

    public void manuallyRemoveInferredNodeFromContext(int level, int attitudeId, PropositionNode node) {
        System.out.println("in manuallyRemoveInferredNodeFromContext");
        Revision.print("Starting removal of node: " + node.printShortData() + " from attitude: " + ContextController.getAttitudeName(attitudeId) + " from Context: " + this.getName());
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> assumptionSupport : node.getSupport().getAssumptionSupport().get(level).get(attitudeId)) {
            for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support : assumptionSupport.getFirst().entrySet()) {
                if (this.isInvalidSupport(level, attitudeId, support.getValue().getFirst())) {
                    continue;
                }
                Revision.print("choose node to remove from this support in attitude: " + support.getKey());
                ArrayList<Node> supportNodes = new ArrayList<>(support.getValue().getFirst().getNodes());
                for (int i = 0; i < supportNodes.size(); i++) {
                    PropositionNode nodeInSupport = (PropositionNode) supportNodes.get(i);
                    Revision.print((i + 1) + ". Node: " + nodeInSupport.printShortData());
                }
                PropositionNode nodeToRemove = (PropositionNode) supportNodes.get(Revision.readInt() - 1);
                this.completelyRemoveNodeFromContext(level, attitudeId, nodeToRemove, true);
            }
        }
        Revision.print("successfully removed Node: " + node + " from attitude: "
                + ContextController.getAttitudeName(attitudeId) + " from Context: " + this.getName());
    }

    public Context cloneContext(int attitudeId, NodeSet assumptionNodes) {

        boolean contextHasContradictingNodes = assumptionNodes.getValues().stream()
                .allMatch(node -> this.isHypothesis(0, attitudeId, (PropositionNode) node.getNegation()));
        if (contextHasContradictingNodes) {
            return null;
        }

        Context clonedContext = new Context(this.getName() + " " + clonedContextsCount,
                ContextController.getAttitudes());
        assumedContexts.add(clonedContext);
        clonedContextsCount++;
        ContextController.getContextSet().add(name, clonedContext);

        for (int level : this.getLevels()) {
            for (int i = 0; i < this.hypotheses.get(0).length; i++) {
                if(i == attitudeId){
                    assumptionNodes.getValues().forEach(node -> clonedContext.addHypothesisToContext(0,attitudeId,(PropositionNode)node));
                }
                Pair<PropositionNodeSet, PropositionNodeSet> pair = this.hypotheses.get(level)[i];
                for (PropositionNode node : pair.getFirst()) {
                    clonedContext.addHypothesisToContext(level, i, node);
                }
                for (PropositionNode node : pair.getSecond()) {
                    clonedContext.addHypothesisToContext(level, i, node);
                }
            }
        }

        return clonedContext;
    }

    public static void deleteAssumedContexts() {
        for (Context c : assumedContexts) {
            ContextController.getContextSet().remove(c.getName());
        }
        clonedContextsCount = 0;
    }

    public HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>[]> getHypotheses() {
        return hypotheses;
    }
}
