package mgip.rules;

import cables.DownCable;
import cables.DownCableSet;
import context.Context;
import exceptions.NoSuchTypeException;
import mgip.ruleIntroduction.MCII;
import mgip.ruleIntroduction.RII;
import nodes.Node;
import nodes.RuleNode;
import set.NodeSet;
import set.PropositionNodeSet;
import support.Support;
import mgip.InferenceType;
import mgip.Report;
import mgip.requests.ChannelType;
import mgip.requests.Request;
import components.Substitutions;

public class AndEntail extends RuleNode{
    public AndEntail(String name, Boolean isVariable){
        super(name, isVariable);
    }

    public AndEntail(DownCableSet downCableSet) {
        super(downCableSet);
    }

    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException{
        String currContextName = currentRequest.getChannel().getContextName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        Context currContext = getContext(currContextName);
        System.out.println("Current Request: " + currentRequest + " Current Context: " + currContextName + 
        " Attitude: " + attitude + " Filter Subs: " + filterSubs + " Switch Subs: " + switchSubs + 
        " Requester Node: " + currentRequest.getChannel().getRequesterNode().getName()+"\n");
        System.out.println("In NumEntail Node");
        System.out.println("In AndEntail Node");
        NodeSet ants = this.getDownAntArgNodeSet();
        System.out.println("Antecedents: "+ants);
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
       Context newContext = new Context("Context 2 new",attitude,subants);//clone of the current context in addition to the assumed antecedents
       // boolean validContext = newContext.checkValidity(ants);
        RII rii = new RII(currentRequest, subants, cons , newContext , attitude);
        mcii.addRII(rii);
        sendRequestsToNodeSet(cons,filterSubs,switchSubs,newContext.getContextName(),attitude,ChannelType.Introduction,this);
        return true;
    }

    public static Node buildPosInstance(RII rii, NodeSet Subs){
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new AndEntail(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ant");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        return newNode;
    }

    public static Node buildNegInstance(RII rii, NodeSet Subs) throws NoSuchTypeException{
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new AndEntail(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ant");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        Node negatedNode = createNegation(newNode);
        return negatedNode;
    }

    public int introductionHandler(RII rii)
    {
        if( (rii.getPosCount() == rii.getConqArgNodes().size())){
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance = (RuleNode) buildPosInstance(rii, rii.getAntNodes());
            //send a report declaring this instance in the context of the original request having the support Sup.
            Report report = new Report(null, supp, rii.getAttitudeID(), true, InferenceType.INTRO, null); 
            instance.broadcastReport(report);
            return 1;
        }
        else if(rii.getNegCount()>0)
        {
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance;
            try {
                instance = (RuleNode) buildNegInstance(rii, rii.getAntNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), false, InferenceType.INTRO, null); 
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
