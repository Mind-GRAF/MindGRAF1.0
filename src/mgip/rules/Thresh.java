package mgip.rules;

import cables.DownCable;
import cables.DownCableSet;
import components.Substitutions;
import context.Context;
import exceptions.NoSuchTypeException;
import mgip.InferenceType;
import mgip.Report;
import mgip.requests.ChannelType;
import mgip.requests.Request;
import mgip.ruleIntroduction.RII;
import nodes.Node;
import nodes.RuleNode;
import set.NodeSet;
import set.PropositionNodeSet;
import support.Support;

public class Thresh extends RuleNode {

    public static int min;
    public static int max;
    
    public Thresh(String name, Boolean isVariable) {
        super(name, isVariable);
        // TODO Auto-generated constructor stub
    }

    public Thresh(DownCableSet downCableSet) {
        super(downCableSet);
    }

    @Override
    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException {
        String currContextName = currentRequest.getChannel().getContextName();
        Context currContext = getContext(currContextName);
        int attitude = currentRequest.getChannel().getAttitudeID();
        Substitutions filterSubs = currentRequest.getChannel().getFilterSubstitutions();
        Substitutions switchSubs = currentRequest.getChannel().getSwitcherSubstitutions();
        NodeSet args = this.getDownAntArgNodeSet();
        if (args.size() > 0){
            sendRequestsToNodeSet(args, filterSubs, switchSubs, currContextName, attitude, ChannelType.Introduction, this);
            RII rii = new RII(currentRequest, null, args, currContext, attitude);
            mcii.addRII(rii);
            return true;
        }
        return false;
    }

    public static Node buildPosInstance(RII rii, NodeSet Subs){
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new AndEntail(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("arg");
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
        if( (rii.getPosCount() > max) || (rii.getNegCount() > rii.getConqArgNodes().size() - min)){
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance = (RuleNode) buildPosInstance(rii, rii.getConqArgNodes());
            //send a report declaring this instance in the context of the original request having the support Sup.
            Report report = new Report(null, supp, rii.getAttitudeID(), true, InferenceType.INTRO, null); 
            instance.broadcastReport(report);
            return 1;
        }
        else if((rii.getPosCount() >= min) && (rii.getNegCount() >= rii.getConqArgNodes().size() - max))
        {
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance;
            try {
                instance = (RuleNode) buildNegInstance(rii, rii.getConqArgNodes());
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
