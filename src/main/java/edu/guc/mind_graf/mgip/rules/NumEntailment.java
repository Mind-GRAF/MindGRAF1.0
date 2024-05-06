package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.mgip.ruleIntroduction.RII;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.CombinationSet;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.support.Support;

import java.util.List;

public class NumEntailment extends RuleNode {

    private int i;
    private NodeSet ant;
    private NodeSet cq;

    public NumEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        i = downcableSet.get("i").getNodeSet().getIntValue();
        ant = downcableSet.get("ants").getNodeSet();
        cq = downcableSet.get("cqs").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        int cAnt = ant.size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, Math.max(0, i - cAnt), Integer.MAX_VALUE, 1);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        //       for(RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
        for(RuleInfo ri : this.getRootRuleInfos()){
            if(ri.getPcount() >= i)
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

    public void putInferenceReportOnQueue(Report report) {
        for(Node node : cq) {
            report.setRequesterNode(node);
        }
        Scheduler.addToHighQueue(report);
    }

    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException, NoSuchTypeException {
        // if(this.introduced){
        //     for(RII rii : mcii.getRIIList()){
        //         if(currentRequest.getChannel().getAttitudeID() == rii.getAttitudeID() && rii.getContext().isSubset(currentRequest.getChannel().getContext()))
        //         {
        //             return false;
        //         }
        //     }
        // }
        String currContextName = currentRequest.getChannel().getName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        Context currContext = getContext(currContextName);
        System.out.println("Current Request: " + currentRequest + " Current Context: " + currContextName + 
        " Attitude: " + attitude + " Filter Subs: " + filterSubs + " Switch Subs: " + switchSubs + 
        " Requester Node: " + currentRequest.getChannel().getRequesterNode().getName()+"\n");
        System.out.println("In NumEntailment Node");
            NodeSet ants = this.getDownAntArgNodeSet();
            System.out.println("Rule Ants"+ants);
            NodeSet cons = this.getDownConsNodeSet();
            System.out.println("Rule Cons"+cons);
            System.out.println("MCII at creation " +mcii );
            NodeSet subants = new NodeSet();
            for(Node ant : ants) {
                // System.out.println("Ant" + ant);
                // System.out.println("Ant is FREEEE "+ ant.isFree(this));
                if(ant.isFree(this)){
                    for(Node freeVar : this.getFreeVariables())
                    {
                        if(filterSubs.contains(freeVar)){
                            System.out.println("Free Variable: "+freeVar);
                            System.out.println("Filter Subs Contain ant: "+filterSubs.contains(freeVar));
                        }
                        else{
                          return false;  
                        };
                    }
                }
                ant = ant.applySubstitution(currentRequest.getChannel().getFilterSubstitutions());
                subants.add(ant);
                System.out.println("Substituted Ant :"+ant);
            }
            System.out.println("In NumEntailment Node");
            //Gets value of i from the NumEntailment/OrEntail node
            System.out.println("NumEntail I is: " + i);
            int n = 1;
            List<NodeSet> combinations = CombinationSet.generateCombinations(subants, i);
            System.out.println("Combinations are :" + combinations);
            for (NodeSet combination : combinations) {
                Context newContext = new Context("Context " + String.valueOf(n++), attitude, combination);
                System.out.println("New Context: " + newContext.getName());
                RII rii = new RII(currentRequest, combination, cons , newContext , attitude);
                System.out.println("RII of new Context: " + rii.getContext().getName() + " Attitude: "
                        + rii.getAttitudeID() + " Request: " + rii.getRequest() + " Antecedents: " + rii.getAntNodes() 
                        + " Consequents: " + rii.getConqArgNodes() + "Requester"+ currentRequest.getChannel().getRequesterNode().getName() + "\n");
                mcii.addRII(rii);
                System.out.println("MCII" + mcii);
                sendRequestsToNodeSet(cons,filterSubs,switchSubs,newContext.getName(),attitude, ChannelType.Introduction,this);
            }
            return true;
    }

    // public int processIntroductionReport(Report report)
    // {
        
    //     if (report.getReportType() == ReportType.Introduction)
    //     try {
    //         //Procssing the report
    //     } catch (Exception e) {
    //     }
    // }

    public static Node buildPosInstance(RII rii, NodeSet Subs){
        System.out.println("\n In buildPosInstance");
        Node node = rii.getReportSet().getReport().getRequesterNode();
        System.out.println("Original node" + node);
        Node newNode = new NumEntailment(node.getDownCableSet());
        newNode.setName(node.getName()+"_instance");
        DownCableSet oldDownCableSet = node.getDownCableSet();
        System.out.println("Old Down Cable Set" + oldDownCableSet);
        DownCable oldAntDownCable = oldDownCableSet.get("antecedent");
        oldAntDownCable.replaceNodeSet(Subs);
        System.out.println("New Node" + newNode);
        return newNode;
    }

    public static Node buildNegInstance(RII rii, NodeSet Subs) throws NoSuchTypeException{
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new NumEntailment(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("antecedent");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        Node negatedNode = createNegation(newNode);
        return negatedNode;
    }

    public int introductionHandler(RII rii)
    {
        System.out.println("In Intro Handler");
        // System.out.println(rii.getConqArgNodes());
        // System.out.println(rii.getPosCount());
        // System.out.println(rii.getConqArgNodes().size());
        if( (rii.getPosCount() == rii.getConqArgNodes().size())){
            rii.setSufficent();
//            Support sup = combineSupport(mcii);
            mcii.addPosRII();
            System.out.println("MCII pos RII count: " + mcii.getPosRII());
            if(mcii.getPosRII() == mcii.getRIICount())
            {
                mcii.setSufficent();
            }
            if(mcii.isSufficient())
            {
                System.out.println("Reached MCII sufficent");
                PropositionNodeSet supp = new PropositionNodeSet();
                System.out.println("Ants to build instance: "+ rii.getAntNodes());
                //Build an instance of the rule using the substitutions found in the original request.
                RuleNode builtInstace = (RuleNode) buildPosInstance(rii,rii.getAntNodes());
                System.out.println("Positive Instance Built: " + builtInstace + " Antecedents: " + rii.getAntNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), true, InferenceType.INTRO, null, builtInstace);
                builtInstace.broadcastReport(report);
                System.out.println("Report broadcasted:" + report + "in context:" + rii.getContext().getName());
                Substitutions subs = new Substitutions();
                subs.add(builtInstace, builtInstace);
                return 1;
            }
        }
        else if(rii.getNegCount()>0)
        {
            System.out.println("In Negative building");
            rii.setSufficent();
//            Support sup = combineSupport(mcii);
            mcii.addNegRII();
            mcii.setSufficent();
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build a negative instance of the rule using the substitutions found in the original request.
            try {
                RuleNode builtInstace =(RuleNode) buildNegInstance(rii,rii.getAntNodes());
                System.out.println("Negative Instance Built: " + builtInstace + " Antecedents: " + rii.getAntNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), false, InferenceType.INTRO, null, builtInstace);
                report.setContextName(rii.getRequest().getChannel().getName()); 
                builtInstace.broadcastReport(report);
                System.out.println("Report broadcasted:" + report + " in context:" + rii.getRequest().getChannel().getName());
                return -1;
            } catch (NoSuchTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return 0;
    }

}
