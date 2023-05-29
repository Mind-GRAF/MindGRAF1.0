package inferenceRules;

import java.util.HashSet;
import java.util.Iterator;

import set.NodeSet;

public class FlagNodeSet implements Iterable<FlagNode>{
    private HashSet<FlagNode> flagNodes;
    
    public FlagNodeSet() {
        flagNodes = new HashSet<>();
    }
    
    public void addFlagNode(FlagNode flagNode) {
        flagNodes.add(flagNode);
    }
    
    public void removeFlagNode(FlagNode flagNode) {
        flagNodes.remove(flagNode);
    }
    
    public FlagNodeSet union(FlagNodeSet x) {
        FlagNodeSet result = new FlagNodeSet();
        result.flagNodes.addAll(this.flagNodes);
        result.flagNodes.addAll(x.flagNodes);
        return result;
    }
    public NodeSet getAllNodes() {
        NodeSet allNodes = new NodeSet();
        for (FlagNode flagNode : flagNodes) {
            allNodes.add(flagNode.getNode());
        }
        return allNodes;
    }
    public int getSize() {
        return flagNodes.size();
    }
    public Iterator<FlagNode> iterator() {
        return flagNodes.iterator();
    }
 
        public boolean contains(FlagNode fn) {
    		
    		for (FlagNode tFn : flagNodes) {
    			if (tFn.isEqual(fn))
    				return true;
    		}
    		return false;
    	}

    
}  
