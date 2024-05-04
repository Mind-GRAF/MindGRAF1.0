package edu.guc.mind_graf.context;

import java.util.BitSet;
import java.util.Hashtable;

import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.Set;

public class Context {

    private Hashtable<Integer, PropositionNodeSet> attitudes;
    private String name;
    private Set<Integer, BitSet> AttitudesBitset;

    
    public Context(String name, Set<String,Integer> attitudeNames){
        this.name=name;
        //TODO Wael: set the attitude names
    }

    public Context(String name, int attitude, NodeSet assumedNodes){ //Constructor for assumed contexts
        this.name = name;
        //TODO Wael: set the attitude names and Supported Nodes Set adjustment
    }
    public Integer getPropositionAttitude(Integer prop) {

        // loop through all the Integer keys of attitudesBitset
        for (Integer key : this.AttitudesBitset.getSet().keySet()) {
            // If the propositionNode is in the attitude then return the name of the
            // attitude
            if (this.AttitudesBitset.get(key).get(prop)) {
                return key;
            }
        }
        throw new RuntimeException("PropositionNode is not in any attitude");
    }
    
    public String getName() {
        return name;
    }

    public boolean isSubset(Context context) {
        //TODO Wael Checks if this.context is subset of another Context context (context)
        return true;
    }


//    public PropositionNodeSet getAllPropositionsInAnAttitude(int attitude) {
//        return this.attitudes.get(attitude);
//    }

}
