package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.HashMap;

public class Singleton extends SIndex {

    HashMap<Integer, RuleInfo> ruleInfoMap;

    @Override
    protected RuleInfo insertIntoMap(RuleInfo ri, int hash) {
        ruleInfoMap.put(hash, ruleInfoMap.getOrDefault(hash, new RuleInfo()).combine(ri));
        return ruleInfoMap.get(hash);
    }

}
