package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;

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

    protected int customHash(Substitutions subs) {
        int[] orderedArray = new int[commonVariables.size()];
        int index = 0;
        for(Node var : commonVariables){
            orderedArray[index++] = subs.get(var).getId();
        }
        //assuming ids wont be over 100
        return orderedArray[0] + orderedArray[1] * 100 + orderedArray[2] * 10000;
    }

    @Override
    public void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        int hash = customHash(ri.getSubs());
        insertIntoMap(ri, hash);
    }

    public abstract void insertIntoMap(RuleInfo ri, int hash);

    @Override
    public void clear() {
        commonVariables.clear();
    }

}