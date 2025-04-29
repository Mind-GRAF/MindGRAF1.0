package edu.guc.mind_graf.compression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.network.NetworkController;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;
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
    sb.append("MaximalHypotheses {\n");
    sb.append("  context: '").append(context).append("',\n");
    sb.append("  maximalHypothesesByAttitude:\n");

    if (maximalHypotheses != null) {
        for (int attitudeID = 0; attitudeID < maximalHypotheses.length; attitudeID++) {
            sb.append("    Attitude ").append(attitudeID).append(": ");

            PropositionNodeSet set = maximalHypotheses[attitudeID];
            if (set != null) {
                String indentedSet = set.toString().indent(6).trim(); // Java 12+
                sb.append(indentedSet).append("\n");
            } else {
                sb.append("null\n");
            }
        }
    } else {
        sb.append("    null\n");
    }

    sb.append("}");
    return sb.toString();
}

    

    public static void main(String[] args) throws NoSuchTypeException {
         Set<String, Integer> attitudeNames = new Set<>();
        attitudeNames.add("a1", 0);
        attitudeNames.add("a2", 1);
        attitudeNames.add("a3", 2);

        ArrayList<ArrayList<Integer>> consistentAttitudes = new ArrayList<>();
        consistentAttitudes.add(new ArrayList<>(Arrays.asList(0, 1))); // a1 and a2
        consistentAttitudes.add(new ArrayList<>(Arrays.asList(1, 2))); // a2 and a3

        Network n = NetworkController.setUp(attitudeNames, consistentAttitudes, false, false, false, 1);

        ContextController.createNewContext("guc");
        ContextController.setCurrContext("guc");
        Context contextToBeTested = ContextController.getContext("guc");

        PropositionNode node1 = (PropositionNode) Network.createNode("1", "propositionnode");
        PropositionNode node2 = (PropositionNode) Network.createNode("2", "propositionnode");
        PropositionNode node3 = (PropositionNode) Network.createNode("3", "propositionnode");
        PropositionNode node4 = (PropositionNode) Network.createNode("4", "propositionnode");
        PropositionNode node5 = (PropositionNode) Network.createNode("5", "propositionnode");

        contextToBeTested.addHypothesisToContext(0, 0, node1);
        contextToBeTested.addHypothesisToContext(0, 1, node1);
        //contextToBeTested.addHypothesisToContext(0, 2, node1);
        contextToBeTested.addHypothesisToContext(0, 0, node2);
        contextToBeTested.addHypothesisToContext(0, 1, node3);
        contextToBeTested.addHypothesisToContext(0, 2, node4);
        contextToBeTested.addHypothesisToContext(0, 0, node5);
        System.out.print(ContextController.getContext("guc").toString());

        PropositionNodeSet ps_1_2 = new PropositionNodeSet(1, 2);
        PropositionNodeSet ps_1_3 = new PropositionNodeSet(1, 3);
        PropositionNodeSet ps_1_2_5 = new PropositionNodeSet(1, 2, 5);
        PropositionNodeSet ps_1_4 = new PropositionNodeSet(1, 4);
        PropositionNodeSet ps_1_5 = new PropositionNodeSet(1, 5);
        PropositionNodeSet ps_3 = new PropositionNodeSet(3);
        PropositionNodeSet ps_4 = new PropositionNodeSet(4);

        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> supports = new ArrayList<>();
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support;

        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_2, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet()));
        node3.addJustificationBasedSupports(1, Network.currentLevel, new ArrayList<>(supports));
        supports.clear();

        support = new HashMap<>();
        support.put(0, new Pair<>(ps_1_5, new PropositionNodeSet()));
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));

        support.clear();
        support.put(1, new Pair<>(ps_3, new PropositionNodeSet()));
        supports.add(new Pair<>(new HashMap<>(support), new PropositionNodeSet(12)));
        node4.addJustificationBasedSupports(2, Network.currentLevel, new ArrayList<>(supports));

        supports.clear();
        support = new HashMap<>();
        support.put(2, new Pair<>(ps_1_4, new PropositionNodeSet()));
        supports.add(new Pair<>(support, new PropositionNodeSet(10)));
        node2.addJustificationBasedSupports(0, Network.currentLevel, new ArrayList<>(supports));

        System.out.println(node1.getSupport() + "node1 assumptionBasedSupport");
        System.out.println(node2.getSupport() + "node2 assumptionBasedSupport");
        System.out.println(node3.getSupport() + "node3 assumptionBasedSupport");
        System.out.println(node4.getSupport() + "node4 assumptionBasedSupport");
        System.out.println(node5.getSupport() + "node5 assumptionBasedSupport");

        MaximalHypotheses maxHyps = new MaximalHypotheses("guc");
        maxHyps.computeMaximalHypotheses();
        System.out.println(maxHyps);

    }

}
