package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.set.RuleInfoSet;

public abstract class RuleInfoHandler {

    private final RuleInfo constantRI;
    private int min;
    public RuleInfoHandler() {
        this.min = 0;
        constantRI = new RuleInfo();
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public RuleInfo getConstantAntecedents() {
        return constantRI;
    }

    public void insertRI(RuleInfo ri) throws InvalidRuleInfoException {
        if (ri.getSubs() == null || ri.getSubs().size() == 0)
            constantRI.combine(ri); // editeable depending on all possible cases
        else
            insertVariableRI(ri);
    }

    public abstract RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException;

}