package mindG.mgip;

import mindG.mgip.matching.Substitutions;
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

    public Substitutions getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(Substitutions substitutions) {
        this.substitutions = substitutions;
    }

    public PropositionSet getSupports() {
        return supports;
    }

    public void setSupports(PropositionSet supports) {
        this.supports = supports;
    }

    public boolean anySupportAssertedInAttitudeContext(String currentContext, int currentAttitude) {
        return false;
    }

    public int getAttitudeID() {
        return attitudeID;
    }

    public void setAttitudeID(int attitudeID) {
        this.attitudeID = attitudeID;
    }

    public Report computeReportFromDifferencesToSend(Report report) {
        return null;
    }

}