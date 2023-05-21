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

    public Substitutions onlyRelevantSubs(Substitutions filterSubs) {
        // System.out.println("i entered");
        NodeSet freeVariablesSet = this.getFreeVariables();
        // System.out.println(freeVariablesSet.toString());
        Substitutions relevantSubs = new Substitutions();
        for (Node variableNode : freeVariablesSet.getValues()) {
            for (Node var : filterSubs.getMap().keySet()) {
                // System.out.println(filterSubs.getMap().keySet());
                // System.out.println("enterrrr");
                Node value = filterSubs.getMap().get(var);
                if (var.getName().equals(variableNode.getName())) {
                    // System.out.println("1");
                    relevantSubs.add(var, value);
                }
            }
        }
        if (relevantSubs.size() == 0) {
            System.out.println("There are no Relevant Substitutions in the input substitutions");
        }
        return relevantSubs;
    }

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
                            .compatible(currentKISubs);
                    boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                            currentContext,
                            currentAttitude);
                    if (subSetCheck && supportCheck) {
                        if (!isNotBound) {
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                            return;
                        } else
                            requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentKnownInstance);
                        return;
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

        boolean assertedInContext = asserted(currentReportContextName, currentReportAttitudeID);
        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(currentReportSubs);

        if (currentReport.getReportType() == ReportType.AntRule) {
            Channel tempChannel = new AntecedentToRuleChannel(null, currentReportSubs, currentReportContextName,
                    currentReportAttitudeID, currentReport.getRequesterNode());
            Request tempRequest = new Request(tempChannel, null);
            /** AntecedentToRule Channel */
            if (forwardReportType) {
                /** Forward Inference */
                if (!this.isOpen()) {
                    /** Close Type Implementation */
                    if (assertedInContext) {
                        forwardDone = true;
                        requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                        //// Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                        //// tempChannel);
                        // handleResponseOfApplyRuleHandler(ruleResponse, currentReport, tempChannel);
                        // TODO Ossama
                    } else {
                        // boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                        super.processSingleRequests(tempRequest);
                        // NodeSet dominatingRules = getUpAntDomRuleNodeSet();
                        // NodeSet toBeSentToDom = removeAlreadyEstablishedChannels(dominatingRules,
                        // tempRequest,
                        // currentReportSubs, ruleType);
                        // sendRequestsToNodeSet(toBeSentToDom, currentReportSubs, null,
                        // currentReportContextName,
                        // currentReportAttitudeID,
                        // ChannelType.AntRule, this);
                        // List<Match> matchesList = new ArrayList<Match>();
                        // // Matcher.match(this, ruleNodeExtractedSubs);
                        // List<Match> toBeSentToMatch = removeAlreadyEstablishedChannels(matchesList,
                        // tempRequest,
                        // currentReportSubs);
                        // sendRequestsToMatches(toBeSentToMatch, currentReportSubs, null,
                        // currentReportContextName,
                        // currentReportAttitudeID, ChannelType.MATCHED, this);
                    }
                } else {
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentReportAttitudeID);
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean subSetCheck = onlySubsBindFreeVar
                                .compatible(currentKISubs);
                        boolean supportCheck = currentKnownInstance.anySupportAssertedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (subSetCheck && supportCheck) {
                            if (notBound) {
                                requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);

                            } else {
                                requestAntecedentsNotAlreadyWorkingOn(tempRequest);

                            }

                        }
                    }
                    // Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                    // tmpChanl);
                    // handleResponseOfApplyRuleHandler(ruleResponse, currentReport,
                    // tmpChanl);
                    // TODO Ossama
                    boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                    NodeSet dominatingRules = getUpConsDomRuleNodeSet();
                    NodeSet toBeSentToDom = removeAlreadyEstablishedChannels(dominatingRules,
                            tempRequest,
                            currentReportSubs, ruleType);
                    Substitutions switchSubs = new Substitutions();
                    sendRequestsToNodeSet(toBeSentToDom, onlySubsBindFreeVar, switchSubs,
                            currentReportContextName,
                            currentReportAttitudeID,
                            ChannelType.RuleCons, this);
                    List<Match> matchingNodes = new ArrayList<>();
                    // Matcher.match(this, ruleNodeExtractedSubs);
                    List<Match> toBeSentToMatch = removeAlreadyEstablishedChannels(matchingNodes,
                            tempRequest,
                            currentReportSubs);
                    sendRequestsToMatches(toBeSentToMatch, onlySubsBindFreeVar, null,
                            currentReportContextName,
                            currentReportAttitudeID, ChannelType.MATCHED, this);
                    // super.processSingleRequests(tempRequest);
                }
            } else {
                /** Backward Inference */
                // Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                // currentChannel);
                // handleResponseOfApplyRuleHandler(ruleResponse, currentReport,
                // currentChannel);
                // currentChannelReportBuffer.removeReport(currentReport);
                // TODO Ossama

            }
        } else {
            /** Not AntecedentToRule Channel */
            if (forwardReportType) {
                super.processSingleReports(currentReport);
                // Rule is asserted we do backward inference
                // law heya antecedent yeb2a teb3at lel rule ka2en galna request men el
                // consequent. before we continue the forward we have to ask the antecedent
                // first hat3amel ma3ah akeni non rule el awel we hab3at el report lel matched
                // wel antecedents
                if (!this.isOpen()) {
                    Scheduler.addNodeAssertionThroughFReport(currentReport, this);
                    // we gali positive reports
                }
                Channel tempChannel = new Channel(null, currentReportSubs, currentReportContextName,
                        currentReportAttitudeID, currentReport.getRequesterNode());
                Request tempRequest = new Request(tempChannel, null);
                forwardReport = true;
                NodeSet argAntNodes = getDownAntArgNodeSet();
                Substitutions switchSubs = new Substitutions();
                boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes, tempRequest,
                        currentReportSubs, ruleType);
                sendRequestsToNodeSet(remainingArgAntNodes, currentReportSubs, switchSubs, currentReportContextName,
                        currentReportAttitudeID,
                        ChannelType.AntRule, this);

                // backward inference during forward inference
                // law ana 3andi consequents lazem acheck el antecedents el awel

            } else {
                Collection<Channel> outgoingMatchedChannels = getOutgoingMatchChannels();
                Collection<Channel> outgoingAntRuleChannels = getOutgoingAntecedentRuleChannels();
                Collection<Channel> outgoingRuleConsChannels = getOutgoingRuleConsequentChannels();

                for (Channel outMatchChannel : outgoingMatchedChannels) {
                    sendReport(currentReport, outMatchChannel);

                }
                for (Channel outAntChannel : outgoingAntRuleChannels) {
                    sendReport(currentReport, outAntChannel);

                }
                Channel tempChannel = new Channel(null, currentReportSubs, currentReportContextName,
                        currentReportAttitudeID, currentReport.getRequesterNode());
                Request tempRequest = new Request(tempChannel, null);
                NodeSet argAntNodes = getDownAntArgNodeSet();
                Substitutions switchSubs = new Substitutions();
                boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes,
                        tempRequest,
                        currentReportSubs, ruleType);

                for (Channel outConsChannel : outgoingRuleConsChannels) {
                    if (currentReportSubs.isSubsetOf(outConsChannel.getFilterSubstitutions()))
                        sendRequestsToNodeSet(remainingArgAntNodes, currentReportSubs, switchSubs,
                                currentReportContextName,
                                currentReportAttitudeID,
                                ChannelType.AntRule, this);

                }

                // mmkn a broadcast the report over the outgoing channels we khalas
                // bass ana keda keda babroadcats fe process Single reports
                // hacheck el outgoing channels beta3ty incase backward we hab3at le matched we
                // antRule
                // law heya RuleCons bashoouf law el report's subs is compatible ma3 el filter
                // subs beta3et el channel if it is bab3at lel antecedents requests bel reps
                // subs
            }
        }

    }

    public void grandparentMethodRequest(Request currentRequest) {
        // Call the grandparent's method
        super.processSingleRequests(currentRequest);
    }

    public void grandparentMethodReport(Report currentReport) {
        // Call the grandparent's method
        super.processSingleReports(currentReport);
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
