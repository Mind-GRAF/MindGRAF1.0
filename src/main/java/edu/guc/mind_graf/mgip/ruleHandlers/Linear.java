package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.HashMap;
import java.util.HashSet;

import edu.guc.mind_graf.nodes.Node;
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

    public Linear(HashSet<Node> commonVariables) {
        super(commonVariables);
        ruleInfoMap = new HashMap<>();
    }

    @Override
    public void insertIntoMap(RuleInfo ri, int hash) {
        //needs editing
        ruleInfoMap.put(hash, ruleInfoMap.getOrDefault(hash, new RuleInfoSet()).combineAdd(ri));
        
    }
    
}
