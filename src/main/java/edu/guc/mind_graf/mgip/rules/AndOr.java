package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
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

}
