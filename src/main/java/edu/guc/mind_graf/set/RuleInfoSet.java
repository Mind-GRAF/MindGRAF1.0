package edu.guc.mind_graf.set;

import java.util.HashSet;

import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;

public class RuleInfoSet {
    
    private HashSet<RuleInfo> ris;

    public RuleInfoSet() {
        ris = new HashSet<RuleInfo>();
    }

    public HashSet<RuleInfo> getRIS() {
        return ris;
    }

    public void addRuleInfo(RuleInfo ri) {
        ris.add(ri);
    }

    public RuleInfoSet combine(RuleInfo ri) {
        boolean combined = false;
        for (RuleInfo r : ris) {
            r.combine(ri);
        }
        return this;
    }

    public RuleInfoSet combineAdd(RuleInfo ri) {
        for(RuleInfo r : ris) {
            RuleInfo combined = r.combineAdd(ri);
            if(combined != null)
                ris.add(combined);
        }
        ris.add(ri);
        return this;
    }

    public void clear() {
        ris.clear();
    }

}
