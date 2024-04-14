package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public abstract class SIndex extends RuleInfoHandler {

    private NodeSet commonVariables;

    public SIndex() {
        commonVariables = new NodeSet();
    }

    public SIndex(NodeSet commonVariables) {
        this.commonVariables = commonVariables;
    }

    public NodeSet getCommonVariables() {
        return commonVariables;
    }

    public void setCommonVariables(NodeSet commonVariables) {
        this.commonVariables = commonVariables;
    }

    public SIndex createSIndex(PropositionNodeSet antecedents) {
        NodeSet commonVariables = antecedents.getCommonVariables();
        return new Linear(commonVariables);
    }

    protected int customHash(Substitutions subs) throws InvalidRuleInfoException {
        int hash = 0;
        int factor = 1;
        for(Node var : commonVariables){
            if(subs.get(var) == null)
                throw new InvalidRuleInfoException("substitution not in info");
            hash += subs.get(var).getId()*factor; //consider checking for invalid subs here (if var not in subs, they're invalid)
            factor *= 100;
        }
        //assuming ids wont be over 100
        return hash;
    }

    @Override
    public RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        int hash = customHash(ri.getSubs());
        return insertIntoMap(ri, hash);
    }

    public abstract RuleInfoSet insertIntoMap(RuleInfo ri, int hash);

}