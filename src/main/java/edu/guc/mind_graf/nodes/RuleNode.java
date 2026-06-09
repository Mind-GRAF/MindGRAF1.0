package edu.guc.mind_graf.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map; 

import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.InvalidRuleInfoException;
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
import edu.guc.mind_graf.mgip.requests.WhenToRuleChannel;
import edu.guc.mind_graf.mgip.ruleHandlers.FlagNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.mgip.rules.AndOr;
import edu.guc.mind_graf.mgip.rules.BridgeRule;
import edu.guc.mind_graf.mgip.rules.Thresh;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.acting.rules.DoIfNode;
import edu.guc.mind_graf.acting.rules.WhenDoNode;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;

public abstract class RuleNode extends PropositionNode {

    // chain might
    private boolean forwardReport;
    // change6.1 done
    private InferenceType currentInferenceType;
    protected RuleInfoHandler ruleInfoHandler;
    protected RuleInfoSet rootRuleInfos;

    public RuleNode(DownCableSet downCableSet) {
        super(downCableSet);
        this.name = "P" + (Network.MolecularCount);
        this.forwardReport = false;
        rootRuleInfos = new RuleInfoSet();
    }

    // 1. Wraps the report into a RuleInfo (scorecard)
    // 2. Gives it to ruleInfoHandler → handler updates the score
    // 3. If handler says "enough antecedents reported" → call mayInfer()

    public void applyRuleHandler(Report report) throws NoSuchTypeException {
        
        // change6.2 done
        this.currentInferenceType = report.getInferenceType();
        
        // original output:
        //System.out.println("applyRuleHandler called on the report: " + report.stringifyReport());
        // changed to:
        System.out.println(
            "\n[RULE HANDLER] " + this.getName()
            + "\n  action: insert antecedent report into rule handler/P-Tree"
            + "\n  reportType: " + report.getReportType()
            + "\n  inference: " + report.getInferenceType()
            + "\n  antecedent/reporter: " + (report.getReporterNode() == null ? "null" : report.getReporterNode().getName())
            + "\n  reportSubs: " + report.getSubstitutions()
        );
        try {
            // original output:
            //RuleInfoSet inserted = ruleInfoHandler.insertRI(RuleInfo.createRuleInfo(report));
            // changed to:
            // After the fix, when A(X) receives the matched report from A(john), 
            // A(X) creates a new forwarded report to the rule with itself as the reporter. 
            // So the RuleInfo stores fns = {A(X)/true} together with the substitution {X = john}. 
            RuleInfo ri = RuleInfo.createRuleInfo(report);

                System.out.println(
                    "\n[RULE INFO]"
                    + "\n  rule: " + this.getName()
                    + "\n  createdRI: " + ri
                    + "\n  riSubs: " + ri.getSubs()
                    + "\n  flagNodes: " + ri.getFns()
                );
            RuleInfoSet inserted = ruleInfoHandler.insertRI(ri);


            if (inserted != null && !inserted.isEmpty()) {
                        System.out.println(
                "\n[RULE INFO INSERTED]"
                + "\n  rule: " + this.getName()
                + "\n  insertedRootRIs: " + inserted
                + "\n  nextAction: run mayInfer()"
            );
                rootRuleInfos.addRootRuleInfo(inserted);
                RuleInfoSet[] mayInfer = mayInfer();
                createInferenceReports(mayInfer);
            }
            else{
                // changed to 
               // System.out.println("Nothing can be inferred yet");
                        System.out.println(
                "\n[RULE INFO WAITING]"
                + "\n  rule: " + this.getName()
                + "\n  reason: report inserted but not enough antecedents are satisfied yet"
            );
            }
        } catch (InvalidRuleInfoException e) {
            // System.out.println("Inserting RI failed");
            System.out.println(
            "[RULE INFO INSERT FAILED]"
            + "\n  rule: " + this.getName()
            + "\n  inputReporter: " + (report.getReporterNode() == null ? "null" : report.getReporterNode().getName())
            + "\n  inputSubs: " + report.getSubstitutions()
            + "\n  likelyReason: reporter node is not one of this rule's antecedents or substitutions are incompatible"
        );
        } catch (DirectCycleException e) {
            System.out.println("Direct Cycle Exception");
        }

    }

    // changes depeding on the rule type 
    public abstract RuleInfoSet[] mayInfer();



     
    // called after the rule has enough evidence to fire 
    // creates the report that goes from the rule to cons 
    public void createInferenceReports(RuleInfoSet[] inferrable) throws DirectCycleException, NoSuchTypeException {
        HashMap<RuleInfo, Report> reports = new HashMap<>();
        for (int i = 0; i < inferrable.length; i++) {
            for (RuleInfo ri : inferrable[i]) {
                rootRuleInfos.removeRuleInfo(ri);
                ri.removeNullSubs();
                // new add change 
                // change6.3 done
                InferenceType producedInference =
        currentInferenceType == null ? InferenceType.BACKWARD : currentInferenceType;
                //InferenceType producedInference = ri.getInferenceType();
                //forwardReport ? InferenceType.FORWARD : InferenceType.BACKWARD;

                System.out.println(
                    "\n[RULE FIRE] " + this.getName()
                    + "\n  meaning: rule has enough antecedent evidence to infer its consequent"
                    + "\n  ri: " + ri
                    + "\n  riSubs: " + ri.getSubs()
                    + "\n  producedReportType: RuleCons"
                    + "\n  producedInference: " + producedInference
                    + "\n  sign: " + (i == 0)
                );
                Report newReport = new Report(ri.getSubs() == null ? new Substitutions() : ri.getSubs(), createSupport(ri),
                        ri.getAttitude(),
                        (i == 0),producedInference, null, this);
                newReport.setContextName(ri.getContext());
                newReport.setReportType(ReportType.RuleCons);
                reports.put(ri, newReport);
            }
        }
        sendInferenceReports(reports);
    }


    // adding the support and ifs for if the rule is closed or if the rule is open
    // public Support createSupport(RuleInfo ri) throws NoSuchTypeException, DirectCycleException {
    //     // original output:
    //     // System.out.println("Creating the inference support");
    //     // changed to:
    //     System.out.println(
    //         "\n[SUPPORT] " + this.getName()
    //         + "\n  meaning: build justification for inferred conclusion"
    //         + "\n  riSubs: " + ri.getSubs()
    //         + "\n  satisfiedAntecedents: " + ri.getFns()
    //         + "\n  context: " + ri.getContext()
    //         + "\n  attitude: " + ri.getAttitude()
    //     );

    //     PropositionNodeSet supportPropSet = new PropositionNodeSet();
    //     for(FlagNode fn : ri.getFns()){
    //         Node n = fn.getNode();
    //         //a(x)
    //         if(n.isOpen()){
    //             // chain change 
    //             // supportPropSet.add(n.applySubstitution(n.onlyRelevantSubs(ri.getSubs())));
    //             // to 
    //             Substitutions relevant = n.onlyRelevantSubs(ri.getSubs());
    //            // if i have a sub apply it if i dont keep the original node 
    //            // like when sending the supports if it came from a(x) and x=john then support is A(john) but if it came from A(john) then support is A(john) without applying any sub
    //             if(relevant == null || relevant.isEmpty())
    //                   supportPropSet.add(n);
    //              else
    //                  supportPropSet.add(n.applySubstitution(relevant));
    //         //a(john)
    //         } else {
    //             // change chain (changed back because otherwise it only adds the rule as the support)
    //             supportPropSet.add(n);
    //             //to 
    //             //  if(ri.getSubs() == null || ri.getSubs().isEmpty())
    //             //     supportPropSet.add(this);
    //             //  else
    //             //     supportPropSet.add(this.applySubstitution(ri.getSubs()));
    //         }
    //     }
    //     if(!this.isOpen()){
    //         supportPropSet.add(this);
    //     } else {
    //         supportPropSet.add(this.applySubstitution(ri.getSubs()));
    //     }
    //     HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> justSupport = new HashMap<>();
    //     justSupport.put(ri.getAttitude(), new Pair(supportPropSet, new PropositionNodeSet()));
    //     Support reportSup = new Support(-1, ri.getAttitude(), Network.currentLevel, justSupport, new PropositionNodeSet());
    //     return reportSup;
    // }
    public Support createSupport(RuleInfo ri) throws NoSuchTypeException, DirectCycleException {
    System.out.println(
        "\n[SUPPORT] " + this.getName()
        + "\n  meaning: build justification for inferred conclusion"
        + "\n  riSubs: " + ri.getSubs()
        + "\n  satisfiedAntecedents: " + ri.getFns()
        + "\n  context: " + ri.getContext()
        + "\n  attitude: " + ri.getAttitude()
    );

    PropositionNodeSet supportPropSet = new PropositionNodeSet();

    /*
     * Add the actual rule node.
     *
     * Before, the code used this.applySubstitution(ri.getSubs()) for open rules.
     * That created a temporary substituted rule node such as P8, which was not
     * supported in the context. The support must point to the real asserted rule.
     */
    supportPropSet.add(this);

    /*
     * Add the actual supported antecedent facts.
     *
     * For an open antecedent such as Vehicle(X), the RuleInfo only stores the
     * pattern node M0 and the substitution X = Car1. If we call applySubstitution()
     * on M0, MindGRAF may create a temporary node such as Vehicletemp(Car1).
     *
     * Instead, we look inside M0's known instances. The known instance was created
     * when the real fact Vehicle(Car1) matched Vehicle(X). Its support already
     * points to the real supported node Vehicle(Car1).
     */
    for (FlagNode fn : ri.getFns()) {
        Node n = fn.getNode();

        if (n instanceof PropositionNode) {
            PropositionNode antecedentNode = (PropositionNode) n;

            if (antecedentNode.isOpen()) {
                Substitutions relevantSubs =
                        antecedentNode.onlyRelevantSubs(ri.getSubs());

                Collection<KnownInstance> knownInstances =
                        antecedentNode.getKnownInstances()
                                .getPositiveCollectionbyAttribute(ri.getAttitude());

                boolean foundRealSupport = false;

                if (knownInstances != null) {
                    for (KnownInstance ki : knownInstances) {
                        Substitutions kiSubs = ki.getSubstitutions();

                        boolean compatible =
                                relevantSubs == null
                                        || relevantSubs.isEmpty()
                                        || relevantSubs.compatible(kiSubs);

                        boolean supportedInContext =
                                ki.anySupportSupportedInAttitudeContext(
                                        ri.getContext(),
                                        ri.getAttitude()
                                );

                        if (compatible && supportedInContext) {
                            addOriginNodesFromSupport(
                                    supportPropSet,
                                    ki.getSupports(),
                                    Network.currentLevel,
                                    ri.getAttitude()
                            );

                            foundRealSupport = true;
                        }
                    }
                }

                /*
                 * Fallback only.
                 * This should rarely be used. It keeps old behavior available
                 * for cases where no known instance support can be found.
                 */
                if (!foundRealSupport) {
                    if (relevantSubs == null || relevantSubs.isEmpty()) {
                        supportPropSet.add(antecedentNode);
                    } else {
                        supportPropSet.add(antecedentNode.applySubstitution(relevantSubs));
                    }
                }

            } else {
                /*
                 * Closed antecedents are already real nodes, so they can be
                 * added directly.
                 */
                supportPropSet.add(antecedentNode);
            }
        }
    }

    HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> justSupport =
            new HashMap<>();

    justSupport.put(
            ri.getAttitude(),
            new Pair<>(supportPropSet, new PropositionNodeSet())
    );

    Support reportSup = new Support(
            -1,
            ri.getAttitude(),
            Network.currentLevel,
            justSupport,
            new PropositionNodeSet()
    );

    return reportSup;
}

    private void addOriginNodesFromSupport(
        PropositionNodeSet target,
        Support support,
        int level,
        int attitude
) {
    if (support == null) {
        return;
    }

    if (!support.getAssumptionSupport().containsKey(level)) {
        return;
    }

    if (!support.getAssumptionSupport().get(level).containsKey(attitude)) {
        return;
    }

    for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> supportEntry
            : support.getAssumptionSupport().get(level).get(attitude)) {

        for (Map.Entry<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> attitudeSupport
                : supportEntry.getFirst().entrySet()) {

            PropositionNodeSet originNodes =
                    attitudeSupport.getValue().getFirst();

            target.putAll(originNodes.getValues());
        }
    }
}

    public void sendResponseToArgs(HashMap<RuleInfo, Report> reports, NodeSet arg) throws NoSuchTypeException {
        for (RuleInfo ri : reports.keySet()) {
            Report report = reports.get(ri);
            NodeSet filteredArgs = new NodeSet();
            for (Node node : arg) {
                if (!ri.getFns().containsNode(node)) {
                    filteredArgs.add(node);
                    // original output:
                    // System.out.println("Inferred " + node.applySubstitution(report.getSubstitutions()));
                    // changed to:
                   System.out.println(
                        "\n[INFERRED ARGUMENT]"
                        + "\n  rule: " + this.getName()
                        + "\n  argumentPattern: " + node
                        + "\n  substitutions: " + report.getSubstitutions()
                        + "\n  inferredNode: " + node.applySubstitution(report.getSubstitutions())
                        + "\n  reportType: " + report.getReportType()
                        + "\n  inference: " + report.getInferenceType()
                    );
                }
            }
            this.sendReportToConsequents(filteredArgs, report);
        }
    }

    
    public void sendInferenceToCq(HashMap<RuleInfo, Report> reports, NodeSet cq) throws NoSuchTypeException {
        for (Report report : reports.values()) {
            for(Node node : cq){
                // System.out.println("Inferred " + node.applySubstitution(report.getSubstitutions()));
                System.out.println(
                    "[INFERRED CONSEQUENT]"
                    + "\n  rule: " + this.getName()
                    + "\n  consequentPattern: " + node
                    + "\n  substitutions: " + report.getSubstitutions()
                    + "\n  inferredNode: " + node.applySubstitution(report.getSubstitutions())
                    + "\n  reportType: " + report.getReportType()
                    + "\n  inference: " + report.getInferenceType()
                );
                

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

    /***
     * Request handling in Rule proposition nodes.
     * 
     * @param currentRequest
     * @return
     * @throws NoSuchTypeException
     * @throws DirectCycleException
     */
    protected void processSingleRequests(Request currentRequest) throws DirectCycleException, NoSuchTypeException {
        // original output:
        // System.out.println(this.getName() + " Processing Requests as a Rule node");
        // changed to:
        System.out.println(
            "\n[REQUEST][RuleNode] " + this.getName()
            + "\n  channelClass: " + currentRequest.getChannel().getClass().getSimpleName()
            + "\n  channelType: " + currentRequest.getChannel().getChannelType()
            + "\n  context: " + currentRequest.getChannel().getContextName()
            + "\n  attitude: " + currentRequest.getChannel().getAttitudeID()
            + "\n  requester: " + currentRequest.getChannel().getRequesterNode().getName()
            + "\n  filterSubs: " + currentRequest.getChannel().getFilterSubstitutions()
            + "\n  switchSubs: " + currentRequest.getChannel().getSwitcherSubstitutions()
        );
        
        // get the channel that brought the request
        Channel currentChannel = currentRequest.getChannel();

        // chain change
        //IF branch  = “treat the rule as a proposition”
        //ELSE branch = “use the rule to prove something”

        // AntecedentToRuleChannel: the ant is reporting, so dont act as an inference rule just yet 
        // MatchChannel: this rule node matched another node , so dont act as an inference rule just yet
        // IfToRuleChannel: the if is reporting, so dont act as an inference rule just yet
        // so removed the current instance of channel because then the if will always be true
        // and will always act as a normal prop node 
        //change3 done
        if (currentChannel instanceof AntecedentToRuleChannel || currentChannel instanceof MatchChannel
         || currentChannel instanceof IfToRuleChannel) //|| currentChannel instanceof Channel)
            super.processSingleRequests(currentRequest);


        // RulecCons channel: the cons is asking can u prove me please act as an inference rule and check if u can infer the cons with the current support and the new report
        else {
            String currentContext = currentChannel.getContextName();
            int currentAttitude = currentChannel.getAttitudeID();
            Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
            Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();

            // if the rule has no free var "A(john) ∧ B(john) → C(john)"
            if (!this.isOpen()) {
                // supported 
                if (this.supported(currentContext, currentAttitude, 0)) {
                    // System.out.println("I am supported");
                        // thresh and andor different in the asking the ant 
                        boolean ruleType = this instanceof Thresh || this instanceof AndOr;
                        // returs a(x), b(x)
                        NodeSet antArgCloseToMe = getDownAntArgNodeSet();
                        // bardo so no redundant req
                        NodeSet antArgNodesToConsiderClose = removeAlreadyEstablishedChannels(antArgCloseToMe,
                                currentRequest,
                                filterRuleSubs, ruleType);

                        // send req to ant CHANNEL TYPE ANT RULE
                        sendRequestsToNodeSet(antArgNodesToConsiderClose, filterRuleSubs, switchRuleSubs,
                                currentContext,
                                currentAttitude,
                                ChannelType.AntRule, this);
                    

                // not supported
                } else
                    // chain change (i changed it back)
                       // used to want to treat the rule as a normal prop and prove it first the rule 
                       // does a rule need to be proven ?
                    super.processSingleRequests(currentRequest);

                    // even if the rule isnt supported ask its antecedents
                   // requestAntecedentsNotAlreadyWorkingOn(currentRequest);

            // rule has free variables "A(x) ∧ B(x) → C(x)"
            } else {
                    // checks if the free var is not bounded , false then it is bounded 
                    boolean isNotBound = isOpenNodeNotBound(filterRuleSubs);
                    //x john x mary fa the rule already has it 
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentChannel.getAttitudeID());
                    // looping over each known instance 
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        // gets its sub x john y mary 
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                       // only extracts the relevant subs for the rule from the report subs x john
                        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(filterRuleSubs);
                        // check the compatibility
                        boolean compatibilityCheck = onlySubsBindFreeVar
                                .compatible(currentKISubs);
                        // and matches the context and attitude 
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentContext,
                                currentAttitude);

                        // matches and supported 
                        if (compatibilityCheck && supportCheck) {
                            // query C(john)? go ask the ants with the current known instance x john
                            if (!isNotBound) {
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                                return;

                            // query C?
                            // then use what the current the known instance returned 
                            } else
                                requestAntecedentsNotAlreadyWorkingOn(currentRequest, currentKnownInstance);
                            return;
                        }

                    }
                    //chain change
                    // "I checked all known instances of the rule, but none of them was both compatible and supported."
                   // super.processSingleRequests(currentRequest);
                    //requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                    //change4 done 
                   if (this.supported(currentContext, currentAttitude, 0)) {
                        requestAntecedentsNotAlreadyWorkingOn(currentRequest);
                   } else {
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
       // original output:
       // System.out.println(this.getName() + " Processing Reports as a Rule node");
       // changed to: 
       System.out.println(
            "\n[REPORT][RuleNode] " + this.getName()
            + "\n  reportType: " + currentReport.getReportType()
            + "\n  inference: " + currentReport.getInferenceType()
            + "\n  context: " + currentReport.getContextName()
            + "\n  attitude: " + currentReport.getAttitude()
            + "\n  requester: " + (currentReport.getRequesterNode() == null ? "null" : currentReport.getRequesterNode().getName())
            + "\n  reporter/antecedant: " + (currentReport.getReporterNode() == null ? "null" : currentReport.getReporterNode().getName())
            + "\n  reportSubs: " + currentReport.getSubstitutions()
            + "\n  sign: " + currentReport.isSign()
        );
       // extract the report info 
        String currentReportContextName = currentReport.getContextName();
        int currentReportAttitudeID = currentReport.getAttitude();
        Substitutions currentReportSubs = currentReport.getSubstitutions();
       // forward or backward 
        boolean forwardReportType = currentReport.getInferenceType() == InferenceType.FORWARD;
       // check if the rule itself is supported 
        boolean assertedInContext = supported(currentReportContextName, currentReportAttitudeID, 0);
       // only revelant subs for the rule so if it uses x only care about x/
        Substitutions onlySubsBindFreeVar = onlyRelevantSubs(currentReportSubs);
       // if the report came from an ant 
        if (currentReport.getReportType() == ReportType.AntRule) {
            // making the report into a request for the rule to ask other ant with the same sub 
            // a(john) so ask for other ants with john
            Channel tempChannel = new AntecedentToRuleChannel(null, currentReportSubs, currentReportContextName,
                    currentReportAttitudeID, currentReport.getRequesterNode());
            Request tempRequest = new Request(tempChannel, null);
            /** AntecedentToRule Channel */
            // A(john) was newly asserted forward.
            //Rule A(x) → C(x) receives A(john). 
            // for the report itself 
            if (forwardReportType) {
                // Forward chaining: rule has received a forward report from antecedent
                // Check if rule can now fire with available support
                // before it wasnt doing anything 
                //if (this.supported(currentReportContextName, currentReportAttitudeID, 0)) {
                //     this.setForwardReport(true);
                //     applyRuleHandler(currentReport);
                // } else {
                //     super.processSingleReports(currentReport);
                // }
            
                // if the report from ant to rule is backward 
                // ant is answering back 
                System.out.println( 
                    "\n[FORWARD ANT REPORT]"
                    + "\n  rule: " + this.getName()
                    + "\n  ruleSupported: " + assertedInContext
                    + "\n  reporterAntecedent: " + currentReport.getReporterNode().getName()
                    + "\n  substitutions: " + currentReport.getSubstitutions()
                );

                // change5 done 
                if (assertedInContext) {
                    this.setForwardReport(true);
                    applyRuleHandler(currentReport);
                } else {
                    System.out.println(
                        "\n[FORWARD ANT REPORT SKIPPED]"
                        + "\n  reason: rule is not supported in this context/attitude"
                    );

                    super.processSingleReports(currentReport);
                }

            } else {
                // Backward inference: rule received backward request from consequent
                // Original backward-request logic preserved below
                /** Forward Inference */ // wrong comment 
                // rule closed A(john) ∧ B(john) → C(john)
                if (!this.isOpen()) {
                    /** Close Type Implementation */
                    // if rule is supported 
                    if (assertedInContext) {
                        // A(john) answered. Now ask B(john), if B(john) was not already asked.
                        if (!this.isForwardReport()) {
                            this.setForwardReport(true); 
                            requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                        }

                        // change5b done 
                        applyRuleHandler(currentReport);
                    } else {
                        //chain change 
                        super.processSingleRequests(tempRequest);
                        // if (this.supported(currentReportContextName, currentReportAttitudeID, 0)) {
                        //        applyRuleHandler(currentReport);
                        // } else {
                        //         super.processSingleRequests(tempRequest);
                        
                        // }
                    }
                // if the rule is open A(x) ∧ B(x) → C(x)
                } else {
                    // checks the known instances of the rule 
                    Collection<KnownInstance> theKnownInstanceSet = knownInstances.mergeKInstancesBasedOnAtt(
                            currentReportAttitudeID);
                    knownInstances.printKnownInstanceSet(theKnownInstanceSet);
                    
                    Boolean notBound = isOpenNodeNotBound(currentReportSubs);
                    //looping over the known instances of the rule 
                    for (KnownInstance currentKnownInstance : theKnownInstanceSet) {
                        Substitutions currentKISubs = currentKnownInstance.getSubstitutions();
                       // comparing it with the report subs
                        boolean compatibilityCheck = currentKISubs
                                .compatible(onlySubsBindFreeVar);
                        boolean supportCheck = currentKnownInstance.anySupportSupportedInAttitudeContext(
                                currentReportContextName,
                                currentReportAttitudeID);
                        if (compatibilityCheck && supportCheck) {
                            // if the report doesnt have sub then ask for the ant with the subs of the known instances 
                            
                            if (notBound) {
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest, currentKnownInstance);
                                    return;
                                }
                            // if the report has sub then ask for the ant with the subs of the report
                            } else {
                                if (!this.isForwardReport()) {
                                    this.setForwardReport(true);
                                    requestAntecedentsNotAlreadyWorkingOn(tempRequest);
                                    return;

                                }
                            }

                        }
                    }
                    // change chain  to do is applyrulehandler 
                    // if (!this.isForwardReport()) {
                    //     this.setForwardReport(true);
                        //  super.processSingleRequests(tempRequest);
                        //change5b2 done 
                       if (this.supported(currentReportContextName, currentReportAttitudeID, 0)) {
                            applyRuleHandler(currentReport);
                       } else {
                             super.processSingleRequests(tempRequest);
                       }
                   // }
                }
            }
        // report is not an antrule
        } else {
            Substitutions switchSubs = new Substitutions();

            Channel tempChannel = new Channel(switchSubs, currentReportSubs, currentReportContextName,
                    currentReportAttitudeID, currentReport.getRequesterNode());
            // it creates a temporary request using the report substitutions.
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
 
            // backward + not antrule 
            // c(x) c(john)! and c(x) infers d(x)
              
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
