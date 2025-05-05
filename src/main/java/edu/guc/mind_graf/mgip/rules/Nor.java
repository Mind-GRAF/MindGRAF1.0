package edu.guc.mind_graf.mgip.rules;


import edu.guc.mind_graf.exceptions.DirectCycleException;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.RuleInfoSet;

import java.util.HashMap;

public class Nor extends RuleNode {
    public Nor(String name, Boolean isVariable) {
        super(name, isVariable);
    }

    @Override
    public RuleInfoSet[] mayInfer() {
        return new RuleInfoSet[0];
    }

    @Override
    public void sendInferenceReports(HashMap<RuleInfo, Report> reports) throws DirectCycleException, NoSuchTypeException {

    }

}
