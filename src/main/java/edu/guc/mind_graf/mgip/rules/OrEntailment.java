package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;

public class OrEntailment  extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;

    public OrEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
    }

}
