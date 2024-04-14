package edu.guc.mind_graf.set;

import java.util.HashSet;
import java.util.Iterator;

import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.PropositionNode;

public class RuleInfoSet implements Iterable<RuleInfo>{
    
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
        RuleInfoSet newInfo = new RuleInfoSet();
        for(RuleInfo r : ris) {
            RuleInfo combined = r.combineAdd(ri);
            if(combined != null) {
                ris.add(combined);
                newInfo.addRuleInfo(combined);
            }
        }
        ris.add(ri);
        newInfo.addRuleInfo(ri);
        return newInfo;    // return only the ruleinfo affected by the addition
    }

    public void clear() {
        ris.clear();
    }

    public int size() {
        return ris.size();
    }

    @Override
    public Iterator<RuleInfo> iterator() {
        return ris.iterator();
    }
}
