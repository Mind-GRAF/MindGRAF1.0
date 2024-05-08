package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.KnownInstance;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.Channel;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.PtreeNode;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.FlagNode;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleIntroduction.RII;
import edu.guc.mind_graf.support.Support;

import java.util.ArrayDeque;
import java.util.Collection;

public class AndEntailment extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;
    private int cAnt; // number of constant antecedents

    public AndEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("ants").getNodeSet();
        cq = downcableSet.get("cqs").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(ant);
        cAnt = ant.size() - antecedents.size();
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, antecedents.size(), Integer.MAX_VALUE, 2);
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
//            for (RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
            for(RuleInfo ri : this.getRootRuleInfos()){
                if (ri.getPcount() == ant.size())
                    inferrable[0].addRuleInfo(ri);
            }
        }
        return inferrable;
    }

    public void putInferenceReportOnQueue(Report report) {
        for(Node node : cq) {
            report.setRequesterNode(node);
            Scheduler.addToHighQueue(report);
        }
    }

    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException{
        String currContextName = currentRequest.getChannel().getName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        System.out.println("Current Request: " + currentRequest + " Current Context: " + currContextName +
        " Attitude: " + attitude + " Filter Subs: " + filterSubs + " Switch Subs: " + switchSubs + 
        " Requester Node: " + currentRequest.getChannel().getRequesterNode().getName()+"\n");
        NodeSet ants = this.getDownAntArgNodeSet();
        System.out.println("Antecedents: "+ants +" of node "+ this);
        NodeSet subants = new NodeSet();
        for(Node ant : ants) {
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
       NodeSet cons = this.getDownConsNodeSet();
        System.out.println("Consequents are :" + cons);
       Context newContext = new Context("Context 2 new",attitude,subants);//clone of the current context in addition to the assumed antecedents
       // boolean validContext = newContext.checkValidity(ants);
        System.out.println("New Context is" + newContext.getName());
        RII rii = new RII(currentRequest, subants, cons , newContext , attitude);
        System.out.println("RII created "+ rii);
        mcii.addRII(rii);
        System.out.println("MCII contains" + mcii);
        sendRequestsToNodeSet(cons,filterSubs,switchSubs,newContext.getName(),attitude,ChannelType.Introduction,this);
        return true;
    }

    public static Node buildPosInstance(RII rii, NodeSet Subs) throws NoSuchTypeException {
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new AndEntailment(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ants");
        DownCable oldCqDownCable = oldDownCableSet.get("cqs");
        oldAntDownCable.replaceNodeSet(Subs);
        NodeSet newCqs =new NodeSet();
        for (Node n : rii.getConqArgNodes())
        {
            n = n.applySubstitution(rii.getRequest().getChannel().getFilterSubstitutions());
            newCqs.add(n);
        }
        oldCqDownCable.replaceNodeSet(newCqs);
        newNode.setName(node.getName()+"_instance "+ ++instanceCount);
        return newNode;
    }

    public static Node buildNegInstance(RII rii, NodeSet Subs) throws NoSuchTypeException{
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new AndEntailment(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ants");
        DownCable oldCqDownCable = oldDownCableSet.get("cqs");
        oldAntDownCable.replaceNodeSet(Subs);
        NodeSet newCqs =new NodeSet();
        for (Node n : rii.getConqArgNodes())
        {
            n = n.applySubstitution(rii.getRequest().getChannel().getFilterSubstitutions());
            newCqs.add(n);
        }
        oldCqDownCable.replaceNodeSet(newCqs);
        newNode.setName(node.getName()+"_instance " + ++instanceCount);
        Node negatedNode = createNegation(newNode);
        return negatedNode;
    }

    public int introductionHandler(RII rii) throws NoSuchTypeException {
        System.out.println("In introHandler");
        Substitutions KIsubs = rii.getRequest().getChannel().getFilterSubstitutions();
        Node reqsterNode = rii.getRequest().getChannel().getRequesterNode();
        System.out.println("Conq nodes size " + rii.getConqArgNodes().size());
        if( (rii.getPosCount() == rii.getConqArgNodes().size())){
            System.out.println("In pos instance");
            rii.setSufficent();
            System.out.println("Starting to build");
//            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet(rii.getAntNodes());
            //Build an instance of the rule using the substitutions found in the original request.
            System.out.println("Ant Nodes" + rii.getAntNodes());
            System.out.println("Ant Node 1" + rii.getAntNodes().get("M10"));
            System.out.println("Ant Node 2" + rii.getAntNodes().get("M11"));
            System.out.println("Cq Node 1" + rii.getConqArgNodes().get("P3"));
            System.out.println("Cq Node 2" + rii.getConqArgNodes().get("P4"));
            RuleNode instance = (RuleNode) buildPosInstance(rii, rii.getAntNodes());
            System.out.println("Built instance " + instance);
            System.out.println("New Cq Node 1" + instance.getDownCable("cqs").getNodeSet().get("M12"));
            System.out.println("New Cq Node 2" + instance.getDownCable("cqs").getNodeSet().get("M13"));
            //send a report declaring this instance in the context of the original request having the support Sup.
            Report report = new Report(KIsubs, supp, rii.getAttitudeID(), true, InferenceType.INTRO, reqsterNode , instance);
            System.out.println("Report used to create KI " + report.stringifyReport());
            this.knownInstances.addKnownInstance(report);
            Collection<KnownInstance> KIs= knownInstances.mergeKInstancesBasedOnAtt(rii.getAttitudeID());
            this.knownInstances.printKnownInstanceSet(KIs);
            instance.broadcastReport(report);
            return 1;
        }
        else if(rii.getNegCount()>0)
        {
            System.out.println("In Negative instance");
            rii.setSufficent();
//            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance;
            try {
                instance = (RuleNode) buildNegInstance(rii, rii.getAntNodes());
                System.out.println("Built instance " + instance);
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(KIsubs, supp, rii.getAttitudeID(), false, InferenceType.INTRO, reqsterNode, instance);
                this.knownInstances.addKnownInstance(report);
                Collection<KnownInstance> KIs= knownInstances.mergeKInstancesBasedOnAtt(rii.getAttitudeID());
                System.out.println("Report used to create KI" + report.stringifyReport());
                this.knownInstances.printKnownInstanceSet(KIs);
                instance.broadcastReport(report);
                return -1;
            } catch (NoSuchTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return 0;
    }

}
