package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.HashMap;

import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class Linear extends SIndex{
    public HashMap<Integer, RuleInfoSet> getRuleInfoMap() {
        return ruleInfoMap;
    }

    public void setRuleInfoMap(HashMap<Integer, RuleInfoSet> ruleInfoMap) {
        this.ruleInfoMap = ruleInfoMap;
    }

    HashMap<Integer, RuleInfoSet> ruleInfoMap;

    public Linear() {
        ruleInfoMap = new HashMap<>();
    }

    public Linear(NodeSet commonVariables) {
        super(commonVariables);
        ruleInfoMap = new HashMap<>();
    }

    @Override
    public RuleInfoSet insertIntoMap(RuleInfo ri, int hash) {
        RuleInfoSet afterInsertion = ruleInfoMap.getOrDefault(hash, new RuleInfoSet()).combineAdd(ri);
        ruleInfoMap.put(hash, afterInsertion);
        return afterInsertion;
    }
    
}
