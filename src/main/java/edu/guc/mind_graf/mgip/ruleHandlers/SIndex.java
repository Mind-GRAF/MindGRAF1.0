package edu.guc.mind_graf.mgip.ruleHandlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.nodes.Node;

public abstract class SIndex extends RuleInfoHandler {

    private HashSet<Node> commonVariables;

    public SIndex() {
        commonVariables = new HashSet<>();
    }

    public SIndex(HashSet<Node> commonVariables) {
        this.commonVariables = commonVariables;
    }

    public Set<Node> getCommonVariables() {
        return Collections.unmodifiableSet(commonVariables);
    }

    public void setCommonVariables(HashSet<Node> commonVariables) {
        this.commonVariables = commonVariables;
    }

    public SIndex createSIndex(HashSet<Node> commonVariables) {
        return new Linear(commonVariables);
    }

    protected int customHash(Substitutions subs) {
        int[] orderedArray = new int[commonVariables.size()];
        int index = 0;
        for(Node var : commonVariables){
            orderedArray[index++] = subs.get(var).getId();
        }
        //assuming ids wont be over 100
        int hash = orderedArray[0] + orderedArray[1] * 100 + orderedArray[2] * 10000;
        return hash;
    }

    @Override
    public void insertVariableRI(RuleInfo ri) throws InvalidRuleInfoException {
        int hash = customHash(ri.getSubs());
        insertIntoMap(ri, hash);
    }

    public abstract void insertIntoMap(RuleInfo ri, int hash);

    @Override
    public void clear() {
        commonVariables.clear();
    }

}