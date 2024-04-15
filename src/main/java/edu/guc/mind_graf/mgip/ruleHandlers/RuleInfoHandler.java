package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public abstract class RuleInfoHandler {

    private RuleInfo constantRI;

    public RuleInfoHandler() {
        constantRI = new RuleInfo();
    }

    public RuleInfo getConstantAntecedents() {
        return constantRI;
    }

    public void insertRI(RuleInfo ri) throws InvalidRuleInfoException {
        if (ri.getSubs() == null || ri.getSubs().size() == 0)
            constantRI.combine(ri); // editable depending on all possible cases
        else
            insertVariableRI(ri);
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

}