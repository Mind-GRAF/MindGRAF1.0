package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.HashMap;

public class Singleton extends SIndex {

    HashMap<Integer, RuleInfo> ruleInfoMap;

    public Singleton() {
        ruleInfoMap = new HashMap<>();
    }

    @Override
    public RuleInfo insertIntoMap(RuleInfo ri, int hash) {
        ruleInfoMap.put(hash, ruleInfoMap.getOrDefault(hash, new RuleInfo()).combine(ri));
        return ruleInfoMap.get(hash);
    }

    public HashMap<Integer, RuleInfo> getRuleInfoMap() {
        return ruleInfoMap;
    }

}
