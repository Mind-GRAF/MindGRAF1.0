package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.PtreeNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.ArrayDeque;

public class AndEntailment extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;
    private int cAnt; // number of constant antecedents

    public AndEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("&ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        cAnt = ant.size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, antecedents.size(), Integer.MAX_VALUE, 2);
    }

    public RuleInfoSet tryToInfer(){

        // otherwise we can infer and we'll combine everything with everything and infer them all
        RuleInfoSet inferred = new RuleInfoSet(this.ruleInfoHandler.getConstantAntecedents());
        for(PtreeNode root : ((Ptree) this.ruleInfoHandler).getRoots()){
            inferred = inferred.combineDisjointSets(root.getSIndex().getAllRuleInfos());
        }
        for(RuleInfo ruleInfo : inferred){
            if(ruleInfo.getPcount() < ant.size())
                inferred.removeRuleInfo(ruleInfo);
        }
        return inferred;
    }

    public boolean mayTryToInfer() {
        if(cAnt < this.ruleInfoHandler.getConstantAntecedents().getPcount())
            return false;
        for(PtreeNode root : ((Ptree)ruleInfoHandler).getRoots()) {
            if(root.getSIndex().getAllRuleInfos().isEmpty())  // maybe should also check pcount of roots?
                return false;
        }
        return true;
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        if(mayTryToInfer()) {
            for (RuleInfo ri : ruleInfoHandler.getInferrablRuleInfos()) {
                if (ri.getPcount() == ant.size())
                    inferrable[0].addRuleInfo(ri);
            }
        }
        return inferrable;
    }

}
