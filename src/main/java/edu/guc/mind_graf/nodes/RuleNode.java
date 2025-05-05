package edu.guc.mind_graf.nodes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.caseFrames.Adjustability;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.KnownInstanceSet;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.reports.ReportType;
import edu.guc.mind_graf.mgip.requests.*;
import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
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
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;
import edu.guc.mind_graf.mgip.requests.IntroductionChannel;
public abstract class RuleNode extends PropositionNode {

    private boolean forwardReport;
    protected RuleInfoHandler ruleInfoHandler;
    protected RuleInfoSet rootRuleInfos;
    public MCII mcii = new MCII();
    protected boolean introduced = false;

    public RuleNode(String name, Boolean isVariable) {
        super(name, isVariable);
        this.forwardReport = false;
        // TODO Auto-generated constructor stub
    }

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);
        this.name = "P" + (Network.MolecularCount);
        this.forwardReport = false;
        rootRuleInfos = new RuleInfoSet();
    }

    public void applyRuleHandler(Report report) throws NoSuchTypeException {
        System.out.println("applyRuleHandler called on the report: " + report.stringifyReport());
        try {
            RuleInfoSet inserted = ruleInfoHandler.insertRI(RuleInfo.createRuleInfo(report));
            if (inserted != null && !inserted.isEmpty()) {
                rootRuleInfos.addRootRuleInfo(inserted);
                RuleInfoSet[] mayInfer = mayInfer();
                createInferenceReports(mayInfer);
            }
            else{
                System.out.println("Nothing can be inferred yet");
            }
        } catch (InvalidRuleInfoException e) {
            System.out.println("Inserting RI failed");
        } catch (DirectCycleException e) {
            System.out.println("Direct Cycle Exception");
        }

    }

    public abstract RuleInfoSet[] mayInfer();

    public void createInferenceReports(RuleInfoSet[] inferrable) throws DirectCycleException, NoSuchTypeException {
        HashMap<RuleInfo, Report> reports = new HashMap<>();
        for (int i = 0; i < inferrable.length; i++) {
            for (RuleInfo ri : inferrable[i]) {
                rootRuleInfos.removeRuleInfo(ri);
                ri.removeNullSubs();
                Report newReport = new Report(ri.getSubs() == null ? new Substitutions() : ri.getSubs(), createSupport(ri),
                        ri.getAttitude(),
                        (i == 0), (forwardReport ? InferenceType.FORWARD : InferenceType.BACKWARD), null, this);
                newReport.setContextName(ri.getContext());
                newReport.setReportType(ReportType.RuleCons);
                reports.put(ri, newReport);
            }
        }
        sendInferenceReports(reports);
    }

    public Support createSupport(RuleInfo ri) throws NoSuchTypeException, DirectCycleException {
        System.out.println("Creating the inference support");
        PropositionNodeSet supportPropSet = new PropositionNodeSet();
        for(FlagNode fn : ri.getFns()){
            Node n = fn.getNode();
            if(n.isOpen()){
                supportPropSet.add(n.applySubstitution(n.onlyRelevantSubs(ri.getSubs())));
            } else {
                supportPropSet.add(n);
            }
        }
        if(!this.isOpen()){
            supportPropSet.add(this);
        } else {
            supportPropSet.add(this.applySubstitution(ri.getSubs()));
        }
        HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> justSupport = new HashMap<>();
        justSupport.put(ri.getAttitude(), new Pair(supportPropSet, new PropositionNodeSet()));
        Support reportSup = new Support(-1, ri.getAttitude(), Network.currentLevel, justSupport, new PropositionNodeSet());
        return reportSup;
    }

    public void sendResponseToArgs(HashMap<RuleInfo, Report> reports, NodeSet arg) throws NoSuchTypeException {
        for (RuleInfo ri : reports.keySet()) {
            Report report = reports.get(ri);
            NodeSet filteredArgs = new NodeSet();
            for (Node node : arg) {
                if (!ri.getFns().containsNode(node)) {
                    filteredArgs.add(node);
                    System.out.println("Inferred " + node.applySubstitution(report.getSubstitutions()));
                }
            }
            this.sendReportToConsequents(filteredArgs, report);
        }
    }

    public void sendInferenceToCq(HashMap<RuleInfo, Report> reports, NodeSet cq) throws NoSuchTypeException {
        for (Report report : reports.values()) {
            for(Node node : cq){
                System.out.println("Inferred " + node.applySubstitution(report.getSubstitutions()));
            }
            this.sendReportToConsequents(cq, report);
        }
    }

    public abstract void sendInferenceReports(HashMap<RuleInfo, Report> reports) throws DirectCycleException, NoSuchTypeException;

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

    public MCII getMcii() {
        return mcii;
    }

    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException {
        String currContextName = currentRequest.getChannel().getContextName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        System.out.println("Current Request: " + currentRequest + " Current Context: " + currContextName +
        " Attitude: " + attitude + " Filter Subs: " + filterSubs + " Switch Subs: " + switchSubs +
        " Requester Node: " + currentRequest.getChannel().getRequesterNode().getName()+"\n");
        if (this instanceof AndOr || this instanceof Thresh){
            System.out.println("In AndOr or Thresh Node");
            NodeSet args = this.getDownAntArgNodeSet();
            Context newContext = new Context(currContextName, ContextController.getAttitudes());
            System.out.println("New Context: " + newContext.getContextName()+ "Modified Attitude: " + attitude + "Assumed Args: " + args+"\n");
          IntroductionChannel initiatedChannel = initiateIntroChannel(currentRequest.getChannel(), this, newContext.getContextName());

            Report reportRII1 = new Report(new Substitutions(), new Support(-1, 0, 0, new HashMap<>(), new PropositionNodeSet()), 1, true, InferenceType.INTRO, this, null);
            System.out.println("Initiated Channel in Context: " + initiatedChannel.getContextName()+" Attitude: "+initiatedChannel.getAttitudeID()+" with Rule: "+initiatedChannel.getRequesterNode().getName()+" Filter Subs: "+initiatedChannel.getFilterSubstitutions()+" Switch Subs: "+initiatedChannel.getSwitcherSubstitutions() + " Requester Node: "+initiatedChannel.getRequesterNode().getName()+"\n");
            RII rii = new RII(currentRequest, null, args , newContext , attitude);
            System.out.println("RII of new Context: " + rii.getContext().getContextName()+" Attitude: "+rii.getAttitudeID()+" Request: "+rii.getRequest().getChannel().getRequesterNode().getName()+" Antecedents: "+rii.getAntNodes()+" Consequents: "+rii.getConqArgNodes()+"\n");
            mcii.addRII(rii);
            // sendRequestsToNodeSet(args, currentRequest,newContext);// sending requests
            System.out.println("Sent Requests to NodeSet: "+args+" in Context:"+newContext+" with current request: "+currentRequest+"\n");
            return true;
        }
        //Check if all the nodes are bound by a quantifier
        else if(this instanceof OrEntailment){
            System.out.println("In OrEntail or NumEntail Node");
            NodeSet ants = this.getDownAntArgNodeSet();
            System.out.println("Rule Ants"+ants);
            NodeSet cons = this.getDownConsNodeSet();
            System.out.println("Rule Cons"+cons);
            System.out.println("MCII at creation " +mcii );
            NodeSet subants = new NodeSet();
            for(Node ant : ants) {
                ant = ant.applySubstitution(currentRequest.getChannel().getFilterSubstitutions());
                subants.add(ant);
                System.out.println("Substituted Ant :"+ant);
            }
           }
           return false;
    }


    protected Context getContext(String currContextName) {
        return new Context(currContextName,ContextController.getAttitudes());
    }

    // public void sendRequestsToNodeSet(NodeSet nodeSet, Request currentRequest, Context newContext) {
    //     for (Node node : nodeSet) {
    //         // Create a new IntroductionChannel based on the current request's channel
    //         IntroductionChannel introChannel = initiateIntroChannel(currentRequest.getChannel(), this, newContext.getContextName());

    //         Request request = new Request(introChannel, this);
    //         System.out.println("Request to be sent to Node: "+node+" with new request: "+request+" in Context: "+introChannel.getContextName()+"\n");

    //         Scheduler.getLowQueue().add(request);
    //     }
    // }

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

        Context currContext = new Context("Original Context", ContextController.getAttitudes());

        System.out.println("DownCableSet ants: " + ruleNode.getDownCableSet().get("antecedent"));

        System.out.println("RuleNode: "+ruleNode.getName());
        System.out.println("RuleNode Type: "+ruleNode.getSyntacticType());
        System.out.println("NumEntail ? :" + (ruleNode instanceof NumEntailment));
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
        ((KnownInstanceSet) instances).printKnownInstances(instances.getPositiveKInstances(), instances.getNegativeKInstances());



        Substitutions sub = new Substitutions();
        sub.add(varant, A);

        System.out.println("Free Vars: "+ ruleNode.getFreeVariables().contains(varant));

        System.out.println("Substitution: "+sub);

        System.out.println("IsFree :" +varant.isFree(ruleNode));

        System.out.println(varant.applySubstitution(sub));

        boolean isO = ruleNode.isOpen();
        System.out.println("Is Open? :" + isO);


        // Test processIntroductionRequest
        Request currentRequest = new Request(new Channel(new Substitutions(), sub, currContext.getContextName(), 1, ruleNode), ruleNode);
        Support support = new Support(-1, 1, 0, new HashMap<>(), new PropositionNodeSet());

        Report reportRII1 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII2 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII3 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII4 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII5 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportRII6 = new Report(new Substitutions(), support, 1, true, InferenceType.INTRO, ruleNode, null);
        Report reportForward = new Report(new Substitutions(), support, 1, false, InferenceType.FORWARD, ruleNode, null);
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
            boolean res = ruleNode.processIntroductionRequest(currentRequest);
            ruleNode.processReports();
            // System.out.println("Processing Report in "+ rep.getContextName());
            System.out.println("Scheduler after reqs: " + S.getLowQueue());
            System.out.println("Scheduler report Q: " + S.getHighQueue());
        } catch (NoSuchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int processIntroReports(ArrayList<Report> introReps) {
        // Find matching RII based on report information (rule, context, etc.)
        int res = 0;
        System.out.println(mcii.getExpectedReportsCount());
        System.out.println(introReps.size());
        if (introReps.size() < mcii.getExpectedReportsCount()) {
            System.out.println("Not enough " + res);
            return res;
        } else {
            for (Report report : introReps) {
                RII rii = findMatchingRII(report);
                Substitutions riiFilterSubs = rii.getRequest().getChannel().getFilterSubstitutions();
                if (report.getSubstitutions().compatible(riiFilterSubs)
                        && report.getContext(report.getContextName()).isSubset(rii.getContext())
                        && !mcii.isSufficient()) {
                    if (filterSupport(rii, report)) {
                        System.out.println("Report to be added: " + report);
                        rii.update(report);
                        res = introductionHandler(rii);
                    }
                }
            }
            System.out.println("Result " + res);
            return res;
        }
    }

    protected int introductionHandler(RII rii) {
        return 1;
    }

    private boolean filterSupport(RII rii, Report report) {
        // // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'filterSupport'");
        return true;
    }

    protected RII findMatchingRII(Report report) {
        for (RII rii : mcii.getRIIList()) {
            System.out.println("RII's Request context Name: "+ rii.getContext().getContextName());
            System.out.println("Report's context Name: "+ report.getContextName());
            if (rii.getContext().getContextName().equals(report.getContextName()) && (rii.getAttitudeID() == report.getAttitude()) && (rii.isSufficient()== false)) {
                return rii;
            }
        }
        return null;
    }

    public static void processIntroductionReport(Report report)
    {
        Report reportHasTurn = Scheduler.getHighQueue().poll();
        if (reportHasTurn.getReportType() == ReportType.Introduction)
        try {
            //Procssing the report
        } catch (Exception e) {
        }
    }

    // public static boolean filterSupports(Report rep, RII rii)
    // {
    //     if(rep.getSupport().isSubset(rii.getContext()) && rii.getContext().checkNodesPresent(rep.getSupport())){  //get context proposition nodes
    //         return true;
    //     }
    //     else{
    //         return false;
    //     }
    // }

    public static Support combineSupport(RII rii)
    { //Return Sup of the rule instance
        Support sup = new Support(0);
        return sup;
    }

    public static Support combineSupport(MCII mcii)
    { //Return Sup of the rule instance
        return combineSupport(mcii.getRii(0));

    }

    public static IntroductionChannel initiateIntroChannel(Channel channel, RuleNode rule, String contextName) {
      IntroductionChannel introChannel = new IntroductionChannel(channel.getSwitcherSubstitutions(), channel.getFilterSubstitutions(), contextName, channel.getAttitudeID(), rule);
        return introChannel;
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
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel
         || currentChannel instanceof IfToRuleChannel || currentChannel instanceof Channel)
            super.processSingleRequests(currentRequest);

        else {
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();

            if (!this.isOpen()) {
                if (this.supported(currentContext, currentAttitude, 0)) {
                    System.out.println("I am supported");

                        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                        NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                        NodeSet antArgNodesToConsiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                                currentRequest,
                                filterRuleSubs, ruleType);
                        sendRequestsToNodeSet(antArgNodesToConsiderClose, filterRuleSubs, switchRuleSubs,
                                currentContext,
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


            }

        }

    }

    /***
     * Method for a certain node to process incoming reports
     * 
     * @return
     */
    ArrayList<Report> introReports = new ArrayList<Report>();
    public void processReports() {
        Report reportHasTurn = Scheduler.getHighQueue().poll();
        try {
            if(Scheduler.getHighQueue().isEmpty() && introReports.size() > 0)
                {
                    processIntroReports(introReports);
                }
            if (reportHasTurn.getInferenceType() == InferenceType.INTRO)
            {
                System.out.println("Adding Intro Report in " + reportHasTurn.getContextName());
                introReports.add(reportHasTurn);
                processReports();
            }
            else{
                System.out.println("Not Intro Report");
                processSingleReports(reportHasTurn);
                processReports();
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
     * @throws DirectCycleException
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException, DirectCycleException {
        System.out.println(this.getName() + " Processing Reports as a Rule node");
        String currentReportContextName = currentReport.getContextName();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;

        boolean assertedInContext = supported(currentReportContextName, currentReportAttitudeID, 0);
        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(currentReportSubs);

        if(currentReport.getReportType() == ReportType.Introduction){
            processIntroductionReport(currentReport);
        }

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
        }   else {
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
    public void grandparentMethodRequest(Request currentRequest) throws DirectCycleException, NoSuchTypeException {
        super.processSingleRequests(currentRequest);
    }

    public void grandparentMethodReport(Report currentReport) throws NoSuchTypeException, DirectCycleException {
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
