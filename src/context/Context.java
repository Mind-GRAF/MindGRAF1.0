package context;

import java.util.BitSet;
import java.util.Hashtable;

import nodes.Node;
import set.NodeSet;
import set.PropositionNodeSet;

public class Context {

    private Hashtable<Integer, PropositionNodeSet> attitudes;
    private String name;
    private Hashtable<Integer, BitSet> AttitudesBitset;
    private NodeSet suppSet;

    public Context(String currContextName, int attitude, NodeSet assumptions) {
        //TODO Auto-generated constructor stub
    }

    public Context(String currContextName, int attitude, Node orAssumption) {
        //TODO Auto-generated constructor stub
    }

    public Context() {
        //TODO Auto-generated constructor stub
    }

    public Integer getPropositionAttitude(Integer prop) {

        // loop through all the Integer keys of attitudesBitset
        for (Integer key : this.AttitudesBitset.keySet()) {
            // If the propositionNode is in the attitude then return the name of the
            // attitude
            if (this.AttitudesBitset.get(key).get(prop)) {
                return key;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");
    }

    public String getContextName(){
        return this.name;
    }

    public boolean checkNodesPresent(PropositionNodeSet nodeSet) {
        for (Node ant : nodeSet) {
            if (!this.suppSet.contains(ant)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkValidity(NodeSet assumedNodes) {
        //Check for the negation of any of the given nodes in that context.
        throw new UnsupportedOperationException("Unimplemented method 'checkValidity'");
    }

}
