package mindG.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
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
import mindG.mgip.rules.AndOr;
import mindG.mgip.rules.Thresh;
import mindG.network.cables.DownCable;
import mindG.network.cables.DownCableSet;
import mindG.network.cables.UpCable;

public class PropositionNode extends Node {
    protected ChannelSet outgoingChannels;
    protected StringChannelSet incomingChannels;
    protected ChannelSet forwardChannels;
    protected KnownInstanceSet knownInstances;
    protected boolean forwardDone;

    public PropositionNode(String name, Boolean isVariable) {
        super(name, isVariable);

        outgoingChannels = new ChannelSet();
        incomingChannels = new StringChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
    }

    public PropositionNode(DownCableSet downCableSet) {
        super(downCableSet);

        outgoingChannels = new ChannelSet();
        incomingChannels = new StringChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
    }

    /***
     * Method getting the NodeSet that this current node is considered a consequent
     * to
     * 
     * @return
     */
    public NodeSet getUpConsDomRuleNodeSet() {
        NodeSet ret = new NodeSet();
        UpCable consequentCable = this.getUpCableSet().get("consequent");
        UpCable argsCable = this.getUpCableSet().get("args");
        if (argsCable != null) {
            ret.addAllTo(argsCable.getNodeSet());
        }
        if (consequentCable != null) {
            ret.addAllTo(consequentCable.getNodeSet());
        }
        return ret;
    }

    /***
     * Method getting the NodeSet that this current node is considered an antecedent
     * to
     * 
     * @return
     */
    public NodeSet getUpAntDomRuleNodeSet() {
        NodeSet ret = new NodeSet();
        UpCable argsCable = this.getUpCableSet().get("args");
        UpCable antCable = this.getUpCableSet().get("antecedent");
        if (argsCable != null) {
            ret.addAllTo(argsCable.getNodeSet());
        }
        if (antCable != null) {
            ret.addAllTo(antCable.getNodeSet());
        }

        return ret;
    }

    /***
     * Method getting the NodeSet of the antecedents for this current node
     * 
     * @return
     */

    public NodeSet getDownAntArgNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable argsCable = this.getDownCableSet().get("args");
        DownCable antCable = this.getDownCableSet().get("antecedent");
        if (argsCable != null) {
            ret.addAllTo(argsCable.getNodeSet());
        }
        if (antCable != null) {
            ret.addAllTo(antCable.getNodeSet());
        }

        return ret;
    }

    public Substitutions onlyRelevantSubs(Substitutions filterSubs) {
        System.out.println("i entered");
        NodeSet freeVariablesSet = this.getFreeVariables();
        System.out.println(freeVariablesSet.toString());
        Substitutions relevantSubs = new Substitutions();
        for (Node variableNode : freeVariablesSet.getValues()) {
            for (Node var : filterSubs.getMap().keySet()) {
                System.out.println(filterSubs.getMap().keySet());
                System.out.println("enterrrr");
                Node value = filterSubs.getMap().get(var);
                if (var.getName().equals(variableNode.getName())) {
                    System.out.println("1");
                    relevantSubs.add(var, value);
                }
            }
        }
        if (relevantSubs.size() == 0) {
            System.out.println("There are no Relevant Substitutions");
        }
        return relevantSubs;
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
        /* BEGIN - Helpful Prints */
        String reporterIdent = targetNode.getName();
        String requesterIdent = requesterNode.getName();
        System.out.println("Trying to establish a channel from " + requesterIdent + " to " + reporterIdent);
        /* END - Helpful Prints */
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
        StringChannelSet incomingChannels = ((PropositionNode) requesterNode).getIncomingChannels();
        String currentChannelID = incomingChannels.getChannelID(newChannel);
        Channel currentChannel = ((PropositionNode) targetNode).getOutgoingChannels().getChannel(currentChannelID);
        if (currentChannelID == null) {
            /* BEGIN - Helpful Prints */
            System.out.println("Channel of type " + newChannel.getChannelType()
                    + " is successfully created and used for further operations");
            /* END - Helpful Prints */
            Request newRequest = new Request(newChannel, targetNode);
            ((PropositionNode) targetNode).addToOutgoingChannels(newChannel);
            printChannelSet(((PropositionNode) targetNode).getOutgoingChannels());
            // .printChannels(((PropositionNode)
            // targetNode).getOutgoingChannels().getChannels());
            addToIncomingChannels(newChannel);
            printStringChannelSet(getIncomingChannels());
            // this.getIncomingChannels().printArrayList(this.getIncomingChannels().getChannelsIDS());
            return newRequest;
        } else if (currentChannelID != null && currentChannel == null) {
            /* BEGIN - Helpful Prints */
            System.out.println("Channel of type " + newChannel.getChannelType()
                    + " is successfully created and used for further operations");
            /* END - Helpful Prints */
            Request newRequest = new Request(newChannel, targetNode);
            ((PropositionNode) targetNode).addToOutgoingChannels(newChannel);
            printChannelSet(((PropositionNode) targetNode).getOutgoingChannels());
            // .printChannels(((PropositionNode)
            // targetNode).getOutgoingChannels().getChannels());
            printStringChannelSet(getIncomingChannels());
            // this.getIncomingChannels().printArrayList(this.getIncomingChannels().getChannelsIDS());
            return newRequest;
        }

        /* BEGIN - Helpful Prints */
        System.out.println(
                "Channel of type " + currentChannel.getChannelType() + " with ID: "
                        + currentChannel.stringifyChannelID()
                        + " is already established and retrieved for further operations");
        /* END - Helpful Prints */
        return new Request(currentChannel, targetNode);

    }

    public void printStringChannelSet(StringChannelSet stringChannelSet) {
        for (ChannelType channelType : StringChannelSet.channelsIDs.keySet()) {
            System.out.println("Channel Type: " + channelType);

            ArrayList<String> channelIDs = StringChannelSet.channelsIDs.get(channelType);
            for (String channelID : channelIDs) {
                System.out.println("Channel ID: " + channelID);
            }

            System.out.println(); // Add a blank line between channel types
        }
    }

    public void printChannelSet(ChannelSet channelSet) {
        for (ChannelType channelType : channelSet.channels.keySet()) {
            System.out.println("Channel Type: " + channelType);

            Hashtable<String, Channel> channelHashtable = channelSet.channels.get(channelType);
            for (String channelId : channelHashtable.keySet()) {
                Channel channel = channelHashtable.get(channelId);
                System.out.println("Channel ID: " + channelId);
                // Print other channel properties as needed
            }

            System.out.println(); // Add a blank line between channel types
        }
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
            System.out.println("the report " + report.stringifyReport() + " was succefully sent over channel "
                    + currentChannel.getChannelType() + " " + currentChannel.stringifyChannelID());
            return true;

        } else {
            System.out.println("the report " + report.stringifyReport() + " has failed to be sent over channel "
                    + currentChannel.getChannelType() + " " + currentChannel.stringifyChannelID());
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
     * Helper method responsible for establishing channels between this current node
     * and each of the non matching ones of the NodeSet to further reports
     * with the given inputs
     * 
     * @param ns          NodeSet to be sent to
     * @param toBeSent    Substitutions to be passed
     * @param channelType
     */
    protected void sendReportToNodeSet(NodeSet ns, Report toBeSent) {
        for (Node sentTo : ns) {
            Substitutions reportSubs = toBeSent.getSubstitutions();
            Request newRequest = establishChannel(ChannelType.MATCHED, sentTo, null, reportSubs,
                    toBeSent.getContextName(), toBeSent.getAttitude(),
                    -1, toBeSent.getRequesterNode());
            if (toBeSent.getInferenceType() == InferenceType.FORWARD) {
                forwardChannels.addChannel(newRequest.getChannel());

            }
            sendReport(toBeSent, newRequest.getChannel());
        }
    }

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
            if (toBeSent.getInferenceType() == InferenceType.FORWARD) {
                forwardChannels.addChannel(newRequest.getChannel());
            }
            sendReport(toBeSent, newRequest.getChannel());
        }
    }

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
     * @param switchSubs
     * @param contextID    latest request context
     * @param attitudeId
     * @param channelType
     * @param reporterNode
     */
    protected void sendRequestsToNodeSet(NodeSet ns, Substitutions filterSubs,
            Substitutions switchSubs, String contextName, int attitudeId,
            ChannelType channelType, Node requesterNode) {

        for (Node sentTo : ns) {
            Request newRequest = establishChannel(channelType, sentTo, switchSubs, filterSubs,
                    contextName, attitudeId, -1, requesterNode);
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
            Substitutions subs2 = new Substitutions();
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
                    NodeSet rulesNodes = getUpAntDomRuleNodeSet();
                    sendReportToNodeSet(rulesNodes, toBeSent);
                    if (this instanceof RuleNode) {
                        NodeSet argAntNodes = getDownAntArgNodeSet();
                        sendRequestsToNodeSet(argAntNodes, subs, subs2, currentContextName, currentAttitudeID,
                                channelType, this);

                    }
                    break;

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
            int currentAttitudeID,
            Substitutions substitutions) {
        try {
            switch (channelType) {
                case MATCHED:
                    List<Match> matchesReturned = new ArrayList<>();
                    // Matcher.match(this, substitutions);
                    if (matchesReturned != null)
                        sendRequestsToMatches(matchesReturned, substitutions, null, currentContextName,
                                currentAttitudeID, channelType, this);
                    break;
                case RuleCons:
                    NodeSet dominatingRules = getUpConsDomRuleNodeSet();
                    Substitutions filtersubs = substitutions == null ? new Substitutions() : substitutions;
                    Substitutions switchSubs = new Substitutions();
                    sendRequestsToNodeSet(dominatingRules, filtersubs, switchSubs, currentContextName,
                            currentAttitudeID,
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
     * @param subs reference substitutions
     * @return boolean
     */
    public boolean isOpenNodeNotBound(Substitutions subs) {
        if (this.isOpen()) {
            boolean isBound = false;
            NodeSet freeVariableSet = this.getFreeVariables();
            for (Node freeVariable : freeVariableSet.getValues()) {
                isBound = subs.isFreeVariableBound(freeVariable);
                System.out.println("isBound" + isBound);
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
        }

        return report;
    }

    /***
     * this method is used to initiate the whole process of backward inference
     * 
     */
    public void deduce() {

        Scheduler.initiate();
        String currentContextName = Controller.getContextName();
        int currentattitudeID = 0;
        // given by the user

        getNodesToSendRequest(ChannelType.RuleCons, currentContextName, currentattitudeID, null);

        getNodesToSendRequest(ChannelType.MATCHED, currentContextName, currentattitudeID, null);
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
        boolean reportSign = true;
        // Controller.isNegated(this);

        getNodesToSendReport(ChannelType.AntRule, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);

        getNodesToSendReport(ChannelType.MATCHED, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);
        Scheduler.schedule();
    }

    public void processRequests() {
        Request requestHasTurn = Scheduler.getLowQueue().poll();
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
        PropositionSet supportNodeSet = new PropositionSet();
        if (this.asserted(currentContext, currentAttitude)) {
            supportNodeSet.add((PropositionNode) this);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, true,
                    InferenceType.BACKWARD, requesterNode);
            NewReport.setContextName(currentContext);
            NewReport.setReportType(currentChannel.getChannelType());
            sendReport(NewReport, currentRequest.getChannel());

        } else {
            boolean sentSuccessfully = false;
            Collection<KnownInstance> thePveKnownInstancesSet = knownInstances
                    .getPositiveCollectionbyAttribute(
                            currentChannel.getAttitudeID());
            for (KnownInstance currentPveKnownInstance : thePveKnownInstancesSet) {

                Report currentPveReport = new Report(currentPveKnownInstance.getSubstitutions(),
                        currentPveKnownInstance.getSupports(), currentPveKnownInstance.getAttitudeID(),
                        true,
                        InferenceType.BACKWARD, requesterNode);
                currentPveReport.setContextName(currentContext);

                currentPveReport.setReportType(currentChannel.getChannelType());

                sentSuccessfully |= sendReport(currentPveReport, currentRequest.getChannel());

            }
            Collection<KnownInstance> theNveKnownInstancesSet = knownInstances
                    .getNegativeCollectionbyAttribute(currentChannel.getAttitudeID());
            for (KnownInstance currentNveKnownInstance : theNveKnownInstancesSet) {
                Report currentNveReport = new Report(currentNveKnownInstance.getSubstitutions(),
                        currentNveKnownInstance.getSupports(), currentNveKnownInstance.getAttitudeID(),
                        false,
                        InferenceType.BACKWARD, requesterNode);
                currentNveReport.setContextName(currentContext);

                currentNveReport.setReportType(currentChannel.getChannelType());

                sentSuccessfully |= sendReport(currentNveReport, currentRequest.getChannel());

            }

            Substitutions filterSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
            if (!sentSuccessfully || isOpenNodeNotBound(filterSubs)) {

                NodeSet dominatingRules = getUpConsDomRuleNodeSet();
                NodeSet remainingNodes = removeAlreadyEstablishedChannels(dominatingRules,
                        currentRequest, filterSubs);
                sendRequestsToNodeSet(remainingNodes, filterSubs, switchSubs, currentContext,
                        currentAttitude,
                        ChannelType.RuleCons, this);
                if (!(currentChannel instanceof MatchChannel)) {
                    List<Match> matchesList = new ArrayList<Match>();
                    // liha 3elaka bel match class!!
                    List<Match> remainingMatches = removeAlreadyEstablishedChannels(matchesList,
                            currentRequest, filterSubs);
                    sendRequestsToMatches(remainingMatches, filterSubs, switchSubs, currentContext, currentAttitude,
                            ChannelType.MATCHED, this);

                }

            }
        }

    }

    private NodeSet removeAlreadyEstablishedChannels(NodeSet nodeSetRemoveFrom, Request currentRequest,
            Substitutions toBeCompared) {
        /* BEGIN - Helpful Prints */
        String nodesToBeFiltered = "[ ";
        String nodesToBeKept = "[ ";
        /* END - Helpful Prints */
        NodeSet remainingNodes = new NodeSet();
        for (Node currentNode : nodeSetRemoveFrom) {
            if (currentNode instanceof PropositionNode) {
                boolean notTheSame = currentNode.getId() != currentRequest.getChannel().getRequesterNode().getId();
                // dih fe case the andor wel thresh node
                if (notTheSame) {
                    ChannelSet outgoingChannels = ((PropositionNode) currentNode).getOutgoingChannels();
                    for (Channel outgoingChannel : outgoingChannels) {
                        Substitutions processedRequestChannelFilterSubs = outgoingChannel.getFilterSubstitutions();
                        notTheSame &= !processedRequestChannelFilterSubs.isSubsetOf(toBeCompared)
                                && outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode()
                                        .getId();
                    }
                    if (notTheSame) {
                        remainingNodes.add(currentNode);
                    }
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
    protected List<Match> removeAlreadyEstablishedChannels(List<Match> matchingNodes,
            Request currentRequest, Substitutions toBeCompared) {
        List<Match> nodesToConsider = new ArrayList<Match>();
        for (Match sourceMatch : matchingNodes) {
            Node sourceNode = sourceMatch.getNode();
            boolean conditionMet = false;
            ChannelSet outgoingChannels = ((PropositionNode) sourceNode).getOutgoingChannels();
            for (Channel outgoingChannel : outgoingChannels) {
                Substitutions processedRequestChannelFilterSubs = outgoingChannel.getFilterSubstitutions();
                conditionMet &= !processedRequestChannelFilterSubs.isSubsetOf(toBeCompared) &&
                        outgoingChannel.getRequesterNode().getId() == currentRequest.getReporterNode().getId();
            }
            if (conditionMet)
                nodesToConsider.add(sourceMatch);

        }
        return nodesToConsider;

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
     * @param currentReport
     */
    protected void processSingleReports(Report currentReport) {

        Report reportToBeBroadcasted = attemptAddingReportToKnownInstances(currentReport);
        if (reportToBeBroadcasted != null) {
            boolean forwardReportType = reportToBeBroadcasted.getInferenceType() == InferenceType.FORWARD;
            if (currentReport.getReportType() == ReportType.RuleCons) {
                PropositionNode supportNode = new PropositionNode("M", forwardReportType);
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
                NodeSet dominatingRules = getUpAntDomRuleNodeSet();
                sendReportToNodeSet(dominatingRules, currentReport);
            } else if (forwardReportType && forwardDone) {
                for (Channel channel : forwardChannels) {
                    sendReport(currentReport, channel);
                }
            } else
                broadcastReport(reportToBeBroadcasted);

        }
        // if ((this instanceof RuleNode))
        // Scheduler.addToHighQueue(currentReport);
        // ;

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

    public static void main(String[] args) {
        Network network = new Network();
        Scheduler.initiate();

        Node base1 = Network.createNode("A", "propositionnode"); // Proposition Node
        Node base2 = Network.createVariableNode("X", "propositionnode"); // Proposition Node
        Node base4 = Network.createVariableNode("X", "propositionnode"); // Proposition Node

        Node base3 = Network.createNode("B", "propositionnode"); // Proposition
        Node base5 = Network.createNode("C", "propositionnode"); // Proposition
        Node base6 = Network.createVariableNode("Y", "propositionnode"); // Proposition
        Node base7 = Network.createVariableNode("Z", "propositionnode"); // Proposition

        Substitutions subs1 = new Substitutions();
        subs1.add(base2, base3);
        subs1.add(base4, base1);
        subs1.add(base6, base5);

        NodeSet freeVariables = new NodeSet();
        freeVariables.add(base7);
        base5.setFreeVariableSet(freeVariables);
        Substitutions newSubs = ((PropositionNode) base5).onlyRelevantSubs(subs1);
        System.out.println(newSubs.toString());
    }
}