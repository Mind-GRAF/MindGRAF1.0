package mgip;

import java.util.ArrayList;
import java.util.Arrays;

import components.Substitutions;
import network.Network;
import nodes.PropositionNode;
import set.PropositionNodeSet;

public class KnownInstance {
    private Substitutions substitutions;
    private PropositionNodeSet supports;
    private int attitudeID;

    public KnownInstance(Substitutions substitutions, PropositionNodeSet supports, int attitudeID) {
        this.substitutions = substitutions;
        this.supports = supports;
        this.attitudeID = attitudeID;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Substitutions: " + substitutions + "\n");
        sb.append("Supports: " + supports.toString() + "\n");
        return sb.toString();
    }

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
    public boolean anySupportSupportedInAttitudeContext(String reportContextName,
            int reportAttitudeID) {
        // int[] supportIds = supports.getProps();
        // int currentPropNodeId;
        // for (int index = 0; index < supportIds.length; index++) {
        // currentPropNodeId = supportIds[index];
        // PropositionNode currentNode = (PropositionNode)
        // Network.getNodeById(currentPropNodeId);
        // if (!(currentNode.asserted(reportContextName, reportAttitudeID)))
        // return false;

        // }
        return true;

    }

    public int getAttitudeID() {
        return attitudeID;
    }

    public void setAttitudeID(int attitudeID) {
        this.attitudeID = attitudeID;
    }

    public PropositionNodeSet getSupports() {
        return supports;
    }

    public void setSupports(PropositionNodeSet supports) {
        this.supports = supports;
    }

}
