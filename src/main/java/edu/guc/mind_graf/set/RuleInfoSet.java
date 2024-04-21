package edu.guc.mind_graf.set;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;

public class RuleInfoSet implements Iterable<RuleInfo>{
    
    private HashSet<RuleInfo> ris;

    public RuleInfoSet() {
        ris = new HashSet<>();
    }

    public RuleInfoSet(RuleInfo... ruleInfos) {
        this.ris = new HashSet<>();
        for (RuleInfo r : ruleInfos)
            this.ris.add(r);
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
        HashSet<RuleInfo> newRIS = new HashSet<>();
        for(RuleInfo r : ris) {
            RuleInfo combined = r.combineAdd(ri);
            if(combined != null) {
                newRIS.add(combined);
                newInfo.addRuleInfo(combined);
            }
            newRIS.add(r);
        }
        newRIS.add(ri);
        ris = newRIS;
        if(newInfo.size() == 0) // may want to remove this line depending on should the parent get the ruleinfo even if it combined with something already?
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

    public RuleInfoSet union(RuleInfoSet ris) {
        RuleInfoSet result = new RuleInfoSet();
        result.ris.addAll(this.ris);
        if(ris != null)
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

    @Override
    public String toString() {
        String res = "";
        for(RuleInfo ri : ris) {
            res += ri.toString() + " ";
        }
        return res;
    }

    public RuleInfoSet combineDisjointSets(RuleInfoSet combineWith){
        RuleInfoSet result = new RuleInfoSet();
        for(RuleInfo ofSet1 : this){
            for(RuleInfo ofSet2 : this){
                result.addRuleInfo(ofSet1.combine(ofSet2));
            }
        }
        return result;
    }

    public void removeRuleInfo(RuleInfo toRemove){
        ris.remove(toRemove);
    }

    public boolean isEmpty(){
        return ris.isEmpty();
    }

    public void addRootRuleInfo(RuleInfoSet inserted) {
        for(RuleInfo rInserted : inserted){
            for(RuleInfo rRoot : ris){
                if(rInserted.getSubs().isDisjoint(rRoot.getSubs())){
                    ris.add(rInserted.combine(rRoot));
                }
            }
            ris.add(rInserted);
        }
    }
}
