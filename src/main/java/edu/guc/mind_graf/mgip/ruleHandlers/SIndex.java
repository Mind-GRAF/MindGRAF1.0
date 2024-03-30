package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.guc.mind_graf.components.Substitutions;
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

    protected int customHash(Substitutions subs) {
        int[] orderedArray = new int[commonVariables.size()];
        int index = 0;
        for(Node var : commonVariables){
            orderedArray[index++] = subs.get(var).getId();
        }
        return Objects.hash(orderedArray);
    }

    @Override
    public void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        int hash = customHash(ri.getSubs());
        insertIntoMap(ri, hash);
    }

    protected abstract void insertIntoMap(RuleInfo ri, int hash);

    @Override
    public void clear() {
        ruleInfoMap.clear();
    }

}