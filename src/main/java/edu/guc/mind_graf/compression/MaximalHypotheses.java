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
    PropositionNodeSet[] maximalHypotheses;

    public MaximalHypotheses(String context) {
        this.context = context;
        int attitudesSize = ContextController.getAttitudes().getSet().size();
        maximalHypotheses = new PropositionNodeSet[attitudesSize];
        for (int i = 0; i < attitudesSize; i++) {
            maximalHypotheses[i] = new PropositionNodeSet();
        }
    }

    public void computeMaximalHypotheses() {
        int attitudesSize = ContextController.getAttitudes().getSet().size();
        Context currentContext = ContextController.getContext(context);
        for (int attitudeID = 0; attitudeID < attitudesSize; attitudeID++) {
            PropositionNodeSet originSet = currentContext.getOriginHypotheses(attitudeID);
            processHypsSet(originSet, attitudeID);
        }

    }

    public void processHypsSet(PropositionNodeSet hypotheses, int attitudeID) {
        for (int hyp : hypotheses.getProps()) {
            if (isMaximalHyp(hyp, attitudeID)) {
                maximalHypotheses[attitudeID].add(hyp);
            }
        }
    }

    public boolean isMaximalHyp(int nodeID, int attitude) {
        boolean isMaxHyp = false;
        PropositionNode node = (PropositionNode) Network.getNodeById(nodeID);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> validSupports = node
                .getDerivingSupport(context, attitude, 0);
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> support : validSupports) {
            boolean isHypSupport = CHB.isHypothesisSupport(attitude, nodeID, support);
            if (isHypSupport)
                isMaxHyp = true;
            else {
                return false;
            }
        }

        return isMaxHyp;
    }

    public PropositionNodeSet[] getMaximalHypotheses() {
        return maximalHypotheses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MaximalHypotheses{");
        sb.append("context='").append(context).append('\'');
        sb.append(", maximalHypothesesByAttitude={\n");

        if (maximalHypotheses != null) {
            for (int attitudeID = 0; attitudeID < maximalHypotheses.length; attitudeID++) {
                PropositionNodeSet set = maximalHypotheses[attitudeID];
                sb.append("  Attitude ").append(attitudeID).append(": ");
                sb.append(set != null ? set.toString() : "null");
                sb.append("\n");
            }
        } else {
            sb.append("  null\n");
        }

        sb.append("}}");
        return sb.toString();
    }

    public static void main(String[] args) {

    }

}
