package edu.guc.mind_graf.context;

import java.util.BitSet;
import java.util.Hashtable;

import edu.guc.mind_graf.set.PropositionNodeSet;

public class Context {

    private Hashtable<Integer, PropositionNodeSet> attitudes;
    private String name;
    private Hashtable<Integer, BitSet> AttitudesBitset;

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

}
