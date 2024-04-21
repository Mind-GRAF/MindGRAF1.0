package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class AndOr extends RuleNode {

    private int min;
    private int max;
    private NodeSet arg;

    public AndOr(DownCableSet downcableSet) {
        super(downcableSet);
        min = downcableSet.get("min").getNodeSet().getIntValue();
        max = downcableSet.get("max").getNodeSet().getIntValue();
        arg = downcableSet.get("arg").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(arg);
        int cAnt = arg.size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, Math.max(0, max - cAnt), Math.max(0, arg.size() - min - cAnt), 0);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet(), new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
//        for(RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
        for(RuleInfo ri : this.getRootRuleInfos()){
            if(ri.getPcount() == max)
                inferrable[1].addRuleInfo(ri);
            else if(ri.getNcount() == (arg.size() - min))
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

}
