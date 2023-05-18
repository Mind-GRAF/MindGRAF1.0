package mindG.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.crypto.dsig.spec.XPathType.Filter;

import mindG.mgip.KnownInstance;
import mindG.mgip.KnownInstanceSet;
import mindG.mgip.InferenceType;
import mindG.mgip.Report;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Match;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.AntecedentToRuleChannel;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.ChannelSet;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.requests.MatchChannel;
import mindG.mgip.ReportType;
import mindG.mgip.requests.Request;
import mindG.mgip.rules.AndOr;
import mindG.mgip.rules.Thresh;
import mindG.network.cables.DownCableSet;

public abstract class RuleNode extends PropositionNode implements Serializable {

    protected boolean forwardReport;

    public RuleNode(String name, Boolean isVariable) {
        super(name, isVariable);

    }

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);

    }

    // public Collection<RuleResponse> applyRuleHandler(Report report, Channel
    // currentRequest) {
    // return null;

    // }
    // This method is implemented to send requests to antecedents that are not
    // working
    // on a similar request to currentRequest.

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, KnownInstance knownInstance) {
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        int currentAttitudeID = currentChannel.getAttitudeID();
        Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
        Substitutions reportSubs = knownInstance.getSubstitutions();
        Substitutions unionSubs = Substitutions.union(filterSubs, reportSubs);

        NodeSet argumentsCloseToMe = getDownAntArgNodeSet();
        NodeSet argNodesToConsiderClose = removeAlreadyEstablishedChannels(argumentsCloseToMe,
                currentRequest,
                unionSubs, ruleType);
        sendRequestsToNodeSet(argNodesToConsiderClose, unionSubs, switchSubs, currentContextName,
                currentAttitudeID,
                ChannelType.AntRule, this);
    }

    // This method is implemented to do certain actions after calling
    // applyRuleHandler()
    // and receive ruleResponses
    // public void handleResponseOfApplyRuleHandler(Collection<RuleResponse>
    // ruleResponses, Report currentReport,
    // Channel currentRequest) {

    // }

    // This method iterates over all the node’s outgoing channels calling on each
    // processSingleRequestsChannel(outgoingRequest) and handles the exceptions that
    // might be
    // thrown.

    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().poll();
        try {
            processSingleRequests(requestHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest) {
        Substitutions filterRuleSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchRuleSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        String currentContext = currentRequest.getChannel().getContextName();
        int currentAttitude = currentRequest.getChannel().getAttitudeID();
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;

        NodeSet antArgNodeSet = getDownAntArgNodeSet();
        NodeSet remainingAntArgNodeSet = removeAlreadyEstablishedChannels(antArgNodeSet,
                currentRequest,
                filterRuleSubs, ruleType);
        sendRequestsToNodeSet(remainingAntArgNodeSet, filterRuleSubs, switchRuleSubs, currentContext,
                currentAttitude,
                ChannelType.AntRule, this);
        return;

    }

    /***
     * Method comparing opened outgoing channels over each match's node of the
     * matches whether a more generic request of the specified channel was
     * previously sent in order not to re-send redundant requests -- ruleType gets
     * applied on Andor or Thresh part.
     * 
     * @param nodes
     * @param currentRequest
     * @param toBeCompared
     * @param ruleType
     * @return
     */
    protected static NodeSet removeAlreadyEstablishedChannels(NodeSet nodes, Request currentRequest,
            Substitutions toBeCompared,
            boolean ruleType) {
        NodeSet nodesToConsider = new NodeSet();
        for (Node currentNode : nodes) {
            boolean notTheSame = !ruleType
                    || currentNode.getId() != currentRequest.getChannel().getRequesterNode().getId();
            if (notTheSame) {
                ChannelSet outgoingChannels = ((PropositionNode) currentNode).getOutgoingChannels();
                for (Channel outgoingChannel : outgoingChannels) {
                    Substitutions processedRequestChannelFilterSubs = outgoingChannel
                            .getFilterSubstitutions();
                    notTheSame &= !processedRequestChannelFilterSubs.isSubsetOf(toBeCompared)
                            && outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode()
                                    .getId();
                }
                if (notTheSame) {
                    nodesToConsider.add(currentNode);

                }
            }

        }
        return nodesToConsider;

    }

    // ha3mel method lel bridge rule fel process requests be casee el heya el
    // different attitudes

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel)
            super.processSingleRequests(currentRequest);

        else {
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();

            if (!this.isOpen()) {
                if (this.asserted(currentContext, currentAttitude)) {
                    boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                    NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                    NodeSet antArgNodesToConsiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                            currentRequest,
                            filterRuleSubs, ruleType);
                    sendRequestsToNodeSet(antArgNodesToConsiderClose, filterRuleSubs, switchRuleSubs, currentContext,
                            currentAttitude,
                            ChannelType.AntRule, this);

                } else
                    super.processSingleRequests(currentRequest);

            } else {
                boolean isNotBound = isOpenNodeNotBound(filterRuleSubs);
                Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                        currentChannel.getAttitudeID());
                for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                    Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                    Substitutions onlySubsBindFreeVar = onlyRelevantSubs(filterRuleSubs);
                    boolean subSetCheck = onlySubsBindFreeVar
                            .isSubsetOf(currentKISubs);
                    boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                            currentContext,
                            currentAttitude);
                    if (subSetCheck && supportCheck) {
                        if (!isNotBound) {
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                            return;
                        } else
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentKnownInstance);
                    }

                }
                super.processSingleRequests(currentRequest);
                return;

            }

        }

    }

    /***
     * Report handling in Rule proposition nodes.
     */
    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().poll();
        try {
            processSingleReports(reportHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    protected void processSingleReports(Report currentReport) {
        String currentReportContextName = currentReport.getContextName();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;
        Channel tempChannel = new Channel(null, currentReportSubs, currentReportContextName,
                currentReportAttitudeID, currentReport.getRequesterNode());
        Request tempRequest = new Request(tempChannel, null);
        boolean assertedInContext = asserted(currentReportContextName, currentReportAttitudeID);
        boolean closedTypeTerm = true;
        if (currentReport.getReportType() == ReportType.AntRule) {
            /** AntecedentToRule Channel */
            if (forwardReportType) {
                /** Forward Inference */
                if (closedTypeTerm) {
                    /** Close Type Implementation */
                    if (assertedInContext) {

                        requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                        //// Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                        //// tempChannel);
                        // handleResponseOfApplyRuleHandler(ruleResponse, currentReport, tempChannel);
                    } else {
                        NodeSet dominatingRules = getUpAntDomRuleNodeSet();
                        NodeSet toBeSentToDom = removeAlreadyEstablishedChannels(dominatingRules, tempRequest,
                                currentReportSubs, false);
                        sendRequestsToNodeSet(toBeSentToDom, currentReportSubs, null, currentReportContextName,
                                currentReportAttitudeID,
                                ChannelType.AntRule, this);
                        List<Match> matchesList = new ArrayList<Match>();
                        // Matcher.match(this, ruleNodeExtractedSubs);
                        List<Match> toBeSentToMatch = removeAlreadyEstablishedChannels(matchesList, tempRequest,
                                currentReportSubs);
                        sendRequestsToMatches(toBeSentToMatch, currentReportSubs, null, currentReportContextName,
                                currentReportAttitudeID, ChannelType.MATCHED, this);
                    }
                } else {
                    /* always sue the extracted report subs in the requests */
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            tempChannel.getAttitudeID());
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean subSetCheck = tempRequest.getChannel().getFilterSubstitutions()
                                .isSubsetOf(currentKISubs);

                        boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (subSetCheck && supportCheck) {
                            requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);

                            // Collection<RuleResponse> ruleResponse = applyRuleHandler(knownInstance,
                            // currentChannel);
                            // handleResponseOfApplyRuleHandler(ruleResponse, knownInstance,
                            // currentChannel);
                        }
                    }
                    // Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                    // currentChannel);
                    // handleResponseOfApplyRuleHandler(ruleResponse, currentReport,
                    // currentChannel);
                    NodeSet dominatingRules = this.getUpCableSet().get("consequent").getNodeSet();

                    NodeSet toBeSentToDom = removeAlreadyEstablishedChannels(dominatingRules, tempRequest,
                            currentReportSubs, false);
                    Substitutions switchSubs = new Substitutions();
                    sendRequestsToNodeSet(toBeSentToDom, currentReportSubs, switchSubs, currentReportContextName,
                            currentReportAttitudeID,
                            ChannelType.RuleCons, this);
                    NodeSet dominatingThreshAndOrRules = this.getUpCableSet().get("args").getNodeSet();
                    NodeSet actualThreshAndOr = new NodeSet();
                    for (Node currentNode : dominatingThreshAndOrRules) {
                        if (currentNode instanceof Thresh || currentNode instanceof AndOr)
                            actualThreshAndOr.add(currentNode);

                    }
                    NodeSet remainingThreshAndOrNodes = removeAlreadyEstablishedChannels(actualThreshAndOr,
                            tempRequest, currentReportSubs, true);
                    sendRequestsToNodeSet(remainingThreshAndOrNodes, currentReportSubs, switchSubs,
                            currentReportContextName,
                            currentReportAttitudeID,
                            ChannelType.RuleCons, this);
                    List<Match> matchingNodes = new ArrayList<>();
                    // Matcher.match(this, ruleNodeExtractedSubs);
                    List<Match> toBeSentToMatch = removeAlreadyEstablishedChannels(matchingNodes, tempRequest,
                            currentReportSubs);
                    sendRequestsToMatches(toBeSentToMatch, currentReportSubs, null, currentReportContextName,
                            currentReportAttitudeID, ChannelType.MATCHED, this);
                    /*
                     * zeyada 3aleiha hnkamel akenaha mesh asserted el heya open
                     */
                }
            } else {
                /** Backward Inference */
                // Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                // currentChannel);
                // handleResponseOfApplyRuleHandler(ruleResponse, currentReport,
                // currentChannel);
                // currentChannelReportBuffer.removeReport(currentReport);
            }
        } else {
            /** Not AntecedentToRule Channel */
            super.processSingleReports(currentReport);
            if (forwardReportType) {
                if (closedTypeTerm)
                    // Scheduler.addNodeAssertionThroughFReport(currentReport, this);
                    getNodesToSendRequest(ChannelType.AntRule, currentReportContextName, currentReportAttitudeID,
                            currentReportSubs);
            } else {
                Collection<Channel> outgoingChannels = getOutgoingChannels().getChannels();
                Collection<String> incomingChannels = getIncomingChannels().getAntRuleChannels();
                // boolean existsForwardReportBuffers = false;
                // for (Channel incomingChannel : incomingChannels) {
                // existsForwardReportBuffers |=
                // !incomingChannel.getReportsBuffer().hasForwardReports();
                // }
                if (!outgoingChannels.isEmpty())
                    super.receiveRequest(tempRequest);
                if (!incomingChannels.isEmpty())
                    super.receiveReport(currentReport);
            }
        }

    }

    public boolean isForwardReport() {
        return forwardReport;
    }

    public void setForwardReport(boolean forwardReport) {
        this.forwardReport = forwardReport;
    }

    public static void main(String[] args) {

        int x = 5;
        int y = 6;
        boolean ruleType = true; // it is thresh or AndOr
        System.out.println(x != y);
        System.out.println(!ruleType);
        boolean notTheSame = !ruleType
                || x != y;
        System.out.println(notTheSame);
    }

}
