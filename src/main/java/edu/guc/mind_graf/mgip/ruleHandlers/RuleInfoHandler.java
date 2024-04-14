package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;

public abstract class RuleInfoHandler {

    private RuleInfo constantRI = new RuleInfo();

    public RuleInfo getConstantAntecedents() {
        return constantRI;
    }

    public void insertRI(RuleInfo ri) throws InvalidRuleInfoException {
        if (ri.getSubs() == null || ri.getSubs().size() == 0)
            constantRI.combine(ri); // editeable depending on all possible cases
        else
            insertVariableRI(ri);
    }

    public abstract void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException;

    public abstract void clear();

}