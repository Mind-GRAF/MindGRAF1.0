package mindG.mgip;

import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.ReportType;
import mindG.network.Node;
import mindG.network.PropositionNode;
import mindG.network.PropositionSet;

public class Report {
    private Substitutions substitutions;
    private PropositionSet support;
    private boolean sign;
    private InferenceTypes inferenceType;
    private Node requesterNode;
    private int attitude;
    private String contextName;
    private ReportType reportType;

    public Report(Substitutions substitution, PropositionSet suppt, int attitudeid,
            boolean sign, InferenceTypes inference, Node requesterNode) {
        this.substitutions = substitution;
        this.attitude = attitudeid;
        this.support = suppt;
        this.requesterNode = requesterNode;
        this.sign = sign;
        this.inferenceType = inference;
    }

    /***
     * this method checks if the nodes that helped in creating the report are
     * supported in the attitude in the context belonging to the report
     * 
     * @param reportContextName
     * @param reportAttitudeID
     */
    // @TODO Ahmed is responsible for supports
    public boolean anySupportAssertedInAttitudeContext(String reportContextName, int reportAttitudeID) {
        for (PropositionNode propositionNode : support) {
            if (!(propositionNode.asserted(reportContextName, reportAttitudeID)))
                return false;

        }
        return true;

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

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public void setReportType(ChannelType channelType) {
        switch (channelType) {
            case MATCHED:
                this.setReportType(ReportType.MATCHED);
                break;
            case AntRule:
                this.setReportType(ReportType.AntRule);
                break;
            case RuleCons:
                this.setReportType(ReportType.RuleCons);
                break;
            default:
                // handle error or do nothing
                break;
        }
    }

    public InferenceTypes getInferenceType() {
        return inferenceType;
    }

    public void setInferenceType(InferenceTypes inferenceType) {
        this.inferenceType = inferenceType;
    }

}