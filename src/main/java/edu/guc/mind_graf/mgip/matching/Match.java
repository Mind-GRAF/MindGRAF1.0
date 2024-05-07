package edu.guc.mind_graf.mgip.matching;

import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;

public class Match {
    private Substitutions filterSubs; // whquestion atleastone free not bound
    // no filter on match channel
    // target (switch el heya variables to variables) <- source (filter el heya
    // variables to constants)
    private Substitutions switchSubs;
    private Node node;
    private int matchType;
    private Object support;

    public Match(Substitutions filterSubs, Substitutions switchSubs, Node node, int matchType, Object support) {
        this.filterSubs = filterSubs;
        this.switchSubs = switchSubs;
        this.node = node;
        this.matchType = matchType;
        this.support = support;
    }

    public Substitutions getFilterSubs() {
        return filterSubs;
    }

    public void setFilterSubs(Substitutions filterSubs) {
        this.filterSubs = filterSubs;
    }

    public Substitutions getSwitchSubs() {
        return switchSubs;
    }

    public void setSwitchSubs(Substitutions switchSubs) {
        this.switchSubs = switchSubs;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public int getMatchType() {
        return matchType;
    }

    public void setMatchType(int matchType) {
        this.matchType = matchType;
    }

    public Object getSupport() {
        return support;
    }

    public void setSupport(Object support) {
        this.support = support;
    }

    public boolean isDuplicate(Match other) {
        return this != other && node.equals(other.node) && matchType == other.matchType
                && filterSubs.equals(other.filterSubs) && switchSubs.equals(other.switchSubs)
                && ((NodeSet) support).equals(((NodeSet) other.getSupport()));
    }

    public Match clone() {
        return new Match(filterSubs.clone(), switchSubs.clone(), node, matchType, ((NodeSet) support).clone());
    }
}