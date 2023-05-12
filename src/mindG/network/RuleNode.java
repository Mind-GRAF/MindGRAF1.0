package mindG.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import javafx.util.Pair;
import mindG.mgip.KnownInstance;
import mindG.mgip.InferenceTypes;
import mindG.mgip.Report;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Match;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.AntecedentToRuleChannel;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.StringChannelSet;
import mindG.mgip.requests.ChannelSet;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.requests.MatchChannel;
import mindG.mgip.ReportType;
import mindG.mgip.requests.Request;
import mindG.mgip.rules.AndOr;
import mindG.mgip.rules.Thresh;

public abstract class RuleNode extends PropositionNode implements Serializable {

    public RuleNode() {
    }

    // public Collection<RuleResponse> applyRuleHandler(Report report, Channel
    // currentRequest) {
    // return null;

    // }
    // This method is implemented to send requests to antecedents that are not
    // working
    // on a similar request to currentRequest.

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, KnownInstance almostReport) {
        NodeSet antecedentNodeSet = new NodeSet();
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        int currentAttitudeID = currentChannel.getAttitudeID();
        Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        Substitutions reportSubs = almostReport.getSubstitutions();
        Substitutions unionSubs = filterSubs.union(filterSubs, reportSubs);
        NodeSet toBeSentTo = removeAlreadyOpenChannels(antecedentNodeSet, currentRequest, unionSubs, ruleType);
        sendRequestsToNodeSet(toBeSentTo, unionSubs, currentContextName,
                currentAttitudeID, ChannelType.AntRule, this);
    }

    // This method is implemented to do certain actions after calling
    // applyRuleHandler()
    // and receive ruleResponses
    // public void handleResponseOfApplyRuleHandler(Collection<RuleResponse>
    // ruleResponses, Report currentReport,
    // Channel currentRequest) {

    // }

    /***
     * This method simply filters channelSet by removing any requests identical to
     * any
     * of the already established outgoing channels. * avoid redundancy
     * 
     * @param requestSet
     * @return
     */
    private Collection<Channel> removeExistingOutgoingChannelsFromSet(Collection<Channel> requestSet) {
        return null;
    }

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

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, boolean removeSender) {

        Substitutions filterRuleSubs = currentRequest.getChannel().getFilterSubstitutions();
        String currentContext = currentRequest.getChannel().getContextName();
        int currentAttitude = currentRequest.getChannel().getAttitudeID();
        NodeSet antecedentNodesToMe = new NodeSet();
        // hena el mafrood a7ot fel nodeset dih kol el antecedents leya
        if (removeSender)
            antecedentNodesToMe.remove(currentRequest.getChannel().getRequesterNode());
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
        NodeSet antNodesToConsider = removeAlreadyOpenChannels(
                antecedentNodesToMe,
                currentRequest,
                filterRuleSubs, ruleType);
        sendRequestsToNodeSet(antNodesToConsider, filterRuleSubs, currentContext,
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
    protected static NodeSet removeAlreadyOpenChannels(NodeSet nodes, Request currentRequest,
            Substitutions toBeCompared,
            boolean ruleType) {
        NodeSet nodesToConsider = new NodeSet();
        for (Node currentNode : nodes) {
            // if it is not of AndOr aw thresh eh el beyehssal?
            if (ruleType) {
                boolean notTheSame = currentNode.getId() != currentRequest.getChannel().getRequesterNode().getId();
                if (notTheSame) {
                    ChannelSet outgoingChannels = ((PropositionNode) currentNode).getOutgoingChannels();
                    for (Channel outgoingChannel : outgoingChannels) {
                        Substitutions processedRequestChannelFilterSubs = outgoingChannel
                                .getFilterSubstitutions();
                        notTheSame &= !Substitutions.isSubSet(processedRequestChannelFilterSubs,
                                toBeCompared)
                                && outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode()
                                        .getId();
                    }
                    if (notTheSame) {
                        nodesToConsider.add(currentNode);

                    }
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
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel) {
            super.processSingleRequests(currentRequest);

        } else {
            boolean closedTypeNode = nodeType(this);
            // mehtaga acheck hena el type beta3 el node fa dih men el network
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            if (closedTypeNode) {
                if (super.asserted(currentContext, currentAttitude)) {
                    NodeSet antecedentNodesToMeClose = new NodeSet();
                    // hena el mafrood a7ot fel nodeset dih kol el antecedents leya
                    boolean ruleTypeClose = this instanceof Thresh || this instanceof AndOr;
                    NodeSet antNodesToConsiderClose = removeAlreadyOpenChannels(antecedentNodesToMeClose,
                            currentRequest,
                            filterRuleSubs, ruleTypeClose);
                    sendRequestsToNodeSet(antNodesToConsiderClose, filterRuleSubs, currentContext, currentAttitude,
                            ChannelType.AntRule, this);

                } else {
                    super.processSingleRequests(currentRequest);
                }

            } else {
                if (super.isVariableNode(this)) {
                    boolean isBound = false;
                    VariableSet freeVariableSet = super.getFreeVariables(this);
                    for (Node freeVariable : freeVariableSet) {
                        isBound = filterRuleSubs.isFreeVariableBound(freeVariable);
                        if (!isBound)
                            break;

                    }

                    Collection<KnownInstance> theAlmostReportsSet = this.getKnownInstances().mergeKInstancesBasedOnAtt(
                            currentChannel.getAttitudeID());
                    // hena based 3ala attribute fa bab3at el known instance
                    for (KnownInstance currentAlmostReport : theAlmostReportsSet) {
                        Substitutions currentAlmostSubs = currentAlmostReport.getSubstitutions();
                        boolean subSetCheck = Substitutions.isSubSet(currentAlmostSubs,
                                currentRequest.getChannel().getFilterSubstitutions());

                        boolean supportCheck = currentAlmostReport.anySupportAssertedInAttitudeContext(
                                currentContext,
                                currentAttitude);
                        if (subSetCheck && supportCheck) {
                            if (isBound) {
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest, false);
                                return;
                            } else
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentAlmostReport);
                        }

                    }
                    super.processSingleRequests(currentRequest);
                    return;

                }

            }

        }
    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, KnownInstance report,
            boolean removeSender) {
        NodeSet antecedentNodeSet = new NodeSet();
        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        int currentAttitudeID = currentChannel.getAttitudeID();
        Substitutions reportSubs = report.getSubstitutions();
        NodeSet toBeSentTo = removeAlreadyOpenChannels(antecedentNodeSet, currentRequest, reportSubs, ruleType);
        sendRequestsToNodeSet(toBeSentTo, reportSubs, currentContextName,
                currentAttitudeID, ChannelType.AntRule, this);
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
        boolean forwardReportType = currentReport.getInferenceType() == InferenceTypes.FORWARD;
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

                        requestAntecedentsNotAlreadyWorkingOn(tempRequest, false);
                        //// Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                        //// tempChannel);
                        // handleResponseOfApplyRuleHandler(ruleResponse, currentReport, tempChannel);
                    } else {
                        NodeSet dominatingRules = new NodeSet();
                        // nodeset with all the dominating rules for which i am antecedent to
                        NodeSet toBeSentToDom = removeAlreadyOpenChannels(dominatingRules, tempRequest,
                                currentReportSubs, false);
                        sendRequestsToNodeSet(toBeSentToDom, currentReportSubs, currentReportContextName,
                                currentReportAttitudeID,
                                ChannelType.AntRule, this);
                        List<Match> matchesList = new ArrayList<Match>();
                        // Matcher.match(this, ruleNodeExtractedSubs);
                        List<Match> toBeSentToMatch = removeAlreadyOpenChannels(matchesList, tempRequest);
                        sendRequestsToMatches(toBeSentToMatch, currentReportSubs, null, currentReportContextName,
                                currentReportAttitudeID, ChannelType.MATCHED, this);
                    }
                } else {

                    /* always sue the extracted report subs in the requests */
                    Collection<KnownInstance> theKnownInstanceSet = this.getKnownInstances().mergeKInstancesBasedOnAtt(
                            tempChannel.getAttitudeID());
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean subSetCheck = Substitutions.isSubSet(currentKISubs,
                                tempRequest.getChannel().getFilterSubstitutions());

                        boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (subSetCheck && supportCheck) {
                            requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance, true);
                            // hena kan maktoob en mafrood haneb3at request lel antecedents bel union
                            // subs???

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
                    NodeSet dominatingRules = new NodeSet();
                    NodeSet toBeSentToDom = removeAlreadyOpenChannels(dominatingRules, tempRequest,
                            currentReportSubs, false);
                    sendRequestsToNodeSet(toBeSentToDom, currentReportSubs, currentReportContextName,
                            currentReportAttitudeID,
                            ChannelType.AntRule, this);
                    List<Match> matchingNodes = new ArrayList<>();
                    // Matcher.match(this, ruleNodeExtractedSubs);
                    List<Match> toBeSentToMatch = removeAlreadyOpenChannels(matchingNodes, tempRequest);
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

}
