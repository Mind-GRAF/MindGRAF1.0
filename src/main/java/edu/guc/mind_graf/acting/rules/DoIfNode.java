package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.IfToRuleChannel;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.network.Network;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Pair;
import edu.guc.mind_graf.support.Support;
import edu.guc.mind_graf.nodes.PropositionNode;;

public class DoIfNode extends RuleNode {

    public DoIfNode(DownCableSet downCableSet) {
        super(downCableSet);
        // TODO Auto-generated constructor stub
    }

    protected void processSingleRequests(Request currentRequest) throws DirectCycleException, NoSuchTypeException {
        System.out.println(this.getName() + " Processing Requests as a DoIf node");
        Channel currentChannel = currentRequest.getChannel();
        String currentContext = currentChannel.getContextName();
        int currentAttitude = currentChannel.getAttitudeID();
        Substitutions filterRuleSubs = currentChannel.getFilterSubstitutions();
        Substitutions switchRuleSubs = currentChannel.getSwitcherSubstitutions();
        if (!this.isOpen()) {
            if (this.supported(currentContext, 0, 0)) {
                if (currentChannel instanceof IfToRuleChannel) {
                    this.applyDoIfHandler(filterRuleSubs, currentRequest, null);

                } else {
                    super.processSingleRequests(currentRequest);
                }
            }
        } else {
            if (currentChannel.getChannelType() == ChannelType.IfRule) {
                Collection<KnownInstance> theKnownInstanceSet = new ArrayList<KnownInstance>();

                if (knownInstances.getPositiveKInstances().containsKey(currentAttitude)) {
                    Collection<KnownInstance> collectionOfSetsPve = knownInstances.getPositiveKInstances()
                            .get(currentAttitude).values();
                    for (KnownInstance currentKIPve : collectionOfSetsPve) {
                        theKnownInstanceSet.add(currentKIPve);
                    }

                }
                // knownInstances.printKnownInstanceSet(theKnownInstanceSet);
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
                        System.out.println("Valid KnownInstance found with substitutions " + currentKISubs
                                + " attitude " + currentKnownInstance.getAttitudeID());
                        if (notBound) {
                            this.applyDoIfHandler(
                                    Substitutions.union(currentKISubs, filterRuleSubs),
                                    currentRequest, currentKnownInstance.getSupports());
                        } else {
                            this.applyDoIfHandler(filterRuleSubs, currentRequest,
                                    currentKnownInstance.getSupports());
                        }
                    }
                }
                if (!flag) {
                    super.processSingleRequests(currentRequest);
                }

            } else {
                super.processSingleRequests(currentRequest);
            }
        }
    }

    public void applyDoIfHandler(Substitutions substitutions, Request request, Support knownInstanceSupport)
            throws NoSuchTypeException {
        NodeSet acts = this.getDownDoNodeSet();
        int currentAttitudeId = request.getChannel().getAttitudeID();
        HashMap<Integer, PropositionNodeSet> negativeSupport = new HashMap<Integer, PropositionNodeSet>();
        Node ifNode = request.getChannel().getRequesterNode();

        if (ifNode instanceof PropositionNode) {
            if (!ifNode.isOpen()) {
                negativeSupport.put(request.getChannel().getAttitudeID(), new PropositionNodeSet(ifNode));
            } else {
                if (!((PropositionNode) ifNode).isOpenNodeNotBound(request.getChannel().getFilterSubstitutions())) {
                    negativeSupport.put(request.getChannel().getAttitudeID(), new PropositionNodeSet(
                            ifNode.applySubstitution(request.getChannel().getFilterSubstitutions())));
                }
            }
        }
        ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>> support = new ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>>();
        if (!this.isOpen()) {
            HashMap<Integer, PropositionNodeSet> positiveSupport = new HashMap<Integer, PropositionNodeSet>();
            positiveSupport.put(0, new PropositionNodeSet(this));
            Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>> pair = new Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>(
                    positiveSupport, negativeSupport);
            support.add(pair);
        } else {
            if (knownInstanceSupport != null) {
                for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> currSupport : knownInstanceSupport
                        .getJustificationSupport().get(Network.currentLevel).get(currentAttitudeId)) {
                    HashMap<Integer, PropositionNodeSet> hash = new HashMap<>();
                    for (Integer innerAttitude : currSupport.getFirst().keySet()) {
                        PropositionNodeSet currNodeSet = currSupport.getFirst().get(currentAttitudeId).getFirst();
                        hash.put(innerAttitude, currNodeSet);
                    }
                    support.add(new Pair(hash, negativeSupport));
                }
            }
        }
        for (Node act : acts) {
            if (act instanceof ActNode) {
                if (!act.isOpen()) {
                    ((ActNode) act).addToSupports(support);
                    if (!Scheduler.getActQueue().contains((ActNode) act)) {
                        ((ActNode) act).restartAgenda();
                        Scheduler.addToActQueue((ActNode) act);
                        System.out.println("Act " + (ActNode) act + " Scheduled Successfully on act stack");
                    }
                } else {
                    ActNode newAct = (ActNode) ((ActNode) act).applySubstitution(substitutions);
                    newAct.setPrimitive(((ActNode)act).getPrimitive());
                    newAct.addToSupports(support);
                    if (!Scheduler.getActQueue().contains(newAct)) {
                        newAct.restartAgenda();
                        Scheduler.addToActQueue(newAct);
                        System.out.println("Act " + newAct + " Scheduled Successfully on act stack");
                    }
                }
            }
        }
    }

    @Override
    public RuleInfoSet[] mayInfer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mayInfer'");
    }

    @Override
    public void sendInferenceReports(HashMap<RuleInfo, Report> reports) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendInferenceReports'");
    }

}
