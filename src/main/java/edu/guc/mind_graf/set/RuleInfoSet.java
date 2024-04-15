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

    public RuleInfoSet combineAdd(RuleInfoSet combineWith) {
        RuleInfoSet newInfo = new RuleInfoSet();
        HashSet combinedSet = new HashSet();
        for(RuleInfo r1 : combineWith.getRIS()) {
            for(RuleInfo r2 : this.ris) {
                RuleInfo combined = r1.combine(r2);
                if(combined != null) {
                    newInfo.addRuleInfo(combined);
                    combinedSet.add(r2);
                    combinedSet.add(r1);
                }
            }
        }
        for(RuleInfo r : combineWith.getRIS()) {
            if(!combinedSet.contains(r)) {
                newInfo.addRuleInfo(r);
            }
        }
        for(RuleInfo r : this.ris) {
            if(!combinedSet.contains(r)) {
                newInfo.addRuleInfo(r);
            }
        }
        return newInfo;    // return set of rule infos that are a result of combining or have never been combined
    }

    public RuleInfoSet addAll(RuleInfoSet ris) {
        RuleInfoSet result = new RuleInfoSet();
        result.ris.addAll(this.ris);
        result.ris.addAll(ris.getRIS());
        return result;
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
