package mindG.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import javafx.util.Pair;
import mindG.mgip.AlmostReports;
import mindG.mgip.InferenceTypes;
import mindG.mgip.KnownInstances;
import mindG.mgip.PropositionSet;
import mindG.mgip.Report;
import mindG.mgip.ReportSet;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Match;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.*;

public class PropositionNode extends Node {
    protected ChannelSet outgoingChannels;
    protected ChannelSet incomingChannels;
    protected KnownInstances knownInstances;

    public PropositionNode() {
        outgoingChannels = new ChannelSet();
        incomingChannels = new ChannelSet();
        knownInstances = new KnownInstances();
    }

    /***
     * Method handling all types of sending requests, taking different inputs to
     * handle different cases.
     * 
     * @param type           type of request being addressed
     * @param currentElement source Node being addressed
     * @param switchSubs     mapped substitutions from origin node
     * @param filterSubs     constraints substitutions for a specific request
     * @param contextName    context name used
     * @param attitudeId     attitude name used
     * @param matchType      int representing the match Type. -1 if not a matching
     *                       node scenario
     * @param reporterNode
     * @return the established type based request
     */
    protected Request establishChannel(ChannelType type, Node currentElement,
            Substitutions switchSubs,
            Substitutions filterSubs, String contextName,
            int attitudeId,
            int matchType, Node requesterNode) {

        Substitutions switchSubstitutions = switchSubs == null ? new Substitutions()
                : switchSubs;
        Substitutions filterSubstitutions = filterSubs == null ? new Substitutions()
                : filterSubs;
        Channel newChannel;
        switch (type) {
            case MATCHED:
                newChannel = new MatchChannel(switchSubstitutions, filterSubstitutions,
                        contextName, attitudeId,
                        true, matchType, requesterNode);
                break;
            case AntRule:
                newChannel = new AntecedentToRuleChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        true, requesterNode);
                break;
            default:
                newChannel = new RuleToConsequentChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        true, requesterNode);
                break;
        }
        // Pair<Channel, NodeSet> newChannelPair = new Pair<>(newChannel, new
        // NodeSet());
        // newChannelPair.getValue().add(this);
        Request newRequest = new Request(newChannel, currentElement);
        ChannelSet incomingChannels = ((PropositionNode) requesterNode).getIncomingChannels();
        Channel currentChannel = incomingChannels.getChannel(newChannel);
        if (currentChannel == null) {
            ((PropositionNode) currentElement).addToOutgoingChannels(newChannel);
            addToIncomingChannels(newChannel);
            return newRequest;
        }

        return new Request(currentChannel, currentElement);
        // keeping in mind en manshelsh el request lama nigui neprocess it

    }

    /***
     * Used to send a report over a request through calling request.testReportToSend
     * 
     * @param report
     * @param currentRequest
     * @return
     */
    public boolean sendReport(Report report, Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        if (currentChannel.testReportToSend(report, currentRequest.getChannel().getRequesterNode())) {
            return true;

        } else {
            return false;

        }

    }

    /***
     * Trying to send a report to all outgoing channels
     * 
     * @param report
     */
    public void broadcastReport(Report report) {

    }

    /***
     * Trying to send reports to all outgoing channels
     * 
     * @param report
     */
    public void broadcastReports(ReportSet reports) {

    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the non matching ones of the NodeSet to further reports
     * with the given inputs
     * 
     * @param ns          NodeSet to be sent to
     * @param toBeSent    Substitutions to be passed
     * @param contextID   latest request context
     * @param attitudeID
     * @param requestType
     */
    protected void sendReportToNodeSet(NodeSet ns, Report toBeSent, String contextName,
            int attitudeID,
            ChannelType channelType) {

    }

    protected void sendReportsToNodeSet(NodeSet ns, ReportSet toBeSent, String contextName,
            String attitudeName,
            ChannelType channelType) {

    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the List<Match> to further reports instances with the given
     * inputs
     * 
     * @param list
     * @param toBeSent
     * @param contextId
     * @param attitudeID
     */
    protected void sendReportToMatches(List<Match> list, Report toBeSent, String contextId, int attitudeID) {

    }

    protected void sendReportsToMatches(List<Match> list, ReportSet reports, String contextId, String attitudeId) {

    }

    /***
     * Helper method responsible for sending reports over each request
     * 
     * @param filteredNodeSet
     * @param toBeSent
     */
    protected void sendReportToRequestSet(ChannelSet filteredNodeSet, Report toBeSent) {

    }
    // hena mesh 3arfa channelSet wala ChannelPairSetSet

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the List<Match> to further request instances with the given
     * inputs
     * 
     * @param list
     * @param filterSubs
     * @param switchSubs
     * @param contextId
     * @param attitudeId
     * @param channelType
     * @param reporterNode
     * 
     */
    protected void sendRequestsToMatches(List<Match> list, Substitutions filterSubs, Substitutions switchSubs,
            String contextId, int attitudeId, ChannelType channelType, Node requesterNode) {
        for (Match currentMatch : list) {
            int matchType = currentMatch.getMatchType();
            PropositionNode matchedNode = (PropositionNode) currentMatch.getNode();
            Request newRequest = establishChannel(channelType, matchedNode,
                    switchSubs, filterSubs, contextId,
                    attitudeId, matchType, requesterNode);
            Scheduler.addToLowQueue(newRequest);
        }
    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the NodeSet to further request instances with the given inputs
     * 
     * @param ns           NodeSet to be sent to
     * @param filterSubs   Substitutions to be passed
     * @param contextID    latest request context
     * @param attitudeId
     * @param channelType
     *                     //
     * @param reporterNode
     */
    protected void sendRequestsToNodeSet(NodeSet ns, Substitutions filterSubs,
            String contextID, int attitudeId,
            ChannelType channelType, Node requesterNode) {

        for (Node sentTo : ns) {
            Request newRequest = establishChannel(channelType, sentTo, null, filterSubs,
                    contextID, attitudeId, -1, requesterNode);
            Scheduler.addToLowQueue(newRequest);
        }
    }

    /***
     * The method calls a method previously implemented in SNeBR in Context class.
     * desiredContext.isAsserted(this) is called to check whether a PropositionNode
     * is
     * asserted in the given context or not.
     * 
     * 
     * @param desiredContext
     * @return whether the PropositionNode is asserted in a desiredContext or not
     */
    public boolean assertedInContext(Context desiredContext) {
        return false;

    }

    public boolean asserted(String desiredContextName, int desiredAttitudeID) {
        return false;

    }

    /***
     * The method is identical to the previous one, it is only implemented to
     * provide easier
     * access to the method. With the desiredContextName we just call the Controller
     * existing in SNeBR to get the relevant context according to desiredContextName
     * through calling Controller.getContextByName(desiredContextName) and store it
     * in
     * desiredContext. Then, we call desiredContext.isAsserted(this).
     */

    public boolean assertedInContext(String desiredContextName) {
        return false;

    }

    /***
     * Method handling all types of Nodes retrieval and sending
     * reports to each Node Type
     * 
     * @param type               type of request being addressed
     * @param currentContextName context name used
     * @param substitutions      channel substitutions applied over the channel
     * @param reportSign
     * @param inferenceType
     */

    protected void getNodesToSendReport(ChannelType channelType, String currentContextName, String currentAttitudeName,
            Substitutions substitutions, boolean reportSign, InferenceTypes inferenceType) {

    }

    /***
     * Method handling all types of Nodes retrieval and sending different
     * type-based
     * requests to each Node Type
     *
     * @param type               type of request being addressed
     * @param currentContextName context name used
     * @param substitutions      request substitutions applied over the request
     */
    protected void getNodesToSendRequest(ChannelType channelType, String currentContextName, String currentAttitudeName,
            Substitutions substitutions) {

    }

    /***
     * Method comparing opened outgoing channels over each match's node of the
     * matches whether a more generic request of the specified channel was
     * previously sent in order not to re-send redundant requests -- ruleType gets
     * applied on Andor or Thresh part.
     * 
     * @param matchingNodes
     * @param currentChannel
     * @param ruleType
     * @return
     */

    /***
     * Checking if this node instance contains not yet bound free variables
     * This method is implemented to check if this node represents a ”who”, ”where”,
     * ”which” question, asking for bindings of a specific a specific set of
     * variables, in other
     * words, to state whether this PropositionNode has atleast one free variable
     * which
     * is not bound or not.
     * 
     * @param filterSubs reference substitutions
     * @return boolean computed from VariableNodeStats.areAllVariablesBound()
     */
    public boolean isWhQuestion(Substitutions subs) {
        if (super.isVariableNode(this)) {
            boolean isBound = false;
            VariableSet freeVariableSet = super.getFreeVariables(this);
            for (Node freeVariable : freeVariableSet) {
                isBound = subs.isFreeVariableBound(freeVariable);
                if (!isBound)
                    return true;

            }

        }
        return false;
        // tab law heya aslun mesh variable Node eh el result?

    }

    private Report attemptAddingReportToKnownInstances(Channel request, Report report) {
        return report;

    }
    // used in backward Inference

    public void deduce() {

    }

    // used in forward Inference

    public void add() {

    }

    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().peek();
        try {
            processSingleRequests(requestHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    // dih kanet betefdal teiterate fel outgoing requests beta3et el node
    /***
     * Request handling in Non-Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        String currentContext = currentChannel.getContextName();
        int currentAttitude = currentChannel.getAttitudeID();
        Node reporterNode = currentRequest.getReporterNode();
        Node requesterNode = currentChannel.getRequesterNode();
        // Substitutions reportSubstitutions = currentChannel.getFilterSubstitutions();
        Substitutions reportSubstitutions = new Substitutions();
        PropositionSet supportNodeSet = new PropositionSet();
        if (this.asserted(currentContext, currentAttitude)) {
            supportNodeSet.add((PropositionNode) reporterNode);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, true,
                    InferenceTypes.BACKWARD, requesterNode);
            sendReport(NewReport, currentRequest);

        } else if (this.assertedNegation(currentContext, currentAttitude))
        // law min we max betpoint le zero khodiha men hazem fa bacheck law el args
        // asserted if they are asserted fa bab3at el report be -ve
        {

            supportNodeSet.add((PropositionNode) reporterNode);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, false,
                    InferenceTypes.BACKWARD, requesterNode);
            sendReport(NewReport, currentRequest);
        } else {
            boolean sentSuccessfully = false;
            Collection<AlmostReports> theAlmostPveReportsSet = this.getKnownInstances()
                    .getPositiveCollectionbyAttribute(
                            currentChannel.getAttitudeID());
            Iterator ReportIteratorPve = theAlmostPveReportsSet.iterator();
            for (AlmostReports currentAlmostPveReport : theAlmostPveReportsSet) {

                Report currentPveReport = new Report(currentAlmostPveReport.getSubstitutions(),
                        currentAlmostPveReport.getSupports(), currentAlmostPveReport.getAttitudeID(), true,
                        InferenceTypes.BACKWARD, requesterNode);
                sentSuccessfully |= sendReport(currentPveReport, currentRequest);

            }
            Collection<AlmostReports> theAlmostNveReportsSet = this.getKnownInstances()
                    .getNegativeCollectionbyAttribute(currentChannel.getAttitudeID());
            Iterator ReportIteratorNve = theAlmostNveReportsSet.iterator();
            for (AlmostReports currentAlmostNveReport : theAlmostNveReportsSet) {
                Report currentNveReport = new Report(currentAlmostNveReport.getSubstitutions(),
                        currentAlmostNveReport.getSupports(), currentAlmostNveReport.getAttitudeID(), false,
                        InferenceTypes.BACKWARD, requesterNode);
                sentSuccessfully |= sendReport(currentNveReport, currentRequest);

            }

            Substitutions filterSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
            if (!sentSuccessfully || isWhQuestion(filterSubs)) {

                NodeSet dominatingRules = new NodeSet();
                // node set containg all the rule nodes that i am consequent to
                NodeSet remainingNodes = removeAlreadyOpenChannels(dominatingRules,
                        currentRequest, filterSubs);
                sendRequestsToNodeSet(remainingNodes, filterSubs, currentContext,
                        currentAttitude,
                        ChannelType.RuleCons, reporterNode);
                if (!(currentChannel instanceof MatchChannel)) {
                    List<Match> matchesList = new ArrayList<Match>();
                    // liha 3elaka bel match class!!
                    // shoufi hatakhdi el nodes nafsaha men el awel wala eh?
                    List<Match> remainingMatches = removeAlreadyOpenChannels(matchesList,
                            currentRequest);
                    sendRequestsToMatches(remainingMatches, filterSubs, switchSubs, currentContext, currentAttitude,
                            ChannelType.MATCHED, reporterNode);

                }

            }
        }

    }

    private NodeSet removeAlreadyOpenChannels(NodeSet nodeSetRemoveFrom, Request currentRequest,
            Substitutions toBeCompared) {
        NodeSet remainingNodes = new NodeSet();
        for (Node currentNode : nodeSetRemoveFrom) {
            boolean notTheSame = currentNode.getId() != currentRequest.getReporterNode().getId();
            // dih fe case the andor wel thresh node
            if (notTheSame) {
                ChannelSet outgoingChannels = ((PropositionNode) currentNode).getOutgoingChannels();
                for (Channel outgoingChannel : outgoingChannels) {
                    Substitutions processedRequestChannelFilterSubs = outgoingChannel.getFilterSubstitutions();
                    notTheSame &= !Substitutions.isSubSet(processedRequestChannelFilterSubs,
                            toBeCompared)
                            && outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode().getId();
                }
                if (notTheSame) {
                    remainingNodes.add(currentNode);

                }
            }

        }

        return remainingNodes;
    }

    private NodeSet removeAlreadyOpenChannels2(NodeSet nodeSetRemoveFrom, Request currentRequest,
            Substitutions toBeCompared) {
        NodeSet remainingNodes = new NodeSet();
        for (Node currentNode : nodeSetRemoveFrom) {
            boolean notTheSame = currentNode.getId() != currentRequest.getReporterNode().getId();
            // dih fe case the andor wel thresh node
            if (notTheSame) {
                ChannelSet incomingChannels = ((PropositionNode) currentRequest.getReporterNode())
                        .getIncomingChannels();
                for (Channel incomingChannel : incomingChannels) {
                    // Channel channelOfThePair = outgoingChannel.getKey();
                    Substitutions processedRequestChannelFilterSubs = incomingChannel.getFilterSubstitutions();
                    // Request channelsRequest = Scheduler.getRequestByChannel(channelOfThePair);
                    notTheSame &= !Substitutions.isSubSet(processedRequestChannelFilterSubs,
                            toBeCompared)
                            && incomingChannel.getRequesterNode().getId() == currentRequest.getReporterNode().getId();
                }
                if (notTheSame) {
                    remainingNodes.add(currentNode);

                }
            }

        }

        return remainingNodes;
    }

    /***
     * This method is specifically implemented to filter matchingNodes with a
     * certain
     * criteria. The filter is perfomed through iterating over each Match extracting
     * the node
     * either to keep it or remove it according to the filtering criteria we are
     * applying over matchingNodes.
     * 
     * @param matchingNodes
     * @param currentRequest
     * @return
     */
    protected List<Match> removeAlreadyOpenChannels(List<Match> matchingNodes,
            Request currentRequest) {
        List<Match> nodesToConsider = new ArrayList<Match>();
        for (Match sourceMatch : matchingNodes) {
            Node sourceNode = sourceMatch.getNode();
            boolean conditionMet = false;
            Substitutions currentRequestFilterSubs = currentRequest.getChannel().getFilterSubstitutions();
            ChannelSet outgoingChannels = ((PropositionNode) sourceNode).getOutgoingChannels();
            for (Channel outgoingChannel : outgoingChannels) {
                Substitutions processedRequestChannelFilterSubs = outgoingChannel.getFilterSubstitutions();
                // Channel currentChannel = outgoingChannel.getKey();
                // Request channelsRequest = Scheduler.getRequestByChannel(currentChannel);
                // el hetta dih lazem liha tafkir 3ashan delwa2ty mmkn kaza request yet7at 3ala
                // el queue benafs el channel bas ana mehtaga request mo3ayan
                conditionMet &= !Substitutions.isSubSet(processedRequestChannelFilterSubs,
                        currentRequestFilterSubs) &&
                        outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode().getId();
            }
            if (conditionMet)
                nodesToConsider.add(sourceMatch);

        }
        return nodesToConsider;

    }

    private boolean assertedNegation(String currentContext, int currentAttitude) {
        return false;
    }

    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().peek();
        // Report reportHasTurn = Scheduler.getHighQueue().peek();
        try {
            processSingleReports(reportHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    /***
     * Report handling in Non-Rule proposition nodes.
     * 
     * @param requestHasTurn
     */
    protected void processSingleReports(Report reportHasTurn) {
        Channel currentChannel = currentRequest.getChannel();
        String contextName = currentChannel.getContextName();
        int attitudeID = currentChannel.getAttitudeID();
        ReportSet reports = currentChannel.getReportsBuffer();
        for (Report currentReport : reports) {
            Report reportToBeBroadcasted = attemptAddingReportToKnownInstances(currentChannel, currentReport);
            if (reportToBeBroadcasted != null) {
                boolean forwardReportType = reportToBeBroadcasted.getInferenceType() == InferenceTypes.FORWARD;
                if (currentChannel instanceof RuleToConsequentChannel) {
                    PropositionNode supportNode = new PropositionNode();
                    // hena el method el babuild biha node bl new subs
                    // buildNodeSubstitutions(currentReport.getSubstitutions());
                    if (supportNode != null) {
                        // supportNode.addJustificationBasedSupport(reportToBeBroadcasted.getSupport());
                        // bazawed lel node justification support
                        PropositionSet reportSupportPropSet = new PropositionSet();
                        reportSupportPropSet.add(supportNode);
                        reportToBeBroadcasted.setSupport(reportSupportPropSet);
                    }
                }
                // TODO: GRADED PROPOSITIONS HANDLING REPORTS
                if (forwardReportType) {
                    if (currentChannel instanceof MatchChannel) {
                        List<Match> matchesReturned = new ArrayList<Match>();
                        // list of matches with a node
                        // Matcher.match(this);
                        sendReportToMatches(matchesReturned, currentReport, contextName, attitudeID);
                    }
                    NodeSet dominatingRules = new NodeSet();
                    // getUpAntNodeSet();
                    sendReportToNodeSet(dominatingRules, currentReport, contextName, attitudeID,
                            ChannelType.AntRule);
                } else
                    broadcastReport(reportToBeBroadcasted);

            }
        }

    }

    public ChannelSet getOutgoingChannels() {
        return outgoingChannels;
    }

    public void setOutgoingChannels(ChannelSet outgoingChannels) {
        this.outgoingChannels = outgoingChannels;
    }

    public ChannelSet getIncomingChannels() {
        return incomingChannels;
    }

    public void setIncomingChannels(ChannelSet incomingChannels) {
        this.incomingChannels = incomingChannels;
    }

    public KnownInstances getKnownInstances() {
        return knownInstances;
    }

    public void setKnownInstances(KnownInstances knownInstances) {
        this.knownInstances = knownInstances;
    }

    protected Collection<Channel> getOutgoingAntecedentRuleChannels() {
        return outgoingChannels.getAntRuleChannels();
    }

    protected Collection<Channel> getOutgoingRuleConsequentChannels() {
        return outgoingChannels.getRuleConsChannels();
    }

    protected Collection<Channel> getOutgoingMatchChannels() {
        return outgoingChannels.getMatchChannels();
    }

    public void addToOutgoingChannels(Channel channel) {
        outgoingChannels.addChannel(channel);
    }

    public void addToIncomingChannels(Channel channel) {
        incomingChannels.addChannel(channel);
    }

    protected Collection<Channel> getIncomingAntecedentRuleChannels() {
        return incomingChannels.getAntRuleChannels();
    }

    protected Collection<Channel> getIncomingRuleConsequentChannels() {
        return incomingChannels.getRuleConsChannels();
    }

    protected Collection<Channel> getIncomingMatchChannels() {
        return incomingChannels.getMatchChannels();
    }

}