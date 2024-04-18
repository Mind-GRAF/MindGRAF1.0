package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class Thresh extends RuleNode {

    private int thresh;
    private int threshmax;
    private NodeSet arg;

    public Thresh(DownCableSet downcableSet) {
        super(downcableSet);
        thresh = downcableSet.get("thresh").getNodeSet().getIntValue();
        threshmax = downcableSet.get("threshmax").getNodeSet().getIntValue();
        arg = downcableSet.get("arg").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(arg);
        int cAnt = arg.size() - antecedents.size(); // number of constants in the antecedents (total args - variable args)
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, Math.max(0, thresh - 1 - cAnt), Math.max(0, arg.size() - threshmax - 1 - cAnt), 0);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet(), new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        for(RuleInfo ri : ruleInfoHandler.getInferrablRuleInfos()) {
            if(ri.getPcount() == thresh - 1 && ri.getNcount() == arg.size() - threshmax)
                inferrable[1].addRuleInfo(ri);
            else if(ri.getPcount() == thresh && ri.getNcount() == (arg.size() - threshmax - 1))
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

}
