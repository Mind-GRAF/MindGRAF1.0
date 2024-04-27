package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class NumEntailment extends RuleNode {

    private int i;
    private NodeSet ant;
    private NodeSet cq;

    public NumEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        i = downcableSet.get("i").getNodeSet().getIntValue();
        ant = downcableSet.get("&ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        int cAnt = ant.size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, Math.max(0, i - cAnt), Integer.MAX_VALUE, 1);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        //       for(RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
        for(RuleInfo ri : this.getRootRuleInfos()){
            if(ri.getPcount() >= i)
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

    public void putInferenceReportOnQueue(Report report) {
        for(Node node : cq) {
            report.setRequesterNode(node);
        }
        Scheduler.addToHighQueue(report);
    }

}
