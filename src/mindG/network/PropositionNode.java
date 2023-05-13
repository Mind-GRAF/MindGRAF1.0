package mindG.network;

import java.util.ArrayList;
import java.util.Collection;

import java.util.Iterator;
import java.util.List;

import mindG.mgip.KnownInstance;
import mindG.mgip.InferenceType;
import mindG.mgip.KnownInstanceSet;
import mindG.mgip.Report;
import mindG.mgip.ReportType;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Match;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.*;

public class PropositionNode extends Node {
    protected ChannelSet outgoingChannels;
    protected StringChannelSet incomingChannels;
    protected ChannelSet forwardChannels;
    protected KnownInstanceSet knownInstances;
    protected boolean forwardDone;

    public PropositionNode() {
        outgoingChannels = new ChannelSet();
        incomingChannels = new StringChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
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
    protected Request establishChannel(ChannelType type, Node targetNode,
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
                        matchType, requesterNode);
                break;
            case AntRule:
                newChannel = new AntecedentToRuleChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;
            default:
                newChannel = new RuleToConsequentChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;
        }
        Request newRequest = new Request(newChannel, targetNode);
        StringChannelSet incomingChannels = ((PropositionNode) requesterNode).getIncomingChannels();
        String currentChannelID = incomingChannels.getChannelID(newChannel);
        Channel currentChannel = outgoingChannels.getChannel(currentChannelID);
        if (currentChannelID == null) {
            ((PropositionNode) targetNode).addToOutgoingChannels(newChannel);
            addToIncomingChannels(newChannel);
            return newRequest;
        }

        return new Request(currentChannel, targetNode);

    }

    /***
     * Used to send a report over a request through calling request.testReportToSend
     * 
     * @param report
     * @param currentChannel
     * @return
     */
    public boolean sendReport(Report report, Channel currentChannel) {
        if (currentChannel.testReportToSend(report)) {
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
        for (Channel outChannel : outgoingChannels)
            sendReport(report, outChannel);

    }

    /***
     * Trying to send reports to all outgoing channels
     * 
     * @param report
     */
    // public void broadcastReports(ReportSet reports) {

    // }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the non matching ones of the NodeSet to further reports
     * with the given inputs
     * 
     * @param ns          NodeSet to be sent to
     * @param toBeSent    Substitutions to be passed
     * @param contextID   latest request context
     * @param requestType
     */
    protected void sendReportToNodeSet(NodeSet ns, Report toBeSent, ChannelType channelType) {
        for (Node sentTo : ns) {
            Substitutions reportSubs = toBeSent.getSubstitutions();
            Request newRequest = establishChannel(channelType, sentTo, null, reportSubs, toBeSent.getContextName(),
                    toBeSent.getAttitude(), -1, toBeSent.getRequesterNode());
            forwardChannels.addChannel(newRequest.getChannel());
            sendReport(toBeSent, newRequest.getChannel());
        }
    }

    // protected void sendReportsToNodeSet(NodeSet ns, ReportSet toBeSent, String
    // contextName,
    // String attitudeName,
    // ChannelType channelType) {

    // }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the List<Match> to further reports instances with the given
     * inputs
     * 
     * @param list
     * @param toBeSent
     */
    protected void sendReportToMatches(List<Match> list, Report toBeSent) {
        for (Match currentMatch : list) {
            Substitutions reportSubs = toBeSent.getSubstitutions();
            int matchType = currentMatch.getMatchType();
            Request newRequest = establishChannel(ChannelType.MATCHED, currentMatch.getNode(), null, reportSubs,
                    toBeSent.getContextName(), toBeSent.getAttitude(),
                    matchType, toBeSent.getRequesterNode());
            forwardChannels.addChannel(newRequest.getChannel());
            sendReport(toBeSent, newRequest.getChannel());
        }
    }

    // protected void sendReportsToMatches(List<Match> list, ReportSet reports,
    // String contextId, String attitudeId) {

    // }

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
     * supported in a specific attitude in a desired context or not.
     * 
     * 
     * @param desiredContextName
     * @param desiredAttitudeID
     * @return whether the PropositionNode is asserted in a desiredContext or not
     */

    public boolean asserted(String desiredContextName, int desiredAttitudeID) {
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

    protected void getNodesToSendReport(ChannelType channelType, String currentContextName, int currentAttitudeID,
            Substitutions substitutions, boolean reportSign, InferenceType inferenceType) {

        try {
            PropositionSet supportPropSet = new PropositionSet();
            supportPropSet.add(this);
            Substitutions subs = substitutions == null ? new Substitutions() : substitutions;
            Report toBeSent = new Report(subs, supportPropSet, currentAttitudeID, reportSign, inferenceType, null);
            toBeSent.setContextName(currentContextName);
            toBeSent.setReportType(channelType);
            switch (channelType) {
                case MATCHED:
                    List<Match> matchesReturned = new ArrayList<>();
                    // Matcher.match(this, substitutions);
                    if (matchesReturned != null)
                        sendReportToMatches(matchesReturned, toBeSent);
                    break;
                case AntRule:
                    if (this instanceof RuleNode) {
                        NodeSet antecedentsNodes = new NodeSet();
                        // ((RuleNode) this).getDownAntNodeSet();
                        sendReportToNodeSet(antecedentsNodes, toBeSent, channelType);
                        break;
                    }

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    protected void getNodesToSendRequest(ChannelType channelType, String currentContextName,
            int currentReportAttitudeID,
            Substitutions substitutions) {
        try {
            switch (channelType) {
                case MATCHED:
                    List<Match> matchesReturned = new ArrayList<>();
                    // Matcher.match(this, substitutions);
                    if (matchesReturned != null)
                        sendRequestsToMatches(matchesReturned, substitutions, null, currentContextName,
                                currentReportAttitudeID, channelType, this);
                    break;
                case RuleCons:
                    NodeSet dominatingRules = new NodeSet();
                    // getUpConsNodeSet();
                    Substitutions linearSubs = substitutions == null ? new Substitutions() : substitutions;
                    sendRequestsToNodeSet(dominatingRules, linearSubs, currentContextName, currentReportAttitudeID,
                            channelType, this);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    }

    private Report attemptAddingReportToKnownInstances(Report report) {
        Substitutions reportSubs = report.getSubstitutions();
        int reportAttitude = report.getAttitude();
        boolean signReport = false;
        KnownInstance compatibleReport = knownInstances.getKnownInstanceByAttSubNve(reportAttitude, reportSubs);
        if (compatibleReport == null) {
            compatibleReport = knownInstances.getKnownInstanceByAttSubPve(reportAttitude, reportSubs);
            signReport = true;
        }
        boolean channelCheck = report.getReportType() == ReportType.MATCHED
                || report.getReportType() == ReportType.RuleCons;
        if (compatibleReport == null) {
            if (channelCheck)
                knownInstances.addKnownInstance(report);
            return report;
        }

        return new Report(compatibleReport.getSubstitutions(), compatibleReport.getSupports(),
                compatibleReport.getAttitudeID(), signReport, null, null);
    }

    /***
     * this method is used to initiate the whole process of backward inference
     * 
     */
    public void deduce() {

        Scheduler.initiate();
        String currentContextName = Controller.getContextName();
        int currentattitudeID = Controller.getAttitudeID();

        getNodesToSendRequest(ChannelType.RuleCons, currentContextName, currentattitudeID, null);

        getNodesToSendRequest(ChannelType.MATCHED, currentContextName, currentattitudeID, null);
        // what to return here ?
        Scheduler.schedule();
    }

    /***
     * this method is used to initiate the whole process of forward inference
     * 
     */
    public void add() {

        Scheduler.initiate();
        String currentContextName = Controller.getContextName();
        int currentAttitudeID = Controller.getAttitudeID();
        boolean reportSign = false;
        // Controller.isNegated(this);

        getNodesToSendReport(ChannelType.AntRule, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);

        getNodesToSendReport(ChannelType.MATCHED, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);
        Scheduler.schedule();
    }

    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().peek();
        try {
            processSingleRequests(requestHasTurn);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /***
     * Request handling in Non-Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) {
        Channel currentChannel = currentRequest.getChannel();
        String currentContext = currentChannel.getContextName();
        int currentAttitude = currentChannel.getAttitudeID();
        Node requesterNode = currentChannel.getRequesterNode();
        Substitutions reportSubstitutions = new Substitutions();
        // meaning it is already closed no free variables so new substitutions will def
        // pass through the filter subs
        PropositionSet supportNodeSet = new PropositionSet();
        if (this.asserted(currentContext, currentAttitude)) {
            supportNodeSet.add((PropositionNode) this);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, true,
                    InferenceType.BACKWARD, requesterNode);
            NewReport.setContextName(currentContext);
            NewReport.setReportType(currentChannel.getChannelType());
            sendReport(NewReport, currentRequest.getChannel());

        } else if (this.assertedNegation(currentContext, currentAttitude))
        // law min we max betpoint le zero khodiha men hazem fa bacheck law el args
        // asserted if they are asserted fa bab3at el report be -ve
        {

            supportNodeSet.add((PropositionNode) this);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, false,
                    InferenceType.BACKWARD, requesterNode);
            NewReport.setContextName(currentContext);
            NewReport.setReportType(currentChannel.getChannelType());
            sendReport(NewReport, currentRequest.getChannel());
            ;
        } else {
            boolean sentSuccessfully = false;
            Collection<KnownInstance> theAlmostPveReportsSet = this.getKnownInstances()
                    .getPositiveCollectionbyAttribute(
                            currentChannel.getAttitudeID());
            Iterator ReportIteratorPve = theAlmostPveReportsSet.iterator();
            for (KnownInstance currentAlmostPveReport : theAlmostPveReportsSet) {

                Report currentPveReport = new Report(currentAlmostPveReport.getSubstitutions(),
                        currentAlmostPveReport.getSupports(), currentAlmostPveReport.getAttitudeID(),
                        true,
                        InferenceType.BACKWARD, requesterNode);
                currentPveReport.setContextName(currentContext);

                currentPveReport.setReportType(currentChannel.getChannelType());

                sentSuccessfully |= sendReport(currentPveReport, currentRequest.getChannel());

            }

            Collection<KnownInstance> theAlmostNveReportsSet = this.getKnownInstances()
                    .getNegativeCollectionbyAttribute(currentChannel.getAttitudeID());
            Iterator ReportIteratorNve = theAlmostNveReportsSet.iterator();
            for (KnownInstance currentAlmostNveReport : theAlmostNveReportsSet) {
                Report currentNveReport = new Report(currentAlmostNveReport.getSubstitutions(),
                        currentAlmostNveReport.getSupports(), currentAlmostNveReport.getAttitudeID(),
                        false,
                        InferenceType.BACKWARD, requesterNode);
                currentNveReport.setContextName(currentContext);

                currentNveReport.setReportType(currentChannel.getChannelType());

                sentSuccessfully |= sendReport(currentNveReport, currentRequest.getChannel());

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
                        ChannelType.RuleCons, this);
                if (!(currentChannel instanceof MatchChannel)) {
                    List<Match> matchesList = new ArrayList<Match>();
                    // liha 3elaka bel match class!!
                    // shoufi hatakhdi el nodes nafsaha men el awel wala eh?
                    List<Match> remainingMatches = removeAlreadyOpenChannels(matchesList,
                            currentRequest);
                    sendRequestsToMatches(remainingMatches, filterSubs, switchSubs, currentContext, currentAttitude,
                            ChannelType.MATCHED, this);

                }

            }
        }

    }

    private NodeSet removeAlreadyOpenChannels(NodeSet nodeSetRemoveFrom, Request currentRequest,
            Substitutions toBeCompared) {
        NodeSet remainingNodes = new NodeSet();
        for (Node currentNode : nodeSetRemoveFrom) {
            boolean notTheSame = currentNode.getId() != currentRequest.getChannel().getRequesterNode().getId();
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
        Report reportHasTurn = Scheduler.getHighQueue().poll();
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
    protected void processSingleReports(Report currentReport) {

        Report reportToBeBroadcasted = attemptAddingReportToKnownInstances(currentReport);
        if (reportToBeBroadcasted != null) {
            boolean forwardReportType = reportToBeBroadcasted.getInferenceType() == InferenceType.FORWARD;
            if (currentReport.getReportType() == ReportType.RuleCons) {
                PropositionNode supportNode = new PropositionNode();
                // hena el method el babuild biha node bl new subs
                // buildNodeSubstitutions(currentReport.getSubstitutions());
                if (supportNode != null) {
                    supportNode.addJustificationBasedSupport(reportToBeBroadcasted.getSupport());
                    // bazawed lel node justification support
                    PropositionSet reportSupportPropSet = new PropositionSet();
                    reportSupportPropSet.add(supportNode);
                    reportToBeBroadcasted.setSupport(reportSupportPropSet);
                }
            }
            // TODO: GRADED PROPOSITIONS HANDLING REPORTS
            if (forwardReportType && !forwardDone) {
                forwardDone = true;
                if (currentReport.getReportType() == ReportType.MATCHED) {
                    List<Match> matchesReturned = new ArrayList<Match>();
                    // list of matches with a node
                    // Matcher.match(this);
                    sendReportToMatches(matchesReturned, currentReport);
                }
                NodeSet dominatingRules = new NodeSet();
                // getUpAntNodeSet();
                sendReportToNodeSet(dominatingRules, currentReport,
                        ChannelType.AntRule);
            } else if (forwardReportType && forwardDone) {
                for (Channel channel : forwardChannels) {
                    sendReport(currentReport, channel);
                }
            } else
                broadcastReport(reportToBeBroadcasted);

        }
        if ((this instanceof RuleNode))
            Scheduler.addToHighQueue(currentReport);
        ;

    }

    private void addJustificationBasedSupport(PropositionSet support) {
    }

    public ChannelSet getOutgoingChannels() {
        return outgoingChannels;
    }

    public void setOutgoingChannels(ChannelSet outgoingChannels) {
        this.outgoingChannels = outgoingChannels;
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

    protected ArrayList<String> getIncomingAntecedentRuleChannels() {
        return incomingChannels.getAntRuleChannels();
    }

    protected ArrayList<String> getIncomingRuleConsequentChannels() {
        return incomingChannels.getRuleConsChannels();
    }

    protected ArrayList<String> getIncomingMatchChannels() {
        return incomingChannels.getMatchChannels();
    }

    /***
     * Requests received added to the low priority queue to be served accordingly
     * through the Scheduler.
     */
    public void receiveRequest(Request tempRequest) {
        Scheduler.addToLowQueue(tempRequest);
    }

    /***
     * Reports received added to the high priority queue to be served accordingly
     * through the Scheduler.
     */
    public void receiveReport(Report currentReport) {
        Scheduler.addToHighQueue(currentReport);
    }

    public ChannelSet getForwardChannels() {
        return forwardChannels;
    }

    public void setForwardChannels(ChannelSet forwardChannels) {
        this.forwardChannels = forwardChannels;
    }

    public void setKnownInstances(KnownInstanceSet knownInstances) {
        this.knownInstances = knownInstances;
    }

    public boolean isForwardDone() {
        return forwardDone;
    }

    public void setForwardDone(boolean forwardDone) {
        this.forwardDone = forwardDone;
    }

    public KnownInstanceSet getKnownInstances() {
        return knownInstances;
    }

    public StringChannelSet getIncomingChannels() {
        return incomingChannels;
    }

    public void setIncomingChannels(StringChannelSet incomingChannels) {
        this.incomingChannels = incomingChannels;
    }

}