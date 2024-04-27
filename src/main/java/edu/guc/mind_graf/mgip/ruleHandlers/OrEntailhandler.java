package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.set.RuleInfoSet;

public class OrEntailhandler extends RuleInfoHandler{

    RuleInfo usedToInfer;
    @Override
    public RuleInfoSet insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        return null;
    }

    @Override
    public RuleInfoSet getAllRuleInfos() {
        return null;
    }

    public RuleInfoSet insertRI(RuleInfo ri) {
        if(ri.getPcount() > 0) {
            usedToInfer = ri;
            return new RuleInfoSet(ri);
        }
        return null;
    }

    public RuleInfo getUsedToInfer() {
        return usedToInfer;
    }

    public void setUsedToInfer(RuleInfo usedToInfer) {
        this.usedToInfer = usedToInfer;
    }
}
