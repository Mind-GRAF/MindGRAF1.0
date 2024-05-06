package edu.guc.mind_graf.mgip.rules;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.ruleHandlers.Ptree;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfo;
import edu.guc.mind_graf.mgip.ruleHandlers.RuleInfoHandler;
import edu.guc.mind_graf.nodes.Node;
import edu.guc.mind_graf.nodes.RuleNode;
import edu.guc.mind_graf.set.NodeSet;
import edu.guc.mind_graf.set.PropositionNodeSet;
import edu.guc.mind_graf.set.RuleInfoSet;
import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.InferenceType;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.mgip.requests.Request;
import edu.guc.mind_graf.mgip.ruleIntroduction.RII;
import edu.guc.mind_graf.support.Support;

public class Thresh extends RuleNode {

    private int thresh;
    private int threshmax;
    private NodeSet arg;

    public Thresh(DownCableSet downcableSet) {
        super(downcableSet);
        thresh = downcableSet.get("thresh").getNodeSet().getIntValue();
        threshmax = downcableSet.get("threshmax").getNodeSet().getIntValue();
        arg = downcableSet.get("args").getNodeSet();
        PropositionNodeSet antecedents = RuleInfoHandler.getVariableAntecedents(arg);
        int cAnt = arg.size() - antecedents.size(); // number of constants in the antecedents (total args - variable args)
        this.ruleInfoHandler = Ptree.constructPtree(antecedents, Math.max(0, thresh - 1 - cAnt), Math.max(0, arg.size() - threshmax - 1 - cAnt), 0);
    }

    public RuleInfoSet[] mayInfer() {
        RuleInfoSet[] inferrable = {new RuleInfoSet(), new RuleInfoSet()};  // at index 0 the set of positively inferred, at index 1 the set of negatively inferred
//        for(RuleInfo ri : ruleInfoHandler.getInferrableRuleInfos()) {
        for(RuleInfo ri : this.getRootRuleInfos()){
            if(ri.getPcount() == thresh - 1 && ri.getNcount() == arg.size() - threshmax)
                inferrable[1].addRuleInfo(ri);
            else if(ri.getPcount() >= thresh && ri.getNcount() >= (arg.size() - threshmax - 1))
                inferrable[0].addRuleInfo(ri);
        }
        return inferrable;
    }

    public void putInferenceReportOnQueue(Report report) {
        for(Node node : arg) {
            if(!report.getSupport().contains(node)) {
                report.setRequesterNode(node);
                Scheduler.addToHighQueue(report);
            }
        }
    }

    @Override
    public boolean processIntroductionRequest(Request currentRequest) throws NoSuchTypeException {
        String currContextName = currentRequest.getChannel().getName();
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
        Node newNode = new Thresh(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("arg");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        return newNode;
    }

    public static Node buildNegInstance(RII rii, NodeSet Subs) throws NoSuchTypeException{
        Node node = rii.getReportSet().getReport().getRequesterNode();
        Node newNode = new Thresh(node.getDownCableSet());
        DownCableSet oldDownCableSet = newNode.getDownCableSet();
        DownCable oldAntDownCable = oldDownCableSet.get("ant");
        oldAntDownCable.replaceNodeSet(Subs);
        newNode.setName(node.getName()+"_instance");
        Node negatedNode = createNegation(newNode);
        return negatedNode;
    }

    public int introductionHandler(RII rii)
    {
        if( (rii.getPosCount() > threshmax) || (rii.getNegCount() > rii.getConqArgNodes().size() - thresh)){
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance = (RuleNode) buildPosInstance(rii, rii.getConqArgNodes());
            //send a report declaring this instance in the context of the original request having the support Sup.
            Report report = new Report(null, supp, rii.getAttitudeID(), true, InferenceType.INTRO, null, instance); 
            instance.broadcastReport(report);
            return 1;
        }
        else if((rii.getPosCount() >= thresh) && (rii.getNegCount() >= rii.getConqArgNodes().size() - threshmax))
        {
            rii.setSufficent();
            Support sup = combineSupport(rii);
            PropositionNodeSet supp = new PropositionNodeSet();
            //Build an instance of the rule using the substitutions found in the original request.
            RuleNode instance;
            try {
                instance = (RuleNode) buildNegInstance(rii, rii.getConqArgNodes());
                //send a report declaring this instance in the context of the original request having the support Sup.
                Report report = new Report(null, supp, rii.getAttitudeID(), false, InferenceType.INTRO, null, instance); 
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
