package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.HashMap;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;

public class MaximalHypotheses {
    String context;
    Pair<PropositionNodeSet, PropositionNodeSet>[] maximalHypotheses;

    public MaximalHypotheses(String context) {
        this.context = context;
        int attitudesSize = ContextController.getAttitudes().getSet().size();
        maximalHypotheses = (Pair<PropositionNodeSet, PropositionNodeSet>[]) new Pair[attitudesSize];
        for (int i = 0; i < attitudesSize; i++) {
            maximalHypotheses[i] = new Pair<>(new PropositionNodeSet(), new PropositionNodeSet());
        }
    }

    public void computeMaximalHypotheses() {
        int attitudesSize = ContextController.getAttitudes().getSet().size();
        Context currentContext = ContextController.getContext(context);
        for (int attitudeID = 0; attitudeID < attitudesSize; attitudeID++) {
            PropositionNodeSet originSet = currentContext.getOriginHypotheses(attitudeID);
            PropositionNodeSet gradedSet = currentContext.getGradedHypotheses(0, attitudeID);
            processHypsSet(originSet, false, attitudeID);
            processHypsSet(gradedSet, true, attitudeID);
        }

    }

    public void processHypsSet(PropositionNodeSet hypotheses, boolean isGradedHypotheses, int attitudeID) {
        for (int hyp : hypotheses.getProps()) {
            if (isMaximalHyp(hyp, attitudeID)) {
                if (isGradedHypotheses) {
                    maximalHypotheses[attitudeID].getSecond().add(hyp);
                } else {
                    maximalHypotheses[attitudeID].getFirst().add(hyp);
                }

            }
        }
    }

    public boolean isMaximalHyp(int nodeID, int attitude) {
        boolean isMaxHyp = false;
        PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> validSupports = node
                .getDerivingSupport(context, attitude, 0);
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : validSupports) {
            boolean isHypSupport = isHypothesisSupport(attitude, nodeID, support);
            if (isHypSupport)
                isMaxHyp = true;
            else {
                return false;
            }
        }

        return isMaxHyp;
    }

    public static boolean isHypothesisSupport(int attitudeID, int nodeID,
            Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support) {
        if (support.getFirst().size() != 1 || !support.getSecond().isEmpty())
            return false;
        Pair<PropositionNodeSet, PropositionNodeSet> supportingNodes = support.getFirst().get(attitudeID);
        if (supportingNodes != null) {
            if (supportingNodes.getFirst().size() == 1 && supportingNodes.getSecond().isEmpty()
                    && supportingNodes.getFirst().contains(nodeID))
                return true;
        }
        return false;
    }
    public Pair<PropositionNodeSet, PropositionNodeSet>[] getMaximalHypotheses() {
        return maximalHypotheses;
    }
    

}
