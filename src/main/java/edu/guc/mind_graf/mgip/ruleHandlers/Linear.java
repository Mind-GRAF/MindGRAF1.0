package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.HashMap;

import edu.guc.mind_graf.set.RuleInfoSet;

public class Linear extends SIndex{

    HashMap<Integer, RuleInfoSet> ruleInfoMap;

    public Linear() {
        ruleInfoMap = new HashMap<>();
    }

    @Override
    protected void insertIntoMap(RuleInfo ri, int hash) {
        //needs editing
        ruleInfoMap.put(hash, ruleInfoMap.getOrDefault(hash, new RuleInfoSet()).combineAdd(ri));
        
    }
    
}
