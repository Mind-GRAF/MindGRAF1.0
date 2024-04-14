package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.HashMap;

public class Singleton extends SIndex {

    HashMap<Integer, RuleInfo> ruleInfoMap;

    public Singleton() {
        ruleInfoMap = new HashMap<>();
    }

    @Override
    public RuleInfoSet insertIntoMap(RuleInfo ri, int hash) {
        ruleInfoMap.put(hash, ri.combine(ruleInfoMap.getOrDefault(hash, new RuleInfo())));
        RuleInfoSet afterInsertion = new RuleInfoSet();
        afterInsertion.addRuleInfo(ruleInfoMap.get(hash));
        return afterInsertion;
    }

    @Override
    public RuleInfoSet getAllRuleInfos() {
        RuleInfoSet allRuleInfos = new RuleInfoSet();
        for (RuleInfo ri : ruleInfoMap.values()) {
            allRuleInfos.addRuleInfo(ri);
        }
        return allRuleInfos;
    }

    public HashMap<Integer, RuleInfo> getRuleInfoMap() {
        return ruleInfoMap;
    }

}
