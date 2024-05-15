package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.HashMap;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
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
                        .getJustificationSupport().get(0).get(currentAttitudeId)) {
                    HashMap<Integer, PropositionNodeSet> hash = new HashMap<>();
                    for (Integer innerAttitude : currSupport.getFirst().keySet()) {
                        PropositionNodeSet currNodeSet = currSupport.getFirst().get(currentAttitudeId).getFirst();
                        hash.put(innerAttitude, currNodeSet);
                    }
                    support.add(new Pair(hash, negativeSupport));
                }
                // TODO: marwa I commented this as it was causing errors
                // for (HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> entry :
                // knownInstanceSupport
                // .getJustificationSupport().getFirst().get(request.getChannel().getAttitudeID()))
                // {
                // HashMap<Integer, PropositionNodeSet> temp = new HashMap<Integer,
                // PropositionNodeSet>();
                // for (Integer attitude : entry.keySet()) {
                // temp.put(attitude, entry.get(attitude).getFirst());
                // }
                // Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer,
                // PropositionNodeSet>> pair = new Pair<HashMap<Integer, PropositionNodeSet>,
                // HashMap<Integer, PropositionNodeSet>>(
                // temp, negativeSupport);
                // support.add(pair);
                // }
            }
        }
        for (Node act : acts) {
            if (act instanceof ActNode) {
                if (!act.isOpen()) {
                    ((ActNode) act).addToSupports(support);
                    if (!Scheduler.getActQueue().contains((ActNode) act)) {
                        ((ActNode) act).restartAgenda();
                        Scheduler.addToActQueue((ActNode) act);
                    }
                } else {
                    ActNode newAct = (ActNode) ((ActNode) act).applySubstitution(substitutions);
                    newAct.addToSupports(support);
                    if (!Scheduler.getActQueue().contains(newAct)) {
                        newAct.restartAgenda();
                        Scheduler.addToActQueue(newAct);
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
