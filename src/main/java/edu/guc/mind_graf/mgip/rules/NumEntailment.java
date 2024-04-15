package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;

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

}
