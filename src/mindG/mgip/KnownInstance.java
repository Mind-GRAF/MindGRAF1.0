package mindG.mgip;

import java.util.ArrayList;
import java.util.Arrays;

import mindG.mgip.matching.Substitutions;
import mindG.network.PropositionNode;
import mindG.network.PropositionSet;

public class KnownInstance {
    private Substitutions substitutions;
    private PropositionSet supports;
    private int attitudeID;

    public KnownInstance(Substitutions substitutions, PropositionSet supports, int attitudeID) {
        this.substitutions = substitutions;
        this.supports = supports;
        this.attitudeID = attitudeID;
    }

    // public String toString() {
    // StringBuilder sb = new StringBuilder();
    // sb.append("Substitutions: " + substitutions + "\n");
    // sb.append("Supports: " + Arrays.deepToString(supports) + "\n");
    // sb.append("Attitude ID: " + attitudeID + "\n");
    // sb.append("Sign: " + sign + "\n");
    // return sb.toString();
    // }
    public Substitutions getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(Substitutions substitutions) {
        this.substitutions = substitutions;
    }

    /***
     * this method checks if the nodes that helped in creating the report are
     * supported in the attitude in the context belonging to the report
     * 
     * @param reportContextName
     * @param reportAttitudeID
     */
    public boolean anySupportAssertedInAttitudeContext(String reportContextName,
            int reportAttitudeID) {
        for (PropositionNode propositionNode : supports) {
            if (!(propositionNode.asserted(reportContextName, reportAttitudeID)))
                return false;

        }
        return true;

    }

    public int getAttitudeID() {
        return attitudeID;
    }

    public void setAttitudeID(int attitudeID) {
        this.attitudeID = attitudeID;
    }

    public PropositionSet getSupports() {
        return supports;
    }

    public void setSupports(PropositionSet supports) {
        this.supports = supports;
    }

}
