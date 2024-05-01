package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.Orentailhandler;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;

public class OrEntailment  extends RuleNode {

    private NodeSet ant;
    private NodeSet cq;

    public OrEntailment(DownCableSet downcableSet) {
        super(downcableSet);
        ant = downcableSet.get("ant").getNodeSet();
        cq = downcableSet.get("cq").getNodeSet();
        this.ruleInfoHandler = new Orentailhandler();
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
        if(((Orentailhandler)this.ruleInfoHandler).getUsedToInfer().getPcount() > 0) {
            inferrable[0].addRuleInfo(((Orentailhandler)this.ruleInfoHandler).getUsedToInfer());
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
        String currContextName = currentRequest.getChannel().getContextName();
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        Context currContext = getContext(currContextName);
        System.out.println("Current Request: " + currentRequest + " Current Context: " + currContextName + 
        " Attitude: " + attitude + " Filter Subs: " + filterSubs + " Switch Subs: " + switchSubs + 
        " Requester Node: " + currentRequest.getChannel().getRequesterNode().getName()+"\n");
        System.out.println("In NumEntail Node");
            NodeSet ants = this.getDownAntArgNodeSet();
            System.out.println("Rule Ants"+ants);
            NodeSet cons = this.getDownConsNodeSet();
            System.out.println("Rule Cons"+cons);
            System.out.println("MCII at creation " +mcii );
        System.out.println("In OrEntail Node");
        for (Node ant : ants) {
            if(ant.isFree(this)){
                for(Node freeVar : this.getFreeVariables())
                {
                    if(filterSubs.contains(freeVar)){}
                    else{
                      return false;  
                    };
                }
            }
            ant = ant.applySubstitution(currentRequest.getChannel().getFilterSubstitutions());
            Context newContext = new Context(String.valueOf(ant.getId()), attitude, new NodeSet(ant));
            System.out.println("New Context: " + newContext.getContextName());
            RII rii = new RII(currentRequest, new NodeSet(ant), cons, newContext, attitude);
            System.out.println("RII of new Context: " + rii.getContext().getContextName() + " Attitude: "
                    + rii.getAttitudeID() + " Request: " + rii.getRequest() + " Antecedents: " + rii.getAntNodes() 
                    + " Consequents: " + rii.getConqArgNodes() + "Requester"+ currentRequest.getChannel().getRequesterNode().getName() + "\n");
            mcii.addRII(rii);
            System.out.println("MCII" + mcii);
            sendRequestsToNodeSet(cons, filterSubs, switchSubs, newContext.getContextName(), attitude, ChannelType.Introduction, this);
        }
        return true;
    }

    public static Node buildPosInstance(RII rii, NodeSet Subs){
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new OrEntail(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ant");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        return newNode;
    }

    public static Node buildNegInstance(RII rii, NodeSet Subs) throws NoSuchTypeException{
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new OrEntail(node.getDownCableSet());
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
            Support sup = combineSupport(mcii);
            mcii.addPosRII();;
            if(mcii.isSufficient())
            {
                PropositionNodeSet supp = new PropositionNodeSet();
                //Build an instance of the rule using the substitutions found in the original request.
                RuleNode builtInstace = (RuleNode) buildPosInstance(rii,rii.getAntNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), true, InferenceType.INTRO, null); 
                builtInstace.broadcastReport(report);
                return 1;
            }
        }
        else if(rii.getNegCount()>0)
        {
            rii.setSufficent();
            Support sup = combineSupport(mcii);
            mcii.setSufficent();
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build a negative instance of the rule using the substitutions found in the original request.
            RuleNode builtInstace;
            try {
                builtInstace =(RuleNode) buildNegInstance(rii,rii.getAntNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), false, InferenceType.INTRO, null); 
                builtInstace.broadcastReport(report);
                return -1;
            } catch (NoSuchTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return 0;
    }

}
