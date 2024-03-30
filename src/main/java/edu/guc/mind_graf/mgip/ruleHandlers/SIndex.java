package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;

public abstract class SIndex extends RuleInfoHandler {

    private HashMap<Integer, Object> ruleInfoMap;

    private Set<Node> commonVariables; 

    public SIndex() {
        ruleInfoMap = new HashMap<>();
        commonVariables = new HashSet<>();
    }

    public SIndex(Set<Node> commonVariables) {
        ruleInfoMap = new HashMap<>();
        this.commonVariables = commonVariables;
    }

    protected int customHash(ArrayList<String> set) {
        Collections.sort(set); // Sort the strings
        return Objects.hash(set.toArray());
    }

    @Override
    public void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        ArrayList<String> key = new ArrayList<>();
        for(Node node : commonVariables){
            String value = ri.getSubs().get(node).toString();
            if(value != null)
                key.add(value);
            else
                throw new InvalidRuleInfoException("Common Substitution not found in RuleInfo");
        }
        int hash = customHash(key);
        insertIntoMap(ri, hash);
    }

    protected abstract RuleInfo insertIntoMap(RuleInfo ri, int hash);

    @Override
    public void clear() {
        ruleInfoMap.clear();
    }

}