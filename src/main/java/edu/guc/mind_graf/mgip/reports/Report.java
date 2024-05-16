package edu.guc.mind_graf.mgip.reports;

import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

import java.util.HashMap;

public class Report {
    private Substitutions substitutions;
    private Support support;
    private boolean sign;
    private InferenceType inferenceType;
    private Node requesterNode;
    private int attitude;
    private String contextName;
    private ReportType reportType;
    private Node reporterNode;

    public Report(Substitutions substitution, Support suppt, int attitudeid,
                  boolean sign, InferenceType inference, Node requesterNode, Node reporterNode) {
        this.substitutions = substitution;
        this.attitude = attitudeid;
        this.support = suppt;
        this.requesterNode = requesterNode;
        this.sign = sign;
        this.inferenceType = inference;
        this.reporterNode = reporterNode;
    }

    public String stringifyReport() {
        String reportContextName = this.getContextName();
        int reportAttitudeId = this.getAttitude();
        Substitutions subs = this.getSubstitutions();
        Node requesterNode = this.getRequesterNode();
        String report = "Context " + reportContextName + " and Attitude " + reportAttitudeId + " and substitutions "
                + subs.toString() +
                " to " + ((requesterNode!=null)?requesterNode.getName():"null");
        return report;
    }

    /***
     * this method checks if the nodes that helped in creating the report are
     * supported in the attitude in the context belonging to the report
     * 
     * @param //reportContextName
     * @param //reportAttitudeID
     */
    public boolean anySupportSupportedInAttitudeContext(String chnlContextName, int chnlAttitudeID) {
        // int[] supportIds = support.getProps();
        // int currentPropNodeId;
        // for (int index = 0; index < supportIds.length; index++) {
        // currentPropNodeId = supportIds[index];
        // PropositionNode currentNode = (PropositionNode)
        // Network.getNodeById(currentPropNodeId);
        // if (!(currentNode.asserted(ChnlContextName, ChnlAttitudeID)))
        // return false;

        // }

        Context chnlContext = ContextController.getContext(chnlContextName);
        int level = Network.currentLevel;

        for(Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> currSupport: support.getJustificationSupport().get(level).get(chnlAttitudeID)){
            for(int propID : currSupport.getSecond().getProps()){
                if(Network.getPropositionNodes().containsKey(propID)){
                    PropositionNode prop = (PropositionNode)Network.getPropositionNodes().get(propID);
                    if(!prop.supported(chnlContextName,chnlAttitudeID,level)){
                        return false;
                    }
                }
            }
            for(Integer innerAttitude : currSupport.getFirst().keySet()){
                for(int propID : currSupport.getFirst().get(innerAttitude).getFirst().getProps()){
                    if(Network.getPropositionNodes().containsKey(propID)){
                        PropositionNode prop = (PropositionNode)Network.getPropositionNodes().get(propID);
                        if(!prop.supported(chnlContextName,chnlAttitudeID,level)){
                            return false;
                        }
                    }
                }
                for (int propID : currSupport.getFirst().get(innerAttitude).getSecond().getProps()) {
                    if (Network.getPropositionNodes().containsKey(propID)) {
                        PropositionNode prop = (PropositionNode) Network.getPropositionNodes().get(propID);
                        if (!prop.supported(chnlContextName, chnlAttitudeID, level)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;

    }

    public Substitutions getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(Substitutions substitutions) {
        this.substitutions = substitutions;
    }

    public Support getSupport() {
        return support;
    }

    public void setSupport(Support support) {
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
            case WhenRule:
                this.setReportType(ReportType.WhenRule);
                break;
            case IfRule:
                this.setReportType(ReportType.IfRule);
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


    public Node getReporterNode() {
        return reporterNode;
    }

    public void setReporterNode(Node reporterNode) {
        this.reporterNode = reporterNode;
    }

    public boolean anySupportSupportedInAttitude(Integer integer) {
        // TODO
        return true;
    }

    public Report clone() {
        return new Report(this.substitutions, this.support, this.attitude, this.sign, this.inferenceType, this.requesterNode, this.reporterNode);
    }

}