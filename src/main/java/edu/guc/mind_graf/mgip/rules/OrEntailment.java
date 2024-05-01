package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.OrEntailhandler;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.ArrayList;

public class OrEntailment  extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;

    public OrEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        this.ruleInfoHandler = new OrEntailhandler();
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        if(((OrEntailhandler)this.ruleInfoHandler).getUsedToInfer().getPcount() > 0) {
            inferrable[0].addRuleInfo(((OrEntailhandler)this.ruleInfoHandler).getUsedToInfer());
        }
        return inferrable;
    }

    public void sendInferenceReports(ArrayList<Report> reports) {
        sendInferenceToCq(reports, cq);
    }
}
