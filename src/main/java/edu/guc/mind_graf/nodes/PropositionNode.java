package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.acting.rules.WhenDoNode;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.cables.UpCable;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoPlansExistForTheActException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.matching.Match;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.KnownInstanceSet;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.*;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

import java.util.*;

public class PropositionNode extends Node {
    protected ChannelSet outgoingChannels;
    protected ChannelSet forwardChannels;
    protected KnownInstanceSet knownInstances;
    protected boolean forwardDone;
    protected Support support;
    protected PropositionNodeSet justificationSupportDependents;
    protected PropositionNodeSet assumptionSupportDependents;
    protected ArrayList<Integer> graded;

    public PropositionNode(String name, Boolean isVariable) {
        super(name, isVariable);

        outgoingChannels = new ChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
        support = new Support(this.getId());
        justificationSupportDependents = new PropositionNodeSet();
        assumptionSupportDependents = new PropositionNodeSet();
        support = new Support(this.getId());
        justificationSupportDependents = new PropositionNodeSet();
        assumptionSupportDependents = new PropositionNodeSet();
        graded = new ArrayList<>();
    }

    public PropositionNode(DownCableSet downCableSet) {
        super(downCableSet);

        outgoingChannels = new ChannelSet();
        forwardChannels = new ChannelSet();
        forwardDone = false;
        knownInstances = new KnownInstanceSet();
        support = new Support(this.getId());
        justificationSupportDependents = new PropositionNodeSet();
        assumptionSupportDependents = new PropositionNodeSet();
        graded = new ArrayList<>();
    }


    /**
     * @return the graded
     */
    public ArrayList<Integer> getGraded() {
        return graded;
    }

    /**
     * @param graded the graded to set
     */
    public void setGraded(ArrayList<Integer> graded) {
        this.graded = graded;
    }

    /**
     * /**
     *
     * @return the support
     */
    public Support getSupport() {
        return support;
    }

    /**
     * @return the justificationSupportDependents
     */
    public PropositionNodeSet getJustificationSupportDependents() {
        return justificationSupportDependents;
    }

    /**
     * @return the assumptionSupportDependents
     */
    public PropositionNodeSet getAssumptionSupportDependents() {
        return assumptionSupportDependents;
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
        UpCable consequentCable = this.getUpCableSet().get("cq");
        UpCable argsCable = this.getUpCableSet().get("arg");
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
        UpCable argsCable = this.getUpCableSet().get("arg");
        UpCable antCable = this.getUpCableSet().get("ant");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (antCable != null) {
            antCable.getNodeSet().addAllTo(ret);
        }

        return ret;
    }

    /***
     * Method getting the NodeSet that this current node is considered an if
     * to
     *
     * @return nodeSet
     */
    public NodeSet getUpIfDomRuleNodeSet(int attitude) {

        NodeSet ret = new NodeSet();
        UpCable ifCable = this.getUpCableSet().get(attitude + "-" + "if");
        if (ifCable != null) {
            ifCable.getNodeSet().addAllTo(ret);
        }


        return ret;
    }

    public NodeSet getUpWhenDomRuleNodeSet(int attitude) {

        NodeSet ret = new NodeSet();
        UpCable whenCable = this.getUpCableSet().get(attitude + "-when");
        if (whenCable != null) {
            whenCable.getNodeSet().addAllTo(ret);

        }

        return ret;
    }

    public NodeSet getDownIfNodeSet(int attitude) {
        NodeSet ret = new NodeSet();
        DownCable whenCable = this.getDownCableSet().get(attitude + "-if");
        if (whenCable != null) {
            whenCable.getNodeSet().addAllTo(ret);
        }

        return ret;
    }

    /***
     * Method getting the NodeSet of the when down cables for this current
     * node
     *
     * @return nodeSet
     */

    public NodeSet getDownWhenNodeSet(int attitude) {
        NodeSet ret = new NodeSet();
        DownCable whenCable = this.getDownCableSet().get(attitude + "-when");
        if (whenCable != null) {
            whenCable.getNodeSet().addAllTo(ret);
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
        DownCable argsCable = this.getDownCableSet().get("arg");
        DownCable antCable = this.getDownCableSet().get("ant");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (antCable != null) {
            antCable.getNodeSet().addAllTo(ret);
        }

        return ret;
    }

    /***
     * Method getting the NodeSet of the acts this current
     * node
     *
     * @return nodeSet
     */

    public NodeSet getDownDoNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable actCable = this.getDownCableSet().get("do");

        if (actCable != null) {
            actCable.getNodeSet().addAllTo(ret);
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
            case IfRule:
                newChannel = new IfToRuleChannel(switchSubstitutions,
                        filterSubstitutions, contextName,
                        attitudeId,
                        requesterNode);
                break;
            case WhenRule:
                newChannel = new WhenToRuleChannel(switchSubstitutions,
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
        // if (type == ChannelType.Act) {
        // currentChannel = ((ActNode)
        // targetNode).getOutgoingChannels().getChannel(newChannel);

        // } else {
        currentChannel = ((PropositionNode) targetNode).getOutgoingChannels().getChannel(newChannel);

        // }
        if (currentChannel == null) {
            /* BEGIN - Helpful Prints */
            System.out.println("Channel of type " + newChannel.getChannelType()
                    + " is successfully created and used for further operations");
            /* END - Helpful Prints */
            Request newRequest = new Request(newChannel, targetNode);
            // if (type == ChannelType.Act) {
            // ((ActNode) targetNode).addToOutgoingChannels(newChannel);

            // } else {
            ((PropositionNode) targetNode).addToOutgoingChannels(newChannel);

            // }
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
        if (currentChannel.getChannelType() == ChannelType.Matched) {
            //union channel support with report support
        }
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
     * supported in a specified grade level in a specific attitude in a desired context or not.
     *
     *
     *  @param desiredContextName context to check in
     *  @param desiredAttitudeID attitude to check in
     *  @param level    level to check in
     * @return boolean
     */

    public boolean supported(String desiredContextName, int desiredAttitudeID, int level) {
        boolean supported = false;
        Context desiredContext = ContextController.getContext(desiredContextName);
        if (desiredContext.isHypothesis(level, desiredAttitudeID, this)) {
            return true;
        }
        //TODO: mohsen get(level) might return null;
        if (this.support.getAssumptionSupport().get(level).get(desiredAttitudeID) == null) {
            return false;
        }
        for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> currSupport : this.support.getAssumptionSupport().get(level).get(desiredAttitudeID)) {
            for (Integer key : currSupport.getFirst().keySet()) {
                if (currSupport.getFirst().get(key).getFirst().isSubset(desiredContext.getAttitudeProps(level, key).getFirst()) && currSupport.getFirst().get(key).getSecond().isSubset(desiredContext.getAttitudeProps(level, key).getSecond()) && currSupport.getSecond().isSubset(desiredContext.getAttitudeProps(level, key).getFirst())) {
                    supported = true;
                } else {
                    supported = false;
                    break;
                }
            }
            if (supported) {
                return true;
            }
        }

        return supported;

    }


    /***
     * Makes this node a hypothesis in the specified attitude in the desired context
     *
     *
     * @param desiredContextName context to make hypothesis in
     * @param attitude attitude to make hypothesis in
     */
    public void setHyp(String desiredContextName, int attitude) {
        Context desiredContext = ContextController.getContext(desiredContextName);
        desiredContext.getAttitudeProps(0, attitude).getFirst().add(this.getId());
        this.support.setHyp(attitude);
    }


    /***
     * Forgets this node from the support of other nodes dependent on it
     */
    public void removeNodeFromOtherNodesSupport() {
        HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();
        int[] assumptionDependents = this.getAssumptionSupportDependents().getProps();
        for (int i = 0; i < assumptionDependents.length; i++) {
            PropositionNode dependent = (PropositionNode) networkPropositions.get(assumptionDependents[i]);
            dependent.getSupport().removeNodeFromAssumptions(this.getId());
        }
        int[] justificationDependents = this.getJustificationSupportDependents().getProps();
        for (int i = 0; i < justificationDependents.length; i++) {
            PropositionNode dependent = (PropositionNode) networkPropositions.get(justificationDependents[i]);
            dependent.getSupport().removeNodeFromJustifications(this.getId());
        }
    }

    public void ForgetNodeFromOtherNodesSupport() {
        HashMap<Integer, Node> networkPropositions = Network.getPropositionNodes();
        int[] assumptionDependents = this.getAssumptionSupportDependents().getProps();
        for (int i = 0; i < assumptionDependents.length; i++) {
            PropositionNode dependent = (PropositionNode) networkPropositions.get(assumptionDependents[i]);
            dependent.getSupport().ForgetNodeFromAssumptions(this.getId());
        }
        int[] justificationDependents = this.getJustificationSupportDependents().getProps();
        for (int i = 0; i < justificationDependents.length; i++) {
            PropositionNode dependent = (PropositionNode) networkPropositions.get(justificationDependents[i]);
            dependent.getSupport().ForgetNodeFromJustifications(this.getId());
        }
    }

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
                    toBeSent.getInferenceType(), sentTo, this);
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

    protected void sendReportToWhenNodeSet(NodeSet nodeset, Report toBeSent) {
        for (Node sentTo : nodeset) {
            Substitutions reportSubs = toBeSent.getSubstitutions();
            Substitutions switchSubs = new Substitutions();
            Report newReport = new Report(reportSubs, toBeSent.getSupport(), toBeSent.getAttitude(), toBeSent.isSign(),
                    toBeSent.getInferenceType(), sentTo, this);
            // new report every loop due to duplications in queues when testing.
            newReport.setContextName(toBeSent.getContextName());
            newReport.setReportType(toBeSent.getReportType());
            Channel newChannel = new WhenToRuleChannel(switchSubs, reportSubs,
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
                    toBeSent.getInferenceType(), currentMatch.getNode(), this);
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

    protected void sendReportToConsequents(NodeSet nodeset, Report toBeSent) {
        for (Node sentTo : nodeset) {
            if (sentTo == null)
                System.out.println("sent to is null");
            Substitutions reportSubs = toBeSent.getSubstitutions();
            Substitutions switchSubs = new Substitutions();
            Report newReport = new Report(reportSubs, toBeSent.getSupport(), toBeSent.getAttitude(), toBeSent.isSign(),
                    toBeSent.getInferenceType(), sentTo, this);
            newReport.setContextName(toBeSent.getContextName());
            newReport.setReportType(toBeSent.getReportType());
            Channel newChannel = new RuleToConsequentChannel(switchSubs, reportSubs,
                    toBeSent.getContextName(), toBeSent.getAttitude(),
                    sentTo);
            if (toBeSent.getInferenceType() == InferenceType.FORWARD) {
                forwardChannels.addChannel(newChannel);

            }
            sendReport(newReport, newChannel);
        }
    }

    /***
     * Helper method responsible for establishing channels between this current node
     * and each of the NodeSet to further request instances with the given inputs
     *
     * @param nodeSet            NodeSet to be sent to
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
            //Support support=currentMatch.getSupport();
            //pass support to establish channel

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
            Support reportSupport = new Support(-1);
            reportSupport.addNode(currentAttitudeID, this);
            Substitutions subs = substitutions == null ? new Substitutions() : substitutions;
            Substitutions subs2 = new Substitutions();
            Report toBeSent = new Report(subs, reportSupport, currentAttitudeID, reportSign, inferenceType, null, this);
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
                case WhenRule:
                    NodeSet whenDoRuleNodes = getUpWhenDomRuleNodeSet(currentAttitudeID);
                    if (whenDoRuleNodes != null) {
                        sendReportToWhenNodeSet(whenDoRuleNodes, toBeSent);
                    }

                    if (this instanceof RuleNode) {
                        NodeSet whenNodes = getDownWhenNodeSet(currentAttitudeID);
                        if (whenNodes != null) {
                            ((WhenDoNode) this).setForwardReport(true);
                            sendRequestsToNodeSet(whenNodes, subs, subs2, currentContextName,
                                    currentAttitudeID,
                                    channelType, this);

                        }

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
                case IfRule:
                    NodeSet ifRules = getUpIfDomRuleNodeSet(currentAttitudeID);
                    if (ifRules != null) {
                        Substitutions filtersubs1 = substitutions == null ? new Substitutions() : substitutions;
                        Substitutions switchSubs1 = new Substitutions();
                        sendRequestsToNodeSet(ifRules, filtersubs1, switchSubs1, currentContextName,
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
     * This method is implemented to check if this node represents a â€�whoâ€�, â€�whereâ€�,
     * â€�whichâ€� question, asking for bindings of a specific a specific set of
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
     * @throws DirectCycleException
     */

    private Report attemptAddingReportToKnownInstances(Report report) throws DirectCycleException {
        if (this.isOpen()) {
            boolean flag;
            boolean channelCheck = report.getReportType() == ReportType.Matched
                    || report.getReportType() == ReportType.RuleCons;
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
     * @throws NoPlansExistForTheActException
     * @throws DirectCycleException
     *
     */
    public void deduce() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        /* BEGIN - Helpful Prints */
        System.out.println("deduce() method initated.");
        System.out.println("-------------------------\n");
        /* END - Helpful Prints */
        Scheduler.initiate();
        String currentContextName = ContextController.getCurrContextName();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your desired attitude: ");
        String att = scanner.nextLine();
        scanner.close();
        int currentattitudeID = 1;
        // given by the user
        System.out.println("Backward Inference initiated in Context: " + currentContextName + " & Attitude: "
                + currentattitudeID);
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
                        InferenceType.BACKWARD, this, this);
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
                        InferenceType.BACKWARD, this, this);
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
        /* BEGIN - Helpful Prints */
        System.out.println("Sending to DoIf rule nodes during deduce()");
        getNodesToSendRequest(ChannelType.IfRule, currentContextName, currentattitudeID, null);

        System.out.println(Scheduler.schedule());
        System.out.println(Scheduler.getBackwardAssertedReplyNodes().values().toString());

    }

    /***
     * this method is used to initiate the whole process of forward inference
     *
     * @return
     * @throws NoSuchTypeException
     * @throws NoPlansExistForTheActException
     * @throws DirectCycleException
     */
    public void add() throws NoSuchTypeException, NoPlansExistForTheActException, DirectCycleException {
        /* BEGIN - Helpful Prints */
        System.out.println("add() method initated.\n");
        System.out.println("-------------------------");
        /* END - Helpful Prints */
        Scheduler.initiate();
        String currentContextName = ContextController.getCurrContextName();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your desired attitude: ");
        String att = scanner.nextLine();
        scanner.close();
        int currentAttitudeID = 0;
        // given by the user
        boolean reportSign = true;
        System.out.println("Forward Inference initiated in Context: " + currentContextName + " & Attitude: "
                + currentAttitudeID);
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
        System.out.println("Sending to WhenDo rule nodes during add()");
        /* END - Helpful Prints */
        getNodesToSendReport(ChannelType.WhenRule, currentContextName, currentAttitudeID, null, reportSign,
                InferenceType.FORWARD);
        System.out.println(Scheduler.schedule());
        System.out.println("*New Knowledge inferred: " + Scheduler.getForwardAssertedNodes().values().toString());

    }

    /***
     * Method for a certain node to process incoming requests
     *
     * @return
     * @throws NoSuchTypeException
     * @throws DirectCycleException
     */
    public void processRequests() throws NoSuchTypeException, DirectCycleException {
        Request requestHasTurn = Scheduler.getLowQueue().poll();

        processSingleRequests(requestHasTurn);

    }

    /***
     * Request handling in Non-Rule proposition nodes.
     *
     * @param currentRequest
     * @return
     * @throws DirectCycleException
     * @throws NoSuchTypeException
     */
    protected void processSingleRequests(Request currentRequest) throws NoSuchTypeException, DirectCycleException {
        System.out.println(this.getName() + " Processing Requests as a Proposition node");

        Channel currentChannel = currentRequest.getChannel();
        String currentContext = currentChannel.getContextName();
        int currentAttitude = currentChannel.getAttitudeID();
        Node requesterNode = currentChannel.getRequesterNode();
        Substitutions reportSubstitutions = new Substitutions();

        if (this.supported(currentContext, currentAttitude, 0)) {
            System.out.println(this.getName() + " is supported");
            Support reportSupport = new Support(-1);
            reportSupport.addNode(currentAttitude, this);
            Report NewReport = new Report(reportSubstitutions, reportSupport, currentAttitude, true,
                    InferenceType.BACKWARD, requesterNode, requesterNode);
            NewReport.setContextName(currentContext);
            NewReport.setReportType(currentChannel.getChannelType());
            sendReport(NewReport, currentRequest.getChannel());

        } else {
            System.out.println(this.getName() + " is not supported");
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
                                InferenceType.BACKWARD, requesterNode, this);
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
                                InferenceType.BACKWARD, requesterNode, this);
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

                NodeSet dominatingRules2 = getUpIfDomRuleNodeSet(currentAttitude);
                if (dominatingRules2 != null) {
                    NodeSet remainingNodes2 = removeAlreadyEstablishedChannels(dominatingRules2,
                            currentRequest, filterSubs);
                    sendRequestsToNodeSet(remainingNodes2, filterSubs, switchSubs, currentContext,
                            currentAttitude,
                            ChannelType.IfRule, this);


                }

                if (!(currentChannel instanceof MatchChannel)) {
                    List<Match> matchesList = new ArrayList<Match>();

                    //testComplexActWithAssertedPreconditions()
                    // if(this.getName().equals("M9")){

                    //     Node n=Network.getMolecularNodes().get("act_precondition").get("act_17precondition11");
                    //     matchesList.add(new Match(filterSubs, switchSubs, n, 0));
                    // }
                    // if(this.getName().equals("M10")){

                    //     Node n=Network.getMolecularNodes().get("act_plan").get("act_17plan15");
                    //     matchesList.add(new Match(filterSubs, switchSubs, n, 0));
                    // }
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
     * @throws DirectCycleException
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException, DirectCycleException {
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
                    Support reportSupport = new Support(-1);
                    reportSupport.addNode(reportToBeBroadcasted.getAttitude(), supportNode);
                    reportToBeBroadcasted.setSupport(reportSupport);
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

            NodeSet dominatingWhenRules = getUpWhenDomRuleNodeSet(reportToBeBroadcasted.getAttitude());
            if (dominatingWhenRules != null) {
                sendReportToWhenNodeSet(dominatingWhenRules, reportToBeBroadcasted);
            }

        } else if (forwardReportType && forwardDone) {
            for (Channel channel : forwardChannels) {
                sendReport(reportToBeBroadcasted, channel);
            }
        } else
            broadcastReport(reportToBeBroadcasted);

    }


    /***
     * adds the justification support of the specified support to this nodes support in the specified level unless there is a direct cycle
     * @param support support to be added
     */
    private void addJustificationBasedSupport(Support support) {
        try {
            this.support.union(support);
        } catch (DirectCycleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /***
     * adds the specified support to this node's support in the specified level unless there is a direct cycle
     * @param attitude attitude to add support in
     * @param level level to add support in
     * @param support support to be added
     * @param bridgeRules Bridge Rules used to get this support
     */
    public void addJustificationBasedSupport(int attitude, int level, HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> support, PropositionNodeSet bridgeRules) throws DirectCycleException {
        Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> pair = new Pair<>(support, bridgeRules);
        ArrayList<Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet>> list = new ArrayList<>();
        list.add(pair);
        this.support.addJustificatoinSupportForAttitude(attitude, level, list);
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

    public int getGradeFromParent() {
        UpCable propCable = this.getUpCable("prop");
        if (propCable == null) {
            return 0;
        }

        NodeSet propNodeSet = propCable.getNodeSet();
        if (propNodeSet.isEmpty()) {
            return 0;
        }

        PropositionNode parentNode = (PropositionNode) propNodeSet.iterator().next();
        DownCable gradeCable = parentNode.getDownCable("grade");
        if (gradeCable == null) {
            return 0;
        }

        NodeSet gradeNodeSet = gradeCable.getNodeSet();
        if (gradeNodeSet.isEmpty()) {
            return 0;
        }

        Node gradeNode = gradeNodeSet.iterator().next();
        if(parentNode.isGraded()){
            //this merges grade on the level of a graded prop son g(g(p,2),4) will merge 2 and 4 using the mergeGrades() operator
            return ContextController.mergeGrades().applyAsInt(Integer.parseInt(gradeNode.getName()),parentNode.getGradeFromParent());
        }else{
            return Integer.parseInt(gradeNode.getName());
        }
    }

    public boolean isGraded() {
        UpCable propCable = this.getUpCable("prop");
        if (propCable == null) {
            return false;
        }

        NodeSet propNodeSet = propCable.getNodeSet();
        if (propNodeSet.isEmpty()) {
            return false;
        }

        PropositionNode parentNode = (PropositionNode) propNodeSet.iterator().next();
        DownCable gradeCable = parentNode.getDownCable("grade");
        if (gradeCable == null) {
            return false;
        }

        NodeSet gradeNodeSet = gradeCable.getNodeSet();
        return !gradeNodeSet.isEmpty();
    }
}