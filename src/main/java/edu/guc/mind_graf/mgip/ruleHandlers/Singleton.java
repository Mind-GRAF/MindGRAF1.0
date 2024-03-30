package edu.guc.mind_graf.mgip.ruleHandlers;

import edu.guc.mind_graf.nodes.PropositionNode;

import java.util.HashMap;

public class Singleton extends SIndex {

    HashMap<Integer, RuleInfo> ruleInfoMap;

    public Singleton() {
        ruleInfoMap = new HashMap<>();
    }

    @Override
    public void insertIntoMap(RuleInfo ri, int hash) {
        ruleInfoMap.put(hash, ri.combine(ruleInfoMap.getOrDefault(hash, new RuleInfo())));
    }

    public HashMap<Integer, RuleInfo> getRuleInfoMap() {
        return ruleInfoMap;
    }

}
