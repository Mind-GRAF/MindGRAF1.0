package edu.guc.mind_graf.mgip.ruleHandlers;

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

    public RuleInfoSet insertRI(RuleInfo ri) throws InvalidRuleInfoException {
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
        for(Node n : allAntecedents){   // only want the antecedents with free variables, other antecedents will be handled by the constant RI
            if(n.isOpen())
                antecedents.add(n);
        }
        return antecedents;
    }

    public abstract RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException;

    public void setcMin(int cMin) {
        this.cMin = cMin;
    }

}