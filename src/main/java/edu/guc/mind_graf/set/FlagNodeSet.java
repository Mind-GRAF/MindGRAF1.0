package edu.guc.mind_graf.set;

import java.util.HashSet;

import edu.guc.mind_graf.nodes.FlagNode;

public class FlagNodeSet{

    private HashSet<FlagNode> flagNodes;

    public FlagNodeSet() {
        flagNodes = new HashSet<FlagNode>();
    }

    public HashSet<FlagNode> getFlagNodes() {
        return flagNodes;
    }

    public void setFlagNodes(HashSet<FlagNode> flagNodes) {
        this.flagNodes = flagNodes;
    }

    public void addFlagNode(FlagNode fn) {
        flagNodes.add(fn);
    }

    public void removeFlagNode(FlagNode fn) {
        flagNodes.remove(fn);
    }

    public boolean contains(FlagNode fn) {
        return flagNodes.contains(fn);
    }  

    public void clear() {
        flagNodes.clear();
    }

    public int size() {
        return flagNodes.size();
    }

    public boolean disjoint(FlagNodeSet fns) {
        return flagNodes.stream().noneMatch(fn -> fns.contains(fn));
    } //The noneMatch method returns true if none of the elements match the given condition, and false otherwise. mthod exists cz fns have to be disjoint for ris to be compatible

    public void addFlagNodes(FlagNodeSet fns) {
        flagNodes.addAll(fns.getFlagNodes());
    }

    public FlagNodeSet combine(FlagNodeSet fns) {
        FlagNodeSet newFns = new FlagNodeSet();
        newFns.addFlagNodes(this);
        newFns.addFlagNodes(fns);
        return newFns;
    }

    public FlagNodeSet intersection(FlagNodeSet fns) {
        FlagNodeSet newFns = new FlagNodeSet();
        flagNodes.stream().filter(fn -> fns.contains(fn)).forEach(fn -> newFns.addFlagNode(fn));
        return newFns;
    } // should return a flag node set of the common flag nodes between the two sets

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FlagNodeSet){
            FlagNodeSet fns = (FlagNodeSet) obj;
            if(flagNodes.size() != fns.size())
                return false;
            for(FlagNode fn : flagNodes){
                if(!fns.contains(fn))
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "FlagNodeSet{" +
                "flagNodes=" + flagNodes +
                '}';
    }
}
