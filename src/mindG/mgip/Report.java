package mindG.mgip;

import mindG.mgip.matching.Substitutions;
import mindG.network.Node;

public class Report {
    private Substitutions substitutions;
    private PropositionSet support;
    private boolean sign;
    private InferenceTypes inferenceType;
    private Node requesterNode;
    private int attitude;

    public Report(Substitutions substitution, PropositionSet suppt, int attitudeid,
            boolean sign, InferenceTypes inference, Node requesterNode) {
        this.substitutions = substitution;
        this.attitude = attitudeid;
        this.support = suppt;
        this.requesterNode = requesterNode;
        this.sign = sign;
        this.inferenceType = inference;
    }

    // This method checks if any of the Report’s support is asserted in
    // reportContext.

    public boolean anySupportAssertedInAttitudeContext(String reportContextName, int reportAttitudeID) {
        return sign;

    }

    /***
     * Handling checks done in processing single report, evaluating differences to
     * know what
     * This method is used through attemptAddingReportToKnownInstances(channel,
     * report) in 3.2.1 to create the report we needed to broadcast while trying to
     * add report
     * to the node’s knownInstances.
     * 
     * @param report
     * @return
     */
    public Report computeReportFromDifferencesToSend(Report report) {
        return report;

    }

    public InferenceTypes getInferenceType() {
        return null;
    }

    public Substitutions getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(Substitutions substitutions) {
        this.substitutions = substitutions;
    }

    public PropositionSet getSupport() {
        return support;
    }

    public void setSupport(PropositionSet support) {
        this.support = support;
    }

    public boolean isSign() {
        return sign;
    }

    public void setSign(boolean sign) {
        this.sign = sign;
    }

    public void setInferenceType(InferenceTypes inferenceType) {
        this.inferenceType = inferenceType;
    }

    public int getAttitude() {
        return attitude;
    }

    public void setAttitude(int attitude) {
        this.attitude = attitude;
    }

    public Node getRequesterNode() {
        return requesterNode;
    }

    public void setRequesterNode(Node requesterNode) {
        this.requesterNode = requesterNode;
    }

}