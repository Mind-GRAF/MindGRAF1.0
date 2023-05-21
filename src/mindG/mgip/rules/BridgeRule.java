package mindG.mgip.rules;

import java.lang.ModuleLayer.Controller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mindG.mgip.InferenceType;
import mindG.mgip.KnownInstance;
import mindG.mgip.Report;
import mindG.mgip.ReportType;
import mindG.mgip.Scheduler;
import mindG.mgip.matching.Match;
import mindG.mgip.matching.Substitutions;
import mindG.mgip.requests.AntecedentToRuleChannel;
import mindG.mgip.requests.Channel;
import mindG.mgip.requests.ChannelType;
import mindG.mgip.requests.MatchChannel;
import mindG.mgip.requests.Request;
import mindG.network.Context;
import mindG.network.Node;
import mindG.network.NodeSet;
import mindG.network.PropositionNode;
import mindG.network.RuleNode;
import mindG.network.cables.DownCableSet;

public class BridgeRule extends RuleNode {

    public BridgeRule(String name, Boolean isVariable) {
        super(name, isVariable);
        // TODO Auto-generated constructor stub
    }

    public BridgeRule(DownCableSet downCableSet) {
        super(downCableSet);

    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest, KnownInstance knownInstance) {
        Channel currentChannel = currentRequest.getChannel();
        String currentContextName = currentChannel.getContextName();
        Substitutions filterSubs = currentChannel.getFilterSubstitutions();
        Substitutions switchSubs = currentChannel.getSwitcherSubstitutions();
        Substitutions reportSubs = knownInstance.getSubstitutions();
        Substitutions unionSubs = Substitutions.union(filterSubs, reportSubs);

        NodeSet argAntCloseToMe = getDownAntArgNodeSet();
        NodeSet argAntNodesToConsiderClose = removeAlreadyEstablishedChannels(argAntCloseToMe,
                currentRequest,
                unionSubs, false);
        Context currContext = mindG.network.Controller.getContext(currentContextName);
        for (Node currentNode : argAntNodesToConsiderClose) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchSubs, unionSubs,
                    currentContextName, currentNodeAttitude, -1, (PropositionNode) this);
            Scheduler.addToLowQueue(newRequest);
        }
    }

    protected void requestAntecedentsNotAlreadyWorkingOn(Request currentRequest) {
        Substitutions filterRuleSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchRuleSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        String currentContext = currentRequest.getChannel().getContextName();

        NodeSet antArgNodeSet = getDownAntArgNodeSet();
        NodeSet remainingAntArgNodeSet = removeAlreadyEstablishedChannels(antArgNodeSet,
                currentRequest,
                filterRuleSubs, false);
        Context currContext = mindG.network.Controller.getContext(currentContext);
        for (Node currentNode : remainingAntArgNodeSet) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchRuleSubs, filterRuleSubs,
                    currentContext, currentNodeAttitude, -1, (PropositionNode) this);
            Scheduler.addToLowQueue(newRequest);
        }

    }

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
                    NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                    NodeSet antArgNodesToConiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                            currentRequest,
                            filterRuleSubs, false);
                    Context currContext = mindG.network.Controller.getContext(currentContext);
                    for (Node currentNode : antArgNodesToConiderClose) {
                        int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
                        Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchRuleSubs,
                                filterRuleSubs,
                                currentContext, currentNodeAttitude, -1, (PropositionNode) this);
                        Scheduler.addToLowQueue(newRequest);

                    }

                } else
                    super.grandparentMethodRequest(currentRequest);
                ;

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
                super.grandparentMethodRequest(currentRequest);
                return;

            }

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
                        grandparentMethodRequest(tempRequest); // NodeSet dominatingRules = getUpAntDomRuleNodeSet();
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
                    // boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                    NodeSet dominatingRules = getUpConsDomRuleNodeSet();
                    NodeSet toBeSentToDom = removeAlreadyEstablishedChannels(dominatingRules,
                            tempRequest,
                            currentReportSubs, false);
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
                super.grandparentMethodReport(currentReport);
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
                NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes, tempRequest,
                        currentReportSubs, false);
                Context currContext = mindG.network.Controller.getContext(currentReportContextName);
                for (Node currentNode : remainingArgAntNodes) {
                    int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
                    Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchSubs,
                            currentReportSubs,
                            currentReportContextName, currentNodeAttitude, -1, (PropositionNode) this);
                    Scheduler.addToLowQueue(newRequest);

                }
                // Collection<RuleResponse> ruleResponse = applyRuleHandler(currentReport,
                // currentChannel);
                // handleResponseOfApplyRuleHandler(ruleResponse, currentReport,
                // currentChannel);
                // currentChannelReportBuffer.removeReport(currentReport);
                // TODO Ossama

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
                NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes,
                        tempRequest,
                        currentReportSubs, false);

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

}
