package edu.guc.mind_graf.acting.rules;

import java.util.ArrayList;
import java.util.HashMap;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.ActNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Pair;

public class WhenDoNode extends RuleNode {

    public WhenDoNode(DownCableSet downCableSet) {
        super(downCableSet);
        // TODO Auto-generated constructor stub
    }

    public void applyRuleHandler(Report report) throws NoSuchTypeException{
        NodeSet acts = this.getDownDoNodeSet();
        int currentAttitudeId=report.getAttitude();
        if (report.isSign()) {
            ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>> support = new ArrayList<Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>>();
            if (report.getSupport() != null) {
                for (Pair<HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>>, PropositionNodeSet> currSupport : report.getSupport()
                        .getJustificationSupport().get(0).get(currentAttitudeId)) {
                    HashMap<Integer, PropositionNodeSet> hash = new HashMap<>();
                    for (Integer innerAttitude : currSupport.getFirst().keySet()) {
                        PropositionNodeSet currNodeSet = currSupport.getFirst().get(currentAttitudeId).getFirst();
                        hash.put(innerAttitude, currNodeSet);
                    }
                    support.add(new Pair(hash, new HashMap<Integer,PropositionNodeSet>()));
                }
                //TODO: marwa I commented this as it was causing errors
//                for (HashMap<Integer, Pair<PropositionNodeSet, PropositionNodeSet>> entry : report.getSupport()
//                        .getJustificationSupport().getFirst().get(report.getAttitude())) {
//                    HashMap<Integer, PropositionNodeSet> temp = new HashMap<Integer, PropositionNodeSet>();
//                    for (Integer attitude : entry.keySet()) {
//                        temp.put(attitude, entry.get(attitude).getFirst());
//                    }
//                    Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>> pair = new Pair<HashMap<Integer, PropositionNodeSet>, HashMap<Integer, PropositionNodeSet>>(
//                            temp, new HashMap<Integer, PropositionNodeSet>());
//                    support.add(pair);
//                }
            }
            System.out.println("HEy");

            for (Node act : acts) {
                if (act instanceof ActNode) {
                    if (!act.isOpen()) {
                        ((ActNode) act).addToSupports(support);
                        if (!Scheduler.getHighActQueue().contains((ActNode) act)) {
                            if (Scheduler.getActQueue().contains((ActNode) act)) {
                                Scheduler.getActQueue().remove((ActNode) act);
                            }
                            Scheduler.addToHighActQueue((ActNode) act);
                            System.out
                                    .println("Act" + act + " scheduled successfully");
                        }
                    } else {
                        ActNode newAct = (ActNode) ((ActNode) act).applySubstitution(report.getSubstitutions());
                        newAct.addToSupports(support);
                        if (!Scheduler.getHighActQueue().contains((ActNode) newAct)) {
                            if (Scheduler.getActQueue().contains((ActNode) newAct)) {
                                Scheduler.getActQueue().remove((ActNode) newAct);
                            }
                            Scheduler.addToHighActQueue((ActNode) newAct);
                            System.out
                                    .println("Act after applying substitutions" + newAct + " scheduled successfully");
                        }
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
    public void sendInferenceReports(HashMap<RuleInfo, edu.guc.mind_graf.mgip.reports.Report> reports) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendInferenceReports'");
    }

}
