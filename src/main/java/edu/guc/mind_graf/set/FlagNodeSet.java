package edu.guc.mind_graf.set;

import java.util.HashSet;
import java.util.Iterator;

import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
import edu.guc.mind_graf.nodes.Node;

public class FlagNodeSet implements Iterable<FlagNode>{

    private HashSet<FlagNode> flagNodes;

    public FlagNodeSet(FlagNode... fns) {
        this.flagNodes = new HashSet<>();
        for (FlagNode n : fns)
            this.flagNodes.add(n);
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
        for(FlagNode flagNode : flagNodes){
            if(flagNode.equals(fn))
                return true;
        }
        return false;
    }  

    public void clear() {
        flagNodes.clear();
    }

    public int size() {
        return flagNodes.size();
    }

    public boolean disjoint(FlagNodeSet fns) {
        return flagNodes.stream().noneMatch(fn -> fns.contains(fn));
    } //The noneMatch method returns true if none of the elements match the given condition, and false otherwise. method exists cz fns have to be disjoint for ris to be compatible

    public void addFlagNodes(FlagNodeSet fns) {
        flagNodes.addAll(fns.getFlagNodes());
    }

    public FlagNodeSet combine(FlagNodeSet fns) {
        FlagNodeSet newFns = new FlagNodeSet();
        newFns.addFlagNodes(this);
        for(FlagNode fn : fns.getFlagNodes()){
            if(!newFns.contains(fn))
                newFns.addFlagNode(fn);
        }
        return newFns;
    }

    public FlagNodeSet intersection(FlagNodeSet fns) {
        FlagNodeSet newFns = new FlagNodeSet();
        flagNodes.stream().filter(fn -> fns.contains(fn)).forEach(fn -> newFns.addFlagNode(fn));
        return newFns;
    } // should return a flag node set of the common flag nodes between the two sets

    public boolean containsNode(Node node){
        for(FlagNode fn : flagNodes){
            if(fn.getNode().equals(node))
                return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FlagNodeSet fns){
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
        String s = "{";
        for(FlagNode fn : flagNodes){
            s += fn;
        }
        s += "}";
        return s;
    }

    public FlagNodeSet clone() {
        FlagNodeSet newFns = new FlagNodeSet();
        newFns.addFlagNodes(this);
        return newFns;
    }

    @Override
    public Iterator<FlagNode> iterator() {
        return flagNodes.iterator();
    }

    public boolean isEmpty() {
        return flagNodes.isEmpty();
    }
}
