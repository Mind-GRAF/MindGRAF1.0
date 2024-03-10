package context;

import java.util.BitSet;
import java.util.Hashtable;

import set.PropositionSet;

public class Context {

    private Hashtable<Integer, PropositionSet> attitudes;
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
