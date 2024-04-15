package nodes;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import mgip.KnownInstance;
import mgip.InferenceType;
import mgip.Report;
import mgip.Scheduler;
import mgip.requests.AntecedentToRuleChannel;
import mgip.requests.Channel;
import mgip.requests.ChannelSet;
import mgip.requests.ChannelType;
import mgip.requests.IntroductionChannel;
import mgip.requests.MatchChannel;
import mgip.ReportType;
import mgip.requests.Request;
import mgip.ruleIntroduction.MCII;
import mgip.ruleIntroduction.RII;
import mgip.rules.AndOr;
import mgip.rules.Thresh;
import network.Network;
import set.CombinationSet;
import set.ContextSet;
import set.MCIISet;
import set.NodeSet;
import set.CombinationSet;
import set.RIISet;
import set.Set;
import support.Support;
import cables.DownCable;
import cables.DownCableSet;
import components.Substitutions;
import context.Context;
import exceptions.NoSuchTypeException;

public class RuleNode extends PropositionNode {
    private boolean forwardReport;

    public RuleNode(String name, Boolean isVariable) {
        super(name, isVariable);
        this.forwardReport = false;
        // TODO Auto-generated constructor stub
    }

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);
        this.name = "P" + (Network.MolecularCount);
        this.forwardReport = false;

        // TODO Auto-generated constructor stub
    }

    public void applyRuleHandler(Report report, RuleNode ruleNode) {
        // if (this.isForwardReport() == true) {
        // this.setForwardReport(false);
        // report.setInferenceType(InferenceType.FORWARD);
        // NodeSet downCons = getDownConsNodeSet();
        // sendReportToCons(downCons, report);

        // } else {
        // for (Channel outChnl : outgoingChannels)
        // sendReport(report, outChnl);
        // }

        // TODO Ossama

    }

    /***
     * this method gets all the consequents and arguments that this node is a rule
     * to
     * 
     * @return nodeSet
     */

    public NodeSet getDownConsNodeSet() {
        NodeSet ret = new NodeSet();
        DownCable consequentCable = this.getDownCableSet().get("consequent");
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
    
    public boolean processIntroductionRequest(Request currentRequest) {
        //need to check if we already made an introduction request to the same node before
        String currContextName = currentRequest.getChannel().getContextName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();

        if (this instanceof AND || this instanceof NOR){
            IntroductionChannel intiatedChannel = intiateIntroChannel(currentRequest.getChannel(), this.getDownConsNodeSet(), null);
            IntroductionChannel intiatedChannel1 = establishChannel(ChannelType.Introduction, this, switchSubs, filterSubs, currContextName, attitude, -1, this)
            if (this.getDownConsNodeSet().size() > 0){
                for (Node consNode : this.getDownConsNodeSet()){
                    if (consNode instanceof RuleNode){
                        ((RuleNode) consNode).processIntroductionRequest(new Request(intiatedChannel, this));
                    }
                }
            }
        }
        else if (this instanceof AndOr || this instanceof Thresh){
            NodeSet args = this.getDownAntArgNodeSet();
            Context newContext = new Context(currContextName,attitude,args);
            RII rii = new RII(currentRequest, null, args , newContext , attitude);
            RIISet.addRII(rii);
            sendRequestsToNodeSet(args, currentRequest,newContext);//installing introduction channels and sending requests
        }
        else if(this instanceof AndEntail){
         NodeSet ants = this.getDownAntArgNodeSet();
         for(Node ant : ants) {
            ant = ant.applySubstitution(currentRequest.getChannel().getFilterSubstitutions());
            if(isFree(ant)){
                return false;
            }
         }  
         NodeSet cons = this.getDownConsNodeSet();
         Context newContext = new Context(currContextName,attitude,ants);//clone of the current context in addition to the assumed antecedents
         RII rii = new RII(currentRequest, ants, cons , newContext , attitude);
         RIISet.addRII(rii);
         sendRequestsToNodeSet(cons, currentRequest, newContext);//installing introduction channels and sending requests
        }
        else if(this instanceof OrEntail || this instanceof NumEntail){
            NodeSet ants = this.getDownAntArgNodeSet();
            NodeSet cons = this.getDownConsNodeSet();
            MCII mcii = new MCII(); //creates a new empty MCII
            for(Node ant : ants) {
                ant = ant.applySubstitution(currentRequest.getChannel().getFilterSubstitutions());
                if(isFree(ant)){
                    return false;
                }
            }
            if (this instanceof NumEntail){
                int i = this.getMin(); //Gets value of i from the NumEntail node
                List<NodeSet> combinations = CombinationSet.generateAntecedentCombinations(ants, i);
                for (NodeSet combination : combinations) {
                    Context newContext = new Context(currContextName, attitude,combination);
                    RII rii = new RII(currentRequest, combination, cons , newContext , attitude);
                    RIISet.addRII(rii);
                    mcii.add(rii);
                    this.sendRequestsToNodeSet(cons, filterSubs, switchSubs, newContext.getContextName(), attitude, ChannelType.RuleCons, this);
                }
            }
            else{//It's an OrEntail node
            for(Node ant : ants) {
                Context newContext = new Context(currContextName,attitude, ant);
                RII rii = new RII(currentRequest, ants, cons , newContext , attitude);
                RIISet riiSet = new RIISet();
                riiSet.addRII(rii);
                mcii.add(riiSet);
                sendRequestToNodeSet(cons, currentRequest, newContext); //send requests to all consequents
                }  
            }
           }  
    }

    public static void processIntroductionReport()
    {
        
    }

    public static boolean filterSupports(Report rep, RII rii)
    {
        if(rep.getSupport().isSubset(rii.getContext()) && rii.getContext().checkNodesPresent(rep.getSupport())){  //get context proposition nodes
            return true;
        }
        else{
            return false;
        }
    }
    public static void introductionHandler(RII rii)
    {
        if(this instanceof AndOr)
        {
            if( (rii.getPosCount() >= min) || (rii.getNegCount() > rii.getConqArgNodes().size() - max)){
                rii.setSufficent();
                Support sup = combineSupport(rii);
                //Build an instance of the rule using the substitutions found in the original request.
                //send a report declaring this instance in the context of the original request having the support Sup.
                
        }
    }

    public static Support combineSupport(RII rii)
    { //Return Sup of the rule instance
        Support sup = new Support();
        for(rep : rii.getReportSet())
        {
            sup = sup.union(rep.getSupport());
        }
        sup = sup - rii.getAntNodes();
        return sup;
    }

    public static Support combineSupport(MCII mcii)
    { //Return Sup of the rule instance
        Support sup = new Support();
        for(RII rii : mcii.getRII())
        {
            sup = sup.union(combineSupport(rii));
        }
        return sup;
    }

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     * @return
     */
    protected void processSingleRequests(Request currentRequest) {
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
     */
    protected void processSingleReports(Report currentReport) throws NoSuchTypeException {
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
                        if (this.isForwardReport() == false) {
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
                                if (this.isForwardReport() == false) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);
                                    return;
                                }
                            } else {
                                if (this.isForwardReport() == false) {
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
                    if (this.isForwardReport() == false) {
                        this.setForwardReport(true);
                        super.processSingleRequests(tempRequest);

                    }
                }
            } else {
                /** Backward Inference */
                applyRuleHandler(currentReport, this);

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

}
