package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.ArrayList;
import java.util.HashMap;

public class AndEntailment extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;
    private int cAnt; // number of constant antecedents

    public AndEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        System.out.println("Creating an and-entailment rule node");
        ant = downcableSet.get("ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        cAnt = ant.size() - antecedents.size();
        System.out.println("The rule has " + antecedents.size() + " variable antecedents and " + cAnt + " constant antecedents.");
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, antecedents.size(), Integer.MAX_VALUE, 2);
        this.ruleInfoHandler.setcMin(cAnt);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        for(RuleInfo ri : this.getRootRuleInfos()){
            if (ri.getPcount() == ant.size())
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

    public void sendInferenceReports(HashMap<RuleInfo, Report> reports) {
        sendInferenceToCq(reports, cq);
    }

    public void applyRuleHandler(Report report) throws NoSuchTypeException {
        if(report.isSign()){
            super.applyRuleHandler(report);
        }
    }

}
