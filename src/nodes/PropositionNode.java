package nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import mgip.KnownInstance;
import mgip.InferenceType;
import mgip.KnownInstanceSet;
import mgip.Report;
import mgip.ReportType;
import mgip.Scheduler;
import mgip.matching.Match;
import mgip.requests.*;
import network.Controller;
import set.NodeSet;
import cables.DownCable;
import cables.DownCableSet;
import cables.UpCable;
import exceptions.NoSuchTypeException;
import set.PropositionNodeSet;
import components.Substitutions;

public class PropositionNode extends Node {
    protected ChannelSet outgoingChannels;
    protected ChannelSet forwardChannels;
    protected KnownInstanceSet knownInstances;
    protected boolean forwardDone;

    public PropositionNode(String name, Boolean isVariable) {
        super(name, isVariable);

        outgoingChannels = new ChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
    }

    public PropositionNode(DownCableSet downCableSet) {
        super(downCableSet);

        outgoingChannels = new ChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
    }

    /***
     * Method getting the NodeSet that this current node is considered a consequent
     * or argument
     * to
     * 
     * @return nodeSet
     */
    public NodeSet getUpConsDomRuleNodeSet() {
        NodeSet ret = new NodeSet();
        UpCable consequentCable = this.getUpCableSet().get("consequent");
        UpCable argsCable = this.getUpCableSet().get("args");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (consequentCable != null) {
            consequentCable.getNodeSet().addAllTo(ret);
        }
        return ret;
    }

    /***
     * Method getting the NodeSet that this current node is considered an antecedent
     * or argument
     * to
     * 
     * @return nodeSet
     */
    public NodeSet getUpAntDomRuleNodeSet() {
        NodeSet ret = new NodeSet();
        UpCable argsCable = this.getUpCableSet().get("args");
        UpCable antCable = this.getUpCableSet().get("antecedent");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (antCable != null) {
            antCable.getNodeSet().addAllTo(ret);
        }

        return ret;
    }

    /***
     * Method getting the NodeSet of the antecedents and arguments for this current
     * node
     * 
     * @return nodeSet
     */

    public NodeSet getDownAntArgNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable argsCable = this.getDownCableSet().get("args");
        DownCable antCable = this.getDownCableSet().get("antecedent");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (antCable != null) {
            antCable.getNodeSet().addAllTo(ret);
        }

        return ret;
    }

    /***
     * Method handling all types of sending requests, taking different inputs to
     * handle different cases.
     * 
     * @param type          type of request being addressed
     * @param targetNode    source Node being addressed
     * @param switchSubs    mapped substitutions from origin node
     * @param filterSubs    constraints substitutions for a specific request
     * @param contextName   context name used
     * @param attitudeId    attitude id used
     * @param matchType     int representing the match Type. -1 if not a matching
     *                      node scenario
     * @param requesterNode
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
            case Matched:
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
            case RuleCons:
                newChannel = new RuleToConsequentChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;
            case Introduction:
                newChannel = new IntroductionChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;
            default:
                newChannel = new ActChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;

        }
        Channel currentChannel;
        if (type == ChannelType.Act) {
            currentChannel = ((ActNode) targetNode).getOutgoingChannels().getChannel(newChannel);

        } else {
            currentChannel = ((PropositionNode) targetNode).getOutgoingChannels().getChannel(newChannel);

        }
        if (currentChannel == null) {
            /* BEGIN - Helpful Prints */
            System.out.println("Channel of type " + newChannel.getChannelType()
                    + " is successfully created and used for further operations");
            /* END - Helpful Prints */
            Request newRequest = new Request(newChannel, targetNode);
            if (type == ChannelType.Act) {
                ((ActNode) targetNode).addToOutgoingChannels(newChannel);

            } else {
                ((PropositionNode) targetNode).addToOutgoingChannels(newChannel);

            }
            return newRequest;
        }

        /* BEGIN - Helpful Prints */
        System.out.println(
                "Channel of type " + currentChannel.getChannelType()
                        + " was already established and re-enqueued for further operations");
        /* END - Helpful Prints */
        return new Request(currentChannel, targetNode);

    }

    public void printChannelSet(ChannelSet channelSet) {
        for (ChannelType channelType : channelSet.channels.keySet()) {
            System.out.println("Channel Type: " + channelType);

            Hashtable<String, Channel> channelHashtable = channelSet.channels.get(channelType);
            for (String channelId : channelHashtable.keySet()) {
                Channel channel = channelHashtable.get(channelId);
                System.out.println("Channel ID: " + channel.getIdCount() + " & Channel Subs: "
                        + channel.getFilterSubstitutions().toString());
                ;
                // Print other channel properties as needed
            }

            System.out.println(); // Add a blank line between channel types
        }
    }

    public void printKnownInstances(KnownInstanceSet knownInstances) {
        for (int attitudeId : knownInstances.positiveKInstances.keySet()) {
            System.out.println("Attitude: " + attitudeId);

            Hashtable<Substitutions, KnownInstance> kiHashtable = knownInstances.positiveKInstances.get(attitudeId);
            for (Substitutions subs : kiHashtable.keySet()) {
                KnownInstance KI = kiHashtable.get(subs);
                System.out.println("KI: " + KI.toString());
            }

            System.out.println();
        }
        for (int attitudeId : knownInstances.negativeKInstances.keySet()) {
            System.out.println("Attitude: " + attitudeId);

            Hashtable<Substitutions, KnownInstance> kiHashtable = knownInstances.negativeKInstances.get(attitudeId);
            for (Substitutions subs : kiHashtable.keySet()) {
                KnownInstance KI = kiHashtable.get(subs);
                System.out.println("KI: " + KI.toString());
            }

            System.out.println();
        }
    }

    /***
     * Used to send a report over a request through calling request.testReportToSend
     * 
     * @param report
     * @param currentChannel
     * @return boolean
     */
    public boolean sendReport(Report report, Channel currentChannel) {
        System.out.println("Sending Report (" + report.stringifyReport() + ") through the channel ("
                + currentChannel.getChannelType() + " of id " + currentChannel.getIdCount() + ")");
        if (currentChannel.testReportToSend(report)) {
            System.out.println("the report was succefully sent over channel ("
                    + currentChannel.getChannelType() + " " + currentChannel.getIdCount() + ")");

            return true;

        } else {
            System.out.println("the report has failed to be sent over channel ("
                    + currentChannel.getChannelType() + " " + currentChannel.stringifyChannelID() + ")");

            return false;

        }

    }

    /***
     * Trying to send a report to all outgoing channels
     * 
     * @param report
     * @return
     */
    public void broadcastReport(Report report) {
        for (Channel outChannel : outgoingChannels)
            sendReport(report, outChannel);

    }

    /***
     * The method is implemented in Context class.
     * is called to check whether a PropositionNode
     * is
     * supported in a specific attitude in a desired context or not.
     * 
     * 
     * @param desiredContextName
     * @param desiredAttitudeID
     * @return boolean
     */

    public boolean supported(String desiredContextName, int desiredAttitudeID) {
        return true;

    }
    // TODO Ahmed

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the non matching ones of the NodeSet to further reports
     * with the given inputs
     * 
     * @param nodeset  NodeSet to be sent to
     * @param toBeSent Substitutions to be passed
     * @return
     */
    protected void sendReportToNodeSet(NodeSet nodeset, Report toBeSent) {
        for (Node sentTo : nodeset) {
            Substitutions reportSubs = toBeSent.getSubstitutions();
            Substitutions switchSubs = new Substitutions();
            Report newReport = new Report(reportSubs, toBeSent.getSupport(), toBeSent.getAttitude(), toBeSent.isSign(),
                    toBeSent.getInferenceType(), sentTo);
            // new report every loop due to duplications in queues when testing.
            newReport.setContextName(toBeSent.getContextName());
            newReport.setReportType(toBeSent.getReportType());
            Channel newChannel = new AntecedentToRuleChannel(switchSubs, reportSubs,
                    toBeSent.getContextName(), toBeSent.getAttitude(),
                    sentTo);
            if (toBeSent.getInferenceType() == InferenceType.FORWARD) {
                forwardChannels.addChannel(newChannel);

            }
            sendReport(toBeSent, newChannel);
        }
    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the List<Match> to further reports instances with the given
     * inputs
     * 
     * @param nodeList
     * @param toBeSent
     * @return
     */
    protected void sendReportToMatches(List<Match> nodeList, Report toBeSent) {
        for (Match currentMatch : nodeList) {
            Report newReport = new Report(currentMatch.getFilterSubs(), toBeSent.getSupport(), toBeSent.getAttitude(),
                    toBeSent.isSign(),
                    toBeSent.getInferenceType(), currentMatch.getNode());
            newReport.setContextName(toBeSent.getContextName());
            newReport.setReportType(toBeSent.getReportType());
            Channel newChannel = new MatchChannel(currentMatch.getSwitchSubs(), newReport.getSubstitutions(),
                    newReport.getContextName(), newReport.getAttitude(), currentMatch.getMatchType(),
                    currentMatch.getNode());
            if (newReport.getInferenceType() == InferenceType.FORWARD) {
                forwardChannels.addChannel(newChannel);

            }

            sendReport(newReport, newChannel);
        }
    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the NodeSet to further request instances with the given inputs
     * 
     * @param ns            NodeSet to be sent to
     * @param filterSubs    Substitutions to be passed
     * @param switchSubs
     * @param contextName   latest request context
     * @param attitudeId
     * @param channelType
     * @param requesterNode
     * @return
     */
    protected void sendRequestsToNodeSet(NodeSet nodeSet, Substitutions filterSubs,
            Substitutions switchSubs, String contextName, int attitudeId,
            ChannelType channelType, Node requesterNode) {
        for (Node sentTo : nodeSet) {
            Request newRequest = establishChannel(channelType, sentTo, switchSubs, filterSubs,
                    contextName, attitudeId, -1, requesterNode);
            Scheduler.addToLowQueue(newRequest);
        }
    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the List<Match> to further request instances with the given
     * inputs
     * 
     * @param matchesList
     * @param filterSubs
     * @param switchSubs
     * @param contextId
     * @param attitudeId
     * @param channelType
     * @param requesterNode
     * @return
     * 
     */
    protected void sendRequestsToMatches(List<Match> matchesList, Substitutions filterSubs,
            Substitutions switchSubs,
            String contextId, int attitudeId, ChannelType channelType, Node requesterNode) {
        for (Match currentMatch : matchesList) {
            int matchType = currentMatch.getMatchType();
            PropositionNode matchedNode = (PropositionNode) currentMatch.getNode();

            Request newRequest = establishChannel(channelType, matchedNode,
                    switchSubs, filterSubs, contextId,
                    attitudeId, matchType, requesterNode);
            Scheduler.addToLowQueue(newRequest);
        }
    }

    /***
     * Method handling all types of Nodes retrieval and sending
     * reports to each Node Type
     * 
     * @param channelType        type of request being addressed
     * @param currentContextName context name used
     * @param currentAttitudeID  Attitude ID used
     * @param substitutions      channel substitutions applied over the channel
     * @param reportSign
     * @param inferenceType
     * @return
     */

    protected void getNodesToSendReport(ChannelType channelType, String currentContextName, int currentAttitudeID,
            Substitutions substitutions, boolean reportSign, InferenceType inferenceType) {

        try {
            PropositionNodeSet supportPropSet = new PropositionNodeSet();
            supportPropSet.add(this);
            Substitutions subs = substitutions == null ? new Substitutions() : substitutions;
            Substitutions subs2 = new Substitutions();
            Report toBeSent = new Report(subs, supportPropSet, currentAttitudeID, reportSign, inferenceType, null);
            toBeSent.setContextName(currentContextName);
            toBeSent.setReportType(channelType);
            switch (channelType) {
                case Matched:
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
                        sendRequestsToNodeSet(argAntNodes, subs, subs2, currentContextName,
                                currentAttitudeID,
                                channelType, this);

                    }
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
     * @param channelType        type of request being addressed
     * @param currentContextName context name used
     * @param currentAttitudeID  attitude ID used
     * @param substitutions      request substitutions applied over the request
     * @return
     */
    protected void getNodesToSendRequest(ChannelType channelType, String currentContextName,
            int currentAttitudeID,
            Substitutions substitutions) {
        try {
            switch (channelType) {
                case Matched:
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * This method is specifically implemented to filter nodeSet with a
     * certain
     * criteria. The filter is perfomed through iterating over each node decide
     * either to keep it or remove it according to the filtering criteria we are
     * applying over the nodeSet.
     * 
     * @param removeFromSet
     * @param currentRequest
     * @param toBeCompared
     * @return nodeSet
     */

    private NodeSet removeAlreadyEstablishedChannels(NodeSet removeFromSet, Request currentRequest,
            Substitutions toBeCompared) {
        NodeSet remainingNodes = new NodeSet();
        for (Node currentNode : removeFromSet) {
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
     * @param toBeCompared
     * @return list<Match>
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
                if (!isBound)
                    return true;

            }

        }
        return false;

    }

    /***
     * attempts adding a report to the known instances if a report is returned means
     * it was never added before and needs to be handled properly else if null
     * returned means it was handled and stored before
     * 
     * @param report
     * @return Report
     */

    private Report attemptAddingReportToKnownInstances(Report report) {
        if (this.isOpen()) {
            boolean flag;
            boolean channelCheck = report.getReportType() == ReportType.Matched
                    || report.getReportType() == ReportType.RuleCons 
                    || report.getReportType() == ReportType.Introduction;// Need to check
            if (channelCheck) {
                flag = knownInstances.addKnownInstance(report);
                System.out.println(
                        "Report " + report.stringifyReport() + " was just added to " + this.getName() + "'s KIs");
                if (flag == false) {
                    return report;

                } else {
                    return null;

                }
            }

        }
        return null;

    }

    /***
     * this method is used to initiate the whole process of backward inference
     * 
     * @return
     * @throws NoSuchTypeException
     * 
     */
    public void deduce() throws NoSuchTypeException {
        /* BEGIN - Helpful Prints */
        System.out.println("deduce() method initated.");
        System.out.println("-------------------------\n");
        /* END - Helpful Prints */
        Scheduler.initiate();
        String currentContextName = Controller.getCurrContext();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your desired attitude: ");
        String att = scanner.nextLine();
        scanner.close();
        int currentattitudeID = 1;
        // given by the user
        System.out.println("Backward Inference initiated in Context: " + currentContextName + " & Attitude: "
                + att);
        Scheduler.setOriginOfBackInf(this);
        Collection<KnownInstance> thePveKnownInstancesSet = knownInstances
                .getPositiveCollectionbyAttribute(
                        currentattitudeID);
        if (thePveKnownInstancesSet != null) {
            for (KnownInstance currentPveKnownInstance : thePveKnownInstancesSet) {

                PropositionNode replyNode = (PropositionNode) applySubstitution(
                        currentPveKnownInstance.getSubstitutions());
                Report currentPveReport = new Report(currentPveKnownInstance.getSubstitutions(),
                        currentPveKnownInstance.getSupports(), currentPveKnownInstance.getAttitudeID(),
                        true,
                        InferenceType.BACKWARD, this);
                currentPveReport.setContextName(currentContextName);
                System.out.println("A reply has been succefully added to the set of backward asserted reply nodes");
                Scheduler.addNodeAssertionThroughBReport(currentPveReport, replyNode);
            }
        } else {
            System.out.println(this.getName() + " doesn't have any positive known instances");
        }
        Collection<KnownInstance> theNveKnownInstancesSet = knownInstances
                .getNegativeCollectionbyAttribute(currentattitudeID);
        if (theNveKnownInstancesSet != null) {

            for (KnownInstance currentNveKnownInstance : theNveKnownInstancesSet) {
                PropositionNode replyNode = (PropositionNode) applySubstitution(
                        currentNveKnownInstance.getSubstitutions());
                Report currentNveReport = new Report(currentNveKnownInstance.getSubstitutions(),
                        currentNveKnownInstance.getSupports(), currentNveKnownInstance.getAttitudeID(),
                        false,
                        InferenceType.BACKWARD, this);
                currentNveReport.setContextName(currentContextName);
                System.out.println("A reply has been succefully added to the set of backward asserted reply nodes");

                Scheduler.addNodeAssertionThroughBReport(currentNveReport, replyNode);

            }
        } else {
            System.out.println(this.getName() + " doesn't have any negative known instances");
        }

        /* BEGIN - Helpful Prints */
        System.out.println("Sending to rule nodes during deduce()");
        /* END - Helpful Prints */
        getNodesToSendRequest(ChannelType.RuleCons, currentContextName, currentattitudeID, null);
        /* BEGIN - Helpful Prints */
        System.out.println("Sending to matching nodes during deduce()");
        /* BEGIN - Helpful Prints */
        getNodesToSendRequest(ChannelType.Matched, currentContextName,
                currentattitudeID, null);
        System.out.println(Scheduler.schedule());
        System.out.println(Scheduler.getBackwardAssertedReplyNodes().values().toString());

    }

    /***
     * this method is used to initiate the whole process of forward inference
     * 
     * @return
     */
    public void add() {
        /* BEGIN - Helpful Prints */
        System.out.println("add() method initated.\n");
        System.out.println("-------------------------");
        /* END - Helpful Prints */
        Scheduler.initiate();
        String currentContextName = Controller.getCurrContext();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your desired attitude: ");
        String att = scanner.nextLine();
        scanner.close();
        int currentAttitudeID = 1;
        // given by the user
        boolean reportSign = true;
        System.out.println("Forward Inference initiated in Context: " + currentContextName + " & Attitude: "
                + att);
        /* BEGIN - Helpful Prints */
        System.out.println("Sending to rule nodes during add()");
        /* END - Helpful Prints */
        getNodesToSendReport(ChannelType.AntRule, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);
        /* BEGIN - Helpful Prints */
        System.out.println("Sending to matching nodes during add()");
        /* END - Helpful Prints */
        getNodesToSendReport(ChannelType.Matched, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);
        System.out.println(Scheduler.schedule());
        System.out.println("*New Knowledge inferred: " + Scheduler.getForwardAssertedNodes().values().toString());

    }

    /***
     * Method for a certain node to process incoming requests
     * 
     * @return
     */
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
     * @return
     */
    protected void processSingleRequests(Request currentRequest) {
        System.out.println(this.getName() + " Processing Requests as a Proposition node");

        Channel currentChannel = currentRequest.getChannel();
        String currentContext = currentChannel.getContextName();
        int currentAttitude = currentChannel.getAttitudeID();
        Node requesterNode = currentChannel.getRequesterNode();
        Substitutions reportSubstitutions = new Substitutions();
        PropositionNodeSet supportNodeSet = new PropositionNodeSet();
        if (this.supported(currentContext, currentAttitude)) {
            supportNodeSet.add((PropositionNode) this);
            Report NewReport = new Report(reportSubstitutions, supportNodeSet, currentAttitude, true,
                    InferenceType.BACKWARD, requesterNode);
            // if (((RuleNode) requesterNode).isForwardReport() == true) {
            // NewReport.setInferenceType(InferenceType.FORWARD);

            // }
            NewReport.setContextName(currentContext);
            NewReport.setReportType(currentChannel.getChannelType());
            sendReport(NewReport, currentRequest.getChannel());

        } else {
            boolean sentSuccessfully = false;

            if (!(this instanceof RuleNode)) {

                Collection<KnownInstance> thePveKnownInstancesSet = knownInstances
                        .getPositiveCollectionbyAttribute(
                                currentChannel.getAttitudeID());
                if (thePveKnownInstancesSet == null) {

                } else {

                    for (KnownInstance currentPveKnownInstance : thePveKnownInstancesSet) {

                        Report currentPveReport = new Report(currentPveKnownInstance.getSubstitutions(),
                                currentPveKnownInstance.getSupports(), currentPveKnownInstance.getAttitudeID(),
                                true,
                                InferenceType.BACKWARD, requesterNode);
                        currentPveReport.setContextName(currentContext);

                        currentPveReport.setReportType(currentChannel.getChannelType());

                        sentSuccessfully |= sendReport(currentPveReport, currentRequest.getChannel());

                    }
                }

                Collection<KnownInstance> theNveKnownInstancesSet = knownInstances
                        .getNegativeCollectionbyAttribute(currentChannel.getAttitudeID());
                if (theNveKnownInstancesSet == null) {

                } else {
                    for (KnownInstance currentNveKnownInstance : theNveKnownInstancesSet) {
                        Report currentNveReport = new Report(currentNveKnownInstance.getSubstitutions(),
                                currentNveKnownInstance.getSupports(), currentNveKnownInstance.getAttitudeID(),
                                false,
                                InferenceType.BACKWARD, requesterNode);
                        currentNveReport.setContextName(currentContext);

                        currentNveReport.setReportType(currentChannel.getChannelType());

                        sentSuccessfully |= sendReport(currentNveReport, currentRequest.getChannel());

                    }
                }
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
                    // List<Match> remainingMatches = removeAlreadyEstablishedChannels(matchesList,
                    // currentRequest, filterSubs);
                    sendRequestsToMatches(matchesList, filterSubs, switchSubs,
                            currentContext, currentAttitude,
                            ChannelType.Matched, this);

                }
            }

        }

    }

    /***
     * Method for a certain node to process incoming reports
     * 
     * @return
     */
    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().poll();
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
     * @throws NoSuchTypeException
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException {
        System.out.println(this.getName() + " Processing Reports as a Proposition node");
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;

        Report reportToBeBroadcasted = attemptAddingReportToKnownInstances(currentReport);
        if (reportToBeBroadcasted != null) {// it didn't get handled before but if null won't be handled and saved again
                                            // but will be broadcasted to the other nodes in the network as new suppors
                                            // could be discovered

            if (reportToBeBroadcasted.getReportType() == ReportType.RuleCons) {
                PropositionNode supportNode = (PropositionNode) applySubstitution(
                        reportToBeBroadcasted.getSubstitutions());
                if (supportNode != null) {
                    supportNode.addJustificationBasedSupport(reportToBeBroadcasted.getSupport());
                    PropositionNodeSet reportSupportPropSet = new PropositionNodeSet();
                    reportSupportPropSet.add(supportNode);
                    reportToBeBroadcasted.setSupport(reportSupportPropSet);
                    if (reportToBeBroadcasted.getInferenceType() == InferenceType.FORWARD) {
                        System.out.println(
                                "A New Fact has been succefully added to the set of forward asserted nodes");

                        Scheduler.addNodeAssertionThroughFReport(reportToBeBroadcasted, supportNode);

                    } else if (this.equals(Scheduler.getOriginOfBackInf())
                            && reportToBeBroadcasted.getInferenceType() == InferenceType.BACKWARD) {
                        System.out.println(
                                "A reply has been succefully added to the set of backward asserted reply nodes");

                        Scheduler.addNodeAssertionThroughBReport(reportToBeBroadcasted, supportNode);
                    }
                }
            }
        }

        // TODO: GRADED PROPOSITIONS HANDLING REPORTS
        if (forwardReportType && !forwardDone) {
            forwardDone = true;
            if (reportToBeBroadcasted.getReportType() != ReportType.Matched) {
                List<Match> matchesReturned = new ArrayList<Match>();
                // list of matches with a node
                // Matcher.match(this);
                sendReportToMatches(matchesReturned, reportToBeBroadcasted);
            }
            NodeSet dominatingRules = getUpAntDomRuleNodeSet();
            sendReportToNodeSet(dominatingRules, reportToBeBroadcasted);

        } else if (forwardReportType && forwardDone) {
            for (Channel channel : forwardChannels) {
                sendReport(reportToBeBroadcasted, channel);
            }
        } else
            broadcastReport(reportToBeBroadcasted);

    }

    private void addJustificationBasedSupport(PropositionNodeSet support) {
        // TODO Ahmed

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
}