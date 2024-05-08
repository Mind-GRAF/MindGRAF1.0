package edu.guc.mind_graf.nodes;

import java.util.ArrayList;
import java.util.Collection;

import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.KnownInstanceSet;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.*;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.mgip.ruleIntroduction.MCII;
import edu.guc.mind_graf.mgip.ruleIntroduction.RII;
import edu.guc.mind_graf.mgip.rules.AndOr;
import edu.guc.mind_graf.mgip.rules.NumEntailment;
import edu.guc.mind_graf.mgip.rules.OrEntailment;
import edu.guc.mind_graf.mgip.rules.Thresh;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.relations.Relation;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.set.Set;
import edu.guc.mind_graf.support.Support;

public abstract class RuleNode extends PropositionNode {
    private boolean forwardReport;
    protected RuleInfoHandler ruleInfoHandler;
    protected RuleInfoSet rootRuleInfos;
    protected MCII mcii = new MCII();
    protected static int instanceCount = 0;
    protected boolean introduced = false;

//    public RuleNode(String name, Boolean isVariable) {
        //        super(name, isVariable);
        //        this.forwardReport = false;
        //        // TODO Auto-generated constructor stub
    //    }

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);
        this.name = "P" + (Network.MolecularCount);
        this.forwardReport = false;
        rootRuleInfos = new RuleInfoSet();
    }

    public void applyRuleHandler(Report report) {
        // if (this.isForwardReport() == true) {
        // this.setForwardReport(false);
        // report.setInferenceType(InferenceType.FORWARD);
        // NodeSet downCons = getDownConsNodeSet();
        // sendReportToCons(downCons, report);

        // } else {
        // for (Channel outChnl : outgoingChannels)
        // putInferenceReportOnQueue(report, outChnl);
        // }

        try{
            RuleInfoSet inserted = ruleInfoHandler.insertRI(RuleInfo.createRuleInfo(report));
            rootRuleInfos.addRootRuleInfo(inserted);
            if(inserted != null && inserted.size() > 0){
                RuleInfoSet[] mayInfer = mayInfer();
                sendInferenceReports(mayInfer, report.getAttitude());
            }
        } catch (Exception e){

        }

    }

    public abstract RuleInfoSet[] mayInfer();

    public void sendInferenceReports(RuleInfoSet[] inferrable, int attitude) {
         for (int i = 0; i < inferrable.length; i++) {
             for(RuleInfo ri : inferrable[i]) {
                 PropositionNodeSet supports = new PropositionNodeSet();   // probably wrong (maybe should make new support of the flag nodes and rule node
                 for (FlagNode fn : ri.getFns()) {
                     supports.add(fn.getNode());
                 }
                 supports.add(this);
                 Report newReport = new Report(ri.getSubs() == null ? new Substitutions() : ri.getSubs(), supports, attitude,
                         (i == 0), InferenceType.FORWARD, null, this);
                 putInferenceReportOnQueue(newReport);
             }
         }
    }

    public abstract void putInferenceReportOnQueue(Report report);

    /***
     * this method gets all the consequents and arguments that this node is a rule
     * to
     * 
     * @return nodeSet
     */

    public NodeSet getDownConsNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable consequentCable = this.getDownCableSet().get("cqs");
        DownCable argsCable = this.getDownCableSet().get("args");
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
        String currentContextName = currentChannel.getName();
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
        String currentContext = currentRequest.getChannel().getName();
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
    
    public abstract boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException;

    public static void main(String[] args) throws NoSuchTypeException {
        //Testing Begin Algo.
        Scheduler S = new Scheduler();
        S.initiate();
        System.out.println("Scheduler low Q: "+S.getLowQueue());

        Network N = new Network();
        
        // Create a new RuleNode
        Node A = new PropositionNode("A", false);
        Node B = new PropositionNode("B", false);
        Node antecedent1 = new PropositionNode("Antecedent 1", false);
        Node antecedent2 = new PropositionNode("Antecedent 2", false);
        Node antecedent3 = new PropositionNode("Antecedent 3", false);
        Node varant = new PropositionNode("VarAnt", true);
        Node X = new PropositionNode("X", true);
        Node consequent = new PropositionNode("Consequent", false);
        
        System.out.println("Antecedent 1: "+antecedent1.getId());
        System.out.println("Antecedent 2: "+antecedent2.getId());
        System.out.println("Antecedent 3: "+antecedent3.getId());
        System.out.println("VarAnt: "+varant.getId());
        System.out.println("Consequent: "+consequent.getId());

        // Add antecedents and consequent to the RuleNode
        Relation ant = new Relation("antecedent", "", Adjustability.NONE, 4);
        Relation consequentRelation = new Relation("consequent", "", Adjustability.EXPAND, 1);
        Relation quantifier = Network.createRelation("forall", "propositionnode",
        Adjustability.EXPAND, 2);

        NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant);
        // NodeSet ants = new NodeSet(antecedent1, antecedent2, antecedent3, varant, X);//With Free Varible no Sub "X"
        
        DownCable d1 = new DownCable(ant, ants);
        DownCable d2 = new DownCable(consequentRelation, new NodeSet(consequent));
        DownCable d3 = new DownCable(quantifier, ants);


        NumEntailment ruleNode = new NumEntailment(new DownCableSet(d1, d2, d3));

        Set<String,Integer> attitudes = new Set<String,Integer>();
        attitudes.add("Belief",1);
        attitudes.add("Fear",2);
        attitudes.add("Desire",3);

        Context currContext = new Context("Original Context", attitudes);

        System.out.println("DownCableSet ants: " + ruleNode.getDownCableSet().get("antecedent"));
        
        System.out.println("RuleNode: "+ruleNode.getName());
        System.out.println("RuleNode Type: "+ruleNode.getSyntacticType());
        System.out.println("NumEntailment ? :" + (ruleNode instanceof NumEntailment));
        System.out.println("Antecedents : "+ruleNode.getDownAntArgNodeSet());
        System.out.println("Consequent : "+ruleNode.getDownConsNodeSet());

        //Makes node an open Formula
        NodeSet fetchedFreeVar = ruleNode.fetchFreeVariables();
        fetchedFreeVar.add(varant);//Have Sub in Filter
        // fetchedFreeVar.add(X);//Doesn't have Sub in Filter
        System.out.println("Fetched free var" + fetchedFreeVar);
        
        KnownInstanceSet instances = ruleNode.getKnownInstances();

        System.out.println("KnownInstances: " + instances);
        //print the known instances
        instances.printKnownInstances(instances.getPositiveKInstances(), instances.getNegativeKInstances());
        
        

        Substitutions sub = new Substitutions();
        sub.add(varant, A);

        System.out.println("Free Vars: "+ ruleNode.getFreeVariables().contains(varant));

        System.out.println("Substitution: "+sub);

        System.out.println("IsFree :" +varant.isFree(ruleNode));

        System.out.println(varant.applySubstitution(sub));

        boolean isO = ruleNode.isOpen();
        System.out.println("Is Open? :" + isO);

        
        // Test processIntroductionRequest
        Request currentRequest = new Request(new Channel(new Substitutions(), sub, currContext.getName(), 1, ruleNode), ruleNode);
        Report reportRII1 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII2 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII3 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII4 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII5 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII6 = new Report(new Substitutions(), new PropositionNodeSet(B), 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportForward = new Report(new Substitutions(), new PropositionNodeSet(B), 1, false, InferenceType.FORWARD, null, null);
        reportRII1.setContextName("Context 1");
        reportRII2.setContextName("Context 2");
        reportRII3.setContextName("Context 3");
        reportForward.setContextName("Context 7");
        reportRII4.setContextName("Context 4");
        reportRII5.setContextName("Context 5");
        reportRII6.setContextName("Context 6");
        Scheduler.addToHighQueue(reportRII1);
        Scheduler.addToHighQueue(reportRII2);
        Scheduler.addToHighQueue(reportRII3);
        Scheduler.addToHighQueue(reportRII4);
        Scheduler.addToHighQueue(reportRII5);
        Scheduler.addToHighQueue(reportRII6);
        Scheduler.addToHighQueue(reportForward);
        System.out.println(Scheduler.getHighQueue());

        try {
            boolean req = ruleNode.processIntroductionRequest(currentRequest);
            ruleNode.processReports();
            // System.out.println("Processing Report in "+ rep.getName());
            System.out.println("Scheduler after reqs: " + S.getLowQueue());
            System.out.println("Scheduler report Q: " + S.getHighQueue());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ArrayList<Report> introReps = new ArrayList<Report>();

    private void beginEndAlgo(Report introReport) throws NoSuchTypeException {
        introReps.add(introReport);
        if(introReps.size() < mcii.getExpectedReportsCount()){
            if(Scheduler.getLowQueue().isEmpty() && Scheduler.getHighQueue().isEmpty()){
                processIntroductionReport(introReps);
            }
        }
        else processIntroductionReport(introReps);
    }
    public int processIntroductionReport(ArrayList<Report> introReps) throws NoSuchTypeException {
        // Find matching RII based on report information (rule, context, etc.)
        System.out.println("Began End Algo");
        int res = 0;
        System.out.println("Expected Reports count " + mcii.getExpectedReportsCount());
        System.out.println("Actual Report count " +introReps.size());
        if(introReps.size()< mcii.getExpectedReportsCount())
        {
            System.out.println("Not enough " + res);
            return res;
        }
        else{
            for(Report report : introReps)
            {
                System.out.println("Enough Reports");
                RII rii = findMatchingRII(report);
                Substitutions riiFilterSubs = rii.getRequest().getChannel().getFilterSubstitutions();
                if(report.getSubstitutions().compatible(riiFilterSubs)&&(report.getContext(report.getName()).isSubset(rii.getContext()))&& !mcii.isSufficient()){
                if(filterSupport(rii, report)){
                    System.out.println("Report to be added: "+report.stringifyReport());
                        rii.update(report);
                    System.out.println("New rii report count "+ rii.getReportSet() +
                            " with positive count "+ rii.getPosCount() + " and neg count "+ rii.getNegCount());
                        res =  introductionHandler(rii);
                    }
                }
            }
            System.out.println("Result " + res);
            return res;
        }
    }

    protected abstract int introductionHandler(RII rii) throws NoSuchTypeException;

    private boolean filterSupport(RII rii, Report report) {
        // // TODO Ahmed Mohsen Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'filterSupport'");
        return true;
    }

    protected RII findMatchingRII(Report report) {
        for (RII rii : mcii.getRIIList()) {
            System.out.println("RII's Request context Name: "+ rii.getContext().getName());
            System.out.println("Report's context Name: "+ report.getName());
            if (rii.getContext().getName().equals(report.getName()) && (rii.getAttitudeID() == report.getAttitude()) && (rii.isSufficient()== false)) {
                return rii;
            }
        }
        return null;
    }

    public static Support combineSupport(RII rii)
    { //Return Sup of the rule instance
        //TODO Ahmed Mohsen Auto-generated method stub
        Support sup = new Support(-1); // ID 1 FOR TESTING
        return sup;
    }

    public static Support combineSupport(MCII mcii)
    { //Return Sup of the rule instance
        return combineSupport(mcii.getRii(0));
    }

    protected static Context getContext(String name)
    {
        return new Context("Context " , 1, new NodeSet());
//        return ContextController.getContext(name);
    }


    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     * @return
     */
    protected void processSingleRequests(Request currentRequest) throws NoSuchTypeException {
        System.out.println(this.getName() + " Processing Requests as a Rule node");
        Channel currentChannel = currentRequest.getChannel();
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel)
            super.processSingleRequests(currentRequest);
        else if(currentChannel instanceof IntroductionChannel)
            processIntroductionRequest(currentRequest);
        else {
            String currentContext = currentChannel.getName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();

            if (!this.isOpen()) {
                if (this.supported(currentContext, currentAttitude)) {
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
                return;

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
            if (reportHasTurn.getInferenceType() == InferenceType.INTRO)
            {
                System.out.println("Adding Intro Report in " + reportHasTurn.getName());
                beginEndAlgo(reportHasTurn);
            }
            else{
                System.out.println("Not an Intro Report");
                processSingleReports(reportHasTurn);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /***
     * report handling in Rule proposition nodes.
     * 
     * @param currentReport
     * @return
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException {
        System.out.println(this.getName() + " Processing Reports as a Rule node");

        String currentReportContextName = currentReport.getName();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;

        boolean assertedInContext = supported(currentReportContextName, currentReportAttitudeID);
        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(currentReportSubs);
//
//        if(currentReport.getReportType() == ReportType.Introduction){
//            processIntroductionReport(currentReport);
//        }

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
    public void grandparentMethodRequest(Request currentRequest) throws NoSuchTypeException {
        super.processSingleRequests(currentRequest);
    }

    public void grandparentMethodReport(Report currentReport) throws NoSuchTypeException {
        super.processSingleReports(currentReport);
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
