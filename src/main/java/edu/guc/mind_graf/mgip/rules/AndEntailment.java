package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;

public class AndEntailment extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;

    public AndEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("&ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, antecedents.size(), Integer.MAX_VALUE, 2);

    }

}
