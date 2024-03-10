package mgip;

import mgip.matching.Substitutions;
import mgip.requests.ChannelType;
import mgip.ReportType;
import network.Network;
import nodes.Node;
import nodes.PropositionNode;
import set.PropositionSet;

public class Report {
    private Substitutions substitutions;
    private PropositionSet support;
    private boolean sign;
    private InferenceType inferenceType;
    private Node requesterNode;
    private int attitude;
    private String contextName;
    private ReportType reportType;

    public Report(Substitutions substitution, PropositionSet suppt, int attitudeid,
            boolean sign, InferenceType inference, Node requesterNode) {
        this.substitutions = substitution;
        this.attitude = attitudeid;
        this.support = suppt;
        this.requesterNode = requesterNode;
        this.sign = sign;
        this.inferenceType = inference;
    }

    public String stringifyReport() {
        String reportContextName = this.getContextName();
        int reportAttitudeId = this.getAttitude();
        Substitutions subs = this.getSubstitutions();
        Node requesterNode = this.getRequesterNode();
        String report = "Context " + reportContextName + " and Attitude " + reportAttitudeId + " and substitutions "
                + subs.toString() +
                " to " + requesterNode.getName();
        return report;
    }

    /***
     * this method checks if the nodes that helped in creating the report are
     * supported in the attitude in the context belonging to the report
     * 
     * @param reportContextName
     * @param reportAttitudeID
     */
    public boolean anySupportSupportedInAttitudeContext(String ChnlContextName, int ChnlAttitudeID) {
        // int[] supportIds = support.getProps();
        // int currentPropNodeId;
        // for (int index = 0; index < supportIds.length; index++) {
        // currentPropNodeId = supportIds[index];
        // PropositionNode currentNode = (PropositionNode)
        // Network.getNodeById(currentPropNodeId);
        // if (!(currentNode.asserted(ChnlContextName, ChnlAttitudeID)))
        // return false;

        // }
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
            case Matched:
                this.setReportType(ReportType.Matched);
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

    public InferenceType getInferenceType() {
        return inferenceType;
    }

    public void setInferenceType(InferenceType inferenceType) {
        this.inferenceType = inferenceType;
    }

}