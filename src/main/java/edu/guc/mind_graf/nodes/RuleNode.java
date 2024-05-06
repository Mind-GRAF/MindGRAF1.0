package edu.guc.mind_graf.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.AntecedentToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelSet;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.IfToRuleChannel;
import edu.guc.mind_graf.mgip.requests.MatchChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.mgip.requests.WhenToRuleChannel;
import edu.guc.mind_graf.mgip.rules.AndOr;
import edu.guc.mind_graf.mgip.rules.Thresh;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.acting.rules.DoIfNode;
import edu.guc.mind_graf.acting.rules.WhenDoNode;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;
import edu.guc.mind_graf.support.Support;

public abstract class RuleNode extends PropositionNode {
    private boolean forwardReport;
    protected RuleInfoHandler ruleInfoHandler;
    protected RuleInfoSet rootRuleInfos;

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);
        this.name = "P" + (Network.MolecularCount);
        this.forwardReport = false;
        rootRuleInfos = new RuleInfoSet();
    }

    public void applyRuleHandler(Report report) throws NoSuchTypeException {

        try{
            RuleInfoSet inserted = ruleInfoHandler.insertRI(RuleInfo.createRuleInfo(report));
            if(inserted != null && !inserted.isEmpty()){
                rootRuleInfos.addRootRuleInfo(inserted);
                RuleInfoSet[] mayInfer = mayInfer();
                createInferenceReports(mayInfer);
            }
        } catch (InvalidRuleInfoException e){
            System.out.println("Inserting RI failed");
        }

    }

    public abstract RuleInfoSet[] mayInfer();

    public void createInferenceReports(RuleInfoSet[] inferrable) {
        HashMap<RuleInfo, Report> reports = new HashMap<>();
         for (int i = 0; i < inferrable.length; i++) {
             for(RuleInfo ri : inferrable[i]) {
                 rootRuleInfos.removeRuleInfo(ri);
                 ri.removeNullSubs();
                 Support supports = new Support(-1);   // probably wrong (maybe should make new support of the flag nodes and rule node
                 supports.addNode(this, ri.getAttitude());
                 if(this.isOpen()){
                     Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                             ri.getAttitude());
                     knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                     for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                         Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                         boolean compatibilityCheck = currentKISubs.compatible(onlyRelevantSubs(ri.getSubs()));
                         boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                 ri.getContext(),
                                 ri.getAttitude());
                         if (compatibilityCheck && supportCheck) {
                                supports.union(currentKnownInstance.getSupports());
                         }
                     }
                 }
                 Report newReport = new Report(ri.getSubs() == null ? new Substitutions() : ri.getSubs(), supports, ri.getAttitude(),
                         (i == 0), InferenceType.FORWARD, null, this);
                 newReport.setContextName(ri.getContext());
                 newReport.setReportType(ReportType.RuleCons);
                 reports.put(ri, newReport);
             }
         }
        sendInferenceReports(reports);
    }

    public void sendResponseToArgs(HashMap<RuleInfo, Report> reports, NodeSet arg) {
        for(RuleInfo ri : reports.keySet()) {
            Report report = reports.get(ri);
            NodeSet filteredArgs = new NodeSet();
            for(Node node : arg) {
                if(!ri.getFns().containsNode(node)) {
                    filteredArgs.add(node);
                }
            }
            this.sendReportToConsequents(filteredArgs, report);
        }
    }

    public void sendInferenceToCq(HashMap<RuleInfo, Report> reports, NodeSet cq) {
        for(Report report : reports.values()) {
            this.sendReportToConsequents(cq, report);
        }
    }

    public abstract void sendInferenceReports(HashMap<RuleInfo, Report> reports);

    /***
     * this method gets all the consequents and arguments that this node is a rule
     * to
     * 
     * @return nodeSet
     */

    public NodeSet getDownConsNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable consequentCable = this.getDownCableSet().get("cq");
        DownCable argsCable = this.getDownCableSet().get("arg");
        if (argsCable != null) {
            argsCable.getNodeSet().addAllTo(ret);
        }
        if (consequentCable != null) {
            consequentCable.getNodeSet().addAllTo(ret);
        }
        return ret;
    }

    /***
     * This method loops on the set of free variables of curRNode and checks if the
     * given substitutions contain bindings for its free variables. Finally, it
     * returns only
     * the relevant substitutions with the free variables’ bindings.
     * 
     * @param filterSubs
     * @return substitutions
     */

    public Substitutions onlyRelevantSubs(Substitutions filterSubs) {
        NodeSet freeVariablesSet = this.getFreeVariables();
        Substitutions relevantSubs = new Substitutions();
        for (Node variableNode : freeVariablesSet.getValues()) {
            for (Node var : filterSubs.getMap().keySet()) {
                Node value = filterSubs.getMap().get(var);
                if (var.getName().equals(variableNode.getName())) {
                    relevantSubs.add(var, value);
                }
            }
        }
        return relevantSubs;
    }

    /***
     * Method comparing opened outgoing channels over each node of the
     * nodeSet whether a more generic request of the specified channel was
     * previously sent in order not to re-send redundant requests -- ruleType gets
     * applied on Andor or Thresh part.
     * 
     * @param nodes
     * @param currentRequest
     * @param toBeCompared
     * @param ruleType
     * @return nodeSet
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

    /***
     * Method to request antecedents that did not receive a similar request before
     * 
     * @param currentRequest
     * @param knownInstance
     * @return
     */
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

    /***
     * Method to request antecedents that did not receive a similar request before
     * 
     * @param currentRequest
     * @return
     */
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
     * Method to request whens that did not receive a similar request before
     * 
     * @param currentRequest
     * @return
     */
    protected void requestWhensNotAlreadyWorkingOn(Request currentRequest) {
        Substitutions filterRuleSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchRuleSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        String currentContext = currentRequest.getChannel().getContextName();
        int currentAttitude = currentRequest.getChannel().getAttitudeID();

        boolean ruleType = this instanceof Thresh || this instanceof AndOr;

        NodeSet whenNodeSet = getDownWhenNodeSet(currentAttitude);
        NodeSet remainingWhenNodeSet = removeAlreadyEstablishedChannels(whenNodeSet,
                currentRequest,
                filterRuleSubs, ruleType);
        sendRequestsToNodeSet(remainingWhenNodeSet, filterRuleSubs, switchRuleSubs, currentContext,
                currentAttitude,
                ChannelType.WhenRule, this);

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
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     * @return
     * @throws NoSuchTypeException
     * @throws DirectCycleException
     */
    protected void processSingleRequests(Request currentRequest) throws DirectCycleException, NoSuchTypeException {
        System.out.println(this.getName() + " Processing Requests as a Rule node");
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
                    System.out.println("I am supported");
                    if (currentChannel instanceof IfToRuleChannel) {
                        if (this instanceof DoIfNode) {
                            ((DoIfNode) this).applyDoIfHandler(filterRuleSubs, currentRequest, null);

                        }
                    } else {
                        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                        NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                        NodeSet antArgNodesToConsiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                                currentRequest,
                                filterRuleSubs, ruleType);
                        sendRequestsToNodeSet(antArgNodesToConsiderClose, filterRuleSubs, switchRuleSubs,
                                currentContext,
                                currentAttitude,
                                ChannelType.AntRule, this);
                    }

                } else
                    super.processSingleRequests(currentRequest);

            } else {
                if (currentChannel.getChannelType() == ChannelType.IfRule) {
                    if (this instanceof DoIfNode) {
                        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                        if (knownInstances.getPositiveKInstances().containsKey(currentAttitude)) {
                            Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                                    .get(currentAttitude).values();
                            for (KnownInstance currentKIPve : collectionOfSetsPve) {
                                theKnownInstanceSet.add(currentKIPve);
                            }

                        }
                        knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                        Boolean notBound = isOpenNodeNotBound(filterRuleSubs);
                        boolean flag = false;
                        for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                            Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                            Substitutions onlySubsBindFreeVar = onlyRelevantSubs(filterRuleSubs);
                            boolean compatibilityCheck = onlySubsBindFreeVar
                                    .compatible(currentKISubs);
                            boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                    currentContext,
                                    currentAttitude);
                            if (compatibilityCheck && supportCheck) {
                                flag = true;
                                if (notBound) {
                                    ((DoIfNode) this).applyDoIfHandler(
                                            Substitutions.union(currentKISubs, filterRuleSubs),
                                            currentRequest, currentKnownInstance.getSupports());
                                } else {
                                    ((DoIfNode) this).applyDoIfHandler(filterRuleSubs, currentRequest,
                                            currentKnownInstance.getSupports());
                                }
                            }
                        }
                        if (!flag) {
                            super.processSingleRequests(currentRequest);
                        }
                    }
                } else {
                    boolean isNotBound = isOpenNodeNotBound(filterRuleSubs);
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentChannel.getAttitudeID());
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(filterRuleSubs);
                        boolean compatibilityCheck = onlySubsBindFreeVar
                                .compatible(currentKISubs);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentContext,
                                currentAttitude);
                        if (compatibilityCheck && supportCheck) {
                            if (!isNotBound) {
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                                return;
                            } else
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentKnownInstance);
                            return;
                        }

                }
                super.processSingleRequests(currentRequest);

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
     * report handling in Rule proposition nodes.
     * 
     * @param currentReport
     * @return
     * @throws DirectCycleException
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException, DirectCycleException {
        System.out.println(this.getName() + " Processing Reports as a Rule node");
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
                        // applyRuleHandler(currentReport, this);
                        // i removed the apply rule handler here because i call it only when the
                        // antecedents report back replying to my request thus whenever its a backward
                        // inference

                    } else {
                        super.processSingleRequests(tempRequest);
                    }
                } else {

                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentReportAttitudeID);
                    knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean compatibilityCheck = currentKISubs
                                .compatible(onlySubsBindFreeVar);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (compatibilityCheck && supportCheck) {

                            if (notBound) {
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);
                                    return;
                                }
                            } else {
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                                    return;

                                }
                            }

                        }
                    }
                    // applyRuleHandler(currentReport, this);
                    // i removed the apply rule handler here because i call it only when the
                    // antecedents report back replying to my request thus whenever its a backward
                    // inference
                    if (!this.isForwardReport()) {
                        this.setForwardReport(true);
                        super.processSingleRequests(tempRequest);

                    }
                }
            } else {
                /** Backward Inference */
                applyRuleHandler(currentReport);

            }
        } else if (this instanceof WhenDoNode) {

            if (forwardReportType) {
                if (!this.isOpen()) {
                    if (!supported(currentReportContextName, 0)
                            && currentReport.getReportType() == ReportType.WhenRule) {
                        // The rule is not asserted and the "when" part is asserted with forward
                        // inference
                        if (this.isForwardReport() == false) {
                            this.setForwardReport(true);
                        }
                        Substitutions switchSubs = new Substitutions();

                        Channel tempChannel = new Channel(switchSubs, currentReportSubs, currentReportContextName,
                                0, this);
                        Request tempRequest = new Request(tempChannel, null);
                        super.processSingleRequests(tempRequest);
                    } else {
                        if (supported(currentReportContextName, 0)) {

                            if (currentReport.getReportType() == ReportType.WhenRule) {

                                // The rule is already asserted and the "when" part is asserted with forward
                                // inference.
                                currentReport.getSupport().addNode(this,0);
                                ((WhenDoNode) this).applyRuleHandler(currentReport);
                            } else {
                                // The "when" part is already asserted and the rule is asserted with forward
                                // inference.
                                // The "when" part is not asserted and the rule is asserted with forward
                                // inference.

                                NodeSet whenNodes = getDownWhenNodeSet(currentReportAttitudeID);
                                if (this.isForwardReport() == false) {
                                    this.setForwardReport(true);
                                }
                                sendRequestsToNodeSet(whenNodes, currentReportSubs, null, currentReportContextName,
                                        currentReportAttitudeID,
                                        ChannelType.WhenRule, this);

                            }
                        }
                    }
                } else {

                    Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                    if (knownInstances.getPositiveKInstances().containsKey(currentReportAttitudeID)) {
                        Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                                .get(currentReportAttitudeID).values();
                        for (KnownInstance currentKIPve : collectionOfSetsPve) {
                            theKnownInstanceSet.add(currentKIPve);
                        }

                    }
                    knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                        boolean compatibilityCheck = currentKISubs
                                .compatible(onlySubsBindFreeVar);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (compatibilityCheck && supportCheck) {

                            if (notBound) {
                                currentReport.setSubstitutions(Substitutions.union(currentReportSubs,
                                        currentKnownInstance.getSubstitutions()));
                            }
                            if (currentReport.getSupport() != null) {
                                currentReport.setSupport(
                                        currentReport.getSupport().union(currentKnownInstance.getSupports()));
                            }

                            if (currentReport.getReportType() == ReportType.WhenRule) {
                                applyRuleHandler(currentReport);
                            } else {
                                NodeSet whenNodes = getDownWhenNodeSet(currentReportAttitudeID);
                                if (this.isForwardReport() == false) {
                                    this.setForwardReport(true);
                                }
                                sendRequestsToNodeSet(whenNodes, currentReportSubs, null, currentReportContextName,
                                        currentReportAttitudeID,
                                        ChannelType.WhenRule, this);
                            }
                        }
                    }
                }
            } else if (isForwardReport()) {
                System.out.println(currentReport.getReportType());
                if (currentReport.getReportType() == ReportType.WhenRule) {
                    System.out.println("Hey");
                    if (!this.isOpen()) {
                        if (currentReport.isSign()) {
                            // backwardInference of when part
                            currentReport.getSupport().addNode(this,0);
                            ((WhenDoNode) this).applyRuleHandler(currentReport);
                        }
                    } else {
                        Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                        if (knownInstances.getPositiveKInstances().containsKey(currentReportAttitudeID)) {
                            Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                                    .get(currentReportAttitudeID).values();
                            for (KnownInstance currentKIPve : collectionOfSetsPve) {
                                theKnownInstanceSet.add(currentKIPve);
                            }

                        }
                        knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                        Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                        for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                            Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                            boolean compatibilityCheck = currentKISubs
                                    .compatible(onlySubsBindFreeVar);
                            boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                    currentReportContextName,
                                    currentReportAttitudeID);

                            if (compatibilityCheck && supportCheck) {
                                if (notBound) {
                                    currentReport.setSubstitutions(Substitutions.union(currentReportSubs,
                                            currentKnownInstance.getSubstitutions()));
                                }
                                if (currentReport.getSupport() != null) {
                                    currentReport.setSupport(
                                            currentReport.getSupport().union(currentKnownInstance.getSupports()));
                                }
                                this.applyRuleHandler(currentReport);
                            }
                        }

                    }
                } else {
                    // backwardInference of when part
                    if (supported(currentReportContextName, 0)) {
                        currentReport.getSupport().addNode(this,0);
                        ((WhenDoNode) this).applyRuleHandler(currentReport);
                    }
                }
            } else {
                super.processSingleReports(currentReport);
            }

        } else {
            Substitutions switchSubs = new Substitutions();

            Channel tempChannel = new Channel(switchSubs, currentReportSubs, currentReportContextName,
                    currentReportAttitudeID, currentReport.getRequesterNode());
            Request tempRequest = new Request(tempChannel, null);
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

                }

                NodeSet argAntNodes = getDownAntArgNodeSet();
                boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                NodeSet remainingArgAntNodes = removeAlreadyEstablishedChannels(argAntNodes,
                        tempRequest,
                        currentReportSubs, ruleType);

                for (Channel outConsChannel : outgoingRuleConsChannels) {
                    Substitutions outConsChannelSubs = outConsChannel.getFilterSubstitutions();
                    Substitutions onlySubsBindFreeVarChnl = onlyRelevantSubs(outConsChannelSubs);
                    boolean compatibilityCheck = onlySubsBindFreeVar
                            .compatible(onlySubsBindFreeVarChnl);

                    if (compatibilityCheck) {
                        Substitutions unionSubs = Substitutions.union(currentReportSubs, outConsChannelSubs);
                        sendRequestsToNodeSet(remainingArgAntNodes, unionSubs, switchSubs, currentReportContextName,
                                currentReportAttitudeID, ChannelType.AntRule, this);
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

    // method for any of the children rules to call whenever it needs to act as a
    // normal proposition node
    public void grandparentMethodRequest(Request currentRequest) {
//        super.processSingleRequests(currentRequest);
        //TODO: sara, changed by wael to merge supports

    }

    public void grandparentMethodReport(Report currentReport) throws NoSuchTypeException {
//        super.processSingleReports(currentReport);
        //TODO: sara, changed by wael to merge supports

    }

    public boolean isForwardReport() {
        return forwardReport;
    }

    public void setForwardReport(boolean forwardReport) {
        this.forwardReport = forwardReport;
    }

    public RuleInfoSet getRootRuleInfos() {
        return rootRuleInfos;
    }

    public void setRootRuleInfos(RuleInfoSet rootRuleInfos) {
        this.rootRuleInfos = rootRuleInfos;
    }

    public RuleInfoHandler getRuleInfoHandler() {
        return ruleInfoHandler;
    }

}
