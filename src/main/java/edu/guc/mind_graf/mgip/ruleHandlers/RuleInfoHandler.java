package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.HashMap;

public abstract class RuleInfoHandler {

    private HashMap<String, RuleInfo> constantRIMap;

    private int cMin;

    public RuleInfoHandler() {
        constantRIMap = new HashMap<>();
    }

    public RuleInfo getConstantAntecedents(String context, int attitude) {
        return constantRIMap.get(context + attitude);
    }

    public RuleInfoSet insertRI(RuleInfo ri) throws InvalidRuleInfoException, DirectCycleException {
        if (ri.getSubs() == null || ri.getSubs().isEmpty()) {
            String cHash = ri.getContext() + ri.getAttitude();
            RuleInfo constantRI = constantRIMap.getOrDefault( cHash, new RuleInfo(ri.getContext(), ri.getAttitude())).combine(ri);
            constantRIMap.put(cHash, constantRI);
            RuleInfoSet result = new RuleInfoSet();
            if(constantRI.getPcount() >= cMin)
                result.addRuleInfo(constantRI);
            return result;
        }
        else
            return insertVariableRI(ri);
    }

    public static PropositionNodeSet getVariableAntecedents(NodeSet allAntecedents) {
        PropositionNodeSet antecedents = new PropositionNodeSet();
        System.out.println("Total antecedents to check: " + allAntecedents.size());

        for(Node n : allAntecedents.getValues()) {
            // Print info about the node
            System.out.println("Checking node: " + n.getName());

            // Fetch free variables explicitly
            NodeSet freeVars = n.fetchFreeVariables();
            System.out.println("Free variables size: " + (freeVars != null ? freeVars.size() : "null"));
            System.out.println("Is node open: " + n.isOpen());

            if(n.isOpen()) {
                System.out.println("Adding node to result: " + n.getName());
                antecedents.add(n);
            } else {
                System.out.println("Node not added");
            }
        }

        System.out.println("Final result size: " + antecedents.size());
        return antecedents;
    }

    public abstract RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException, DirectCycleException;

    public void setcMin(int cMin) {
        this.cMin = cMin;
    }

}