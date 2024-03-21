package edu.guc.mind_graf.mgip.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.KnownInstance;
import edu.guc.mind_graf.mgip.Report;
import edu.guc.mind_graf.mgip.ReportType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.AntecedentToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.MatchChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.nodes.PropositionNode;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
public class BridgeRule extends RuleNode {

    public BridgeRule(String name, Boolean isVariable) {
        super(name, isVariable);
        // TODO Auto-generated constructor stub
    }

    public BridgeRule(DownCableSet downCableSet) {
        super(downCableSet);

    }

    public static void applyRuleHandler(Report report, BridgeRule node) {
        // TODO Ossama

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
        Context currContext = edu.guc.mind_graf.network.Controller.getContext(currentContextName);
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
        Context currContext = edu.guc.mind_graf.network.Controller.getContext(currentContext);
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
                if (this.supported(currentContext, currentAttitude)) {
                    NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                    NodeSet antArgNodesToConiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                            currentRequest,
                            filterRuleSubs, false);
                    Context currContext = edu.guc.mind_graf.network.Controller.getContext(currentContext);
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
                    boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
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

    protected void processSingleReports(Report currentReport) throws NoSuchTypeException {
        String currentReportContextName = currentReport.getContextName();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;

        boolean assertedInContext = supported(currentReportContextName, currentReportAttitudeID);
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
                        if (this.isForwardReport() == false) {
                            this.setForwardReport(true);
                            requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                        }
                    } else {
                        grandparentMethodRequest(tempRequest);

                    }
                } else {
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentReportAttitudeID);
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean compatibilityCheck = onlySubsBindFreeVar
                                .compatible(currentKISubs);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (compatibilityCheck && supportCheck) {
                            if (notBound) {
                                if (this.isForwardReport() == false) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);
                                }

                            } else {
                                if (this.isForwardReport() == false) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                                }

                            }

                        }
                    }
                    if (this.isForwardReport() == false) {
                        this.setForwardReport(true);
                        grandparentMethodRequest(tempRequest);
                    }
                }
            } else {
                /** Backward Inference */
                applyRuleHandler(currentReport, this);

            }
        } else {
            /** Not AntecedentToRule Channel */
            Substitutions switchSubs = new Substitutions();

            Channel tempChannel = new Channel(switchSubs, currentReportSubs, currentReportContextName,
                    currentReportAttitudeID, currentReport.getRequesterNode());
            Request tempRequest = new Request(tempChannel, null);
            if (forwardReportType) {
                super.grandparentMethodReport(currentReport);
                // Rule is asserted we do backward inference
                // law heya antecedent yeb2a teb3at lel rule ka2en galna request men el
                // consequent. before we continue the forward we have to ask the antecedent
                // first hat3amel ma3ah akeni non rule el awel we hab3at el report lel matched
                // wel antecedents
                if (!this.isOpen()) {
                    Scheduler.addNodeAssertionThroughFReport(currentReport, this);
                }

                this.setForwardReport(true);
                requestAntecedentsNotAlreadyWorkingOn(tempRequest);

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

                    NodeSet argAntNodes = getDownAntArgNodeSet();
                    NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes,
                            tempRequest,
                            currentReportSubs, false);

                    for (Channel outConsChannel : outgoingRuleConsChannels) {
                        Substitutions outConsChannelSubs = outConsChannel.getFilterSubstitutions();
                        Substitutions onlySubsBindFreeVarChnl = onlyRelevantSubs(outConsChannelSubs);
                        boolean compatibilityCheck = onlySubsBindFreeVar
                                .compatible(onlySubsBindFreeVarChnl);

                        if (compatibilityCheck) {
                            Substitutions unionSubs = Substitutions.union(currentReportSubs, outConsChannelSubs);
                            Context currContext = edu.guc.mind_graf.network.Controller.getContext(currentReportContextName);
                            for (Node currentNode : remainingArgAntNodes) {
                                int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
                                Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchSubs,
                                        unionSubs,
                                        currentReportContextName, currentNodeAttitude, -1, this);
                                Scheduler.addToLowQueue(newRequest);
                            }
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
    }
}
