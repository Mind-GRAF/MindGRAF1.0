package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.ruleHandlers.Orentailhandler;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class OrEntailment  extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;

    public OrEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        this.ruleInfoHandler = new Orentailhandler();
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        if(((Orentailhandler)this.ruleInfoHandler).getUsedToInfer().getPcount() > 0) {
            inferrable[0].addRuleInfo(((Orentailhandler)this.ruleInfoHandler).getUsedToInfer());
        }
        return inferrable;
    }

}
