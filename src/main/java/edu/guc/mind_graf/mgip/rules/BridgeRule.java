package edu.guc.mind_graf.mgip.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.AntecedentToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.MatchChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.PtreeNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class BridgeRule extends RuleNode {

    private HashMap <Node, Integer> antToAttitude;
    private  HashMap <Node, Integer> cqToAttitude;
    private int cAnt;

    public BridgeRule(DownCableSet downCableSet) {
        super(downCableSet);
        antToAttitude = new HashMap<>();
        cqToAttitude = new HashMap<>();
        for(DownCable downCable : downCableSet){
            if(downCable.getRelation().getName().contains("-ant")){ // assuming all antecedents of a bridge rule would be of the form 1-ant where 1 is the attitude id
                int attitude =  Integer.parseInt(downCable.getRelation().getName().split("-")[0]);
                for(Node nodeAnt : downCable.getNodeSet()){
                    antToAttitude.put(nodeAnt, attitude);
                }
            }
            else if(downCable.getRelation().getName().contains("-cq")){ // assuming all consequents of a bridge rule would be of the form 1-cq where 1 is the attitude id
                int attitude =  Integer.parseInt(downCable.getRelation().getName().split("-")[0]);
                for(Node nodeCq : downCable.getNodeSet()){
                    cqToAttitude.put(nodeCq, attitude);
                }
            }
        }
        PropositionNodeSet antecedents = new PropositionNodeSet();
        for(Node antNode : antToAttitude.keySet()){
            if(antNode.isOpen())
                antecedents.add(antNode);
        }
        cAnt = antToAttitude.keySet().size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, antecedents.size(), Integer.MAX_VALUE, 2);
    }

    public boolean mayTryToInfer() {
        if(cAnt < this.ruleInfoHandler.getConstantAntecedents().getPcount())
            return false;
        for(PtreeNode root : ((Ptree)ruleInfoHandler).getRoots()) {
            if(root.getSIndex().getAllRuleInfos().isEmpty()) {  // maybe should also check pcount of roots?
                return false;
            }
        }
        return true;
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        if(mayTryToInfer()) {
//            for (RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
            for(RuleInfo ri : this.getRootRuleInfos()){
                if (ri.getPcount() == antToAttitude.size())
                    inferrable[0].addRuleInfo(ri);
            }
        }
        return inferrable;
    }

    public void applyRuleHandler(Report report) {
        if(report.anySupportSupportedInAttitude(antToAttitude.get(report.getReporterNode()))) {
            super.applyRuleHandler(report);
        }
    }

    public void putInferenceReportOnQueue(Report report) {
        for(Node node : cqToAttitude.keySet()) {
            report.setAttitude(cqToAttitude.get(node));
            report.setRequesterNode(node);
            Scheduler.addToHighQueue(report);
        }
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
        Context currContext = ContextController.getContext(currentContextName);
        for (Node currentNode : argAntNodesToConsiderClose) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchSubs, unionSubs,
                    currentContextName, currentNodeAttitude, -1, this);
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
        Context currContext = ContextController.getContext(currentContext);
        for (Node currentNode : remainingAntArgNodeSet) {
            int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
            Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchRuleSubs, filterRuleSubs,
                    currentContext, currentNodeAttitude, -1, this);
            Scheduler.addToLowQueue(newRequest);
        }

    }

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     */
    protected void processSingleRequests(Request currentRequest) throws DirectCycleException {
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
                    Context currContext = ContextController.getContext(currentContext);
                    for (Node currentNode : antArgNodesToConiderClose) {
                        int currentNodeAttitude = currContext.getPropositionAttitude(currentNode.getId());
                        Request newRequest = establishChannel(ChannelType.AntRule, currentNode, switchRuleSubs,
                                filterRuleSubs,
                                currentContext, currentNodeAttitude, -1, this);
                        Scheduler.addToLowQueue(newRequest);

                    }

                } else
                    super.grandparentMethodRequest(currentRequest);

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
                        if (!this.isForwardReport()) {
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
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);
                                }

                            } else {
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                                }

                            }

                        }
                    }
                    if (!this.isForwardReport()) {
                        this.setForwardReport(true);
                        grandparentMethodRequest(tempRequest);
                    }
                }
            } else {
                /** Backward Inference */
                applyRuleHandler(currentReport);
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
                            Context currContext = ContextController.getContext(currentReportContextName);
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
