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

}
