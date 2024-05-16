package edu.guc.mind_graf.nodes;

import java.util.ArrayList;
import java.util.List;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.matching.Match;
import edu.guc.mind_graf.mgip.matching.Matcher;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.set.NodeSet;

public class WithAllNode extends ActNode {

    private ActAgenda controlAgenda;

    public WithAllNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
        ActAgenda controlAgenda = ActAgenda.START;
    }

    @Override
    public void runActuator() throws NoSuchTypeException {
        Node qualifier = this.getDownCableSet().get("qualifier").getNodeSet().getNode(0);
        Node act = this.getDownCableSet().get("obj").getNodeSet().getNode(0);
        switch(controlAgenda) {
			case START:
                controlAgenda = ActAgenda.TEST;
                List<Match> matches = Matcher.match(qualifier, ContextController.getContext(ContextController.getCurrContextName()), 0);
                ((PropositionNode)qualifier).sendRequestsToMatches(matches, new Substitutions(), new Substitutions(),
                ContextController.getCurrContextName(), 0, ChannelType.Matched, act);
                System.out.println("Sending requests to matches");
                this.setAgenda(ActAgenda.EXECUTE);
                Scheduler.addToActQueue(this);
                break;
            case TEST:
                controlAgenda = ActAgenda.DONE;
                ArrayList<Report> reports = ((ActNode)act).getReports();
                NodeSet newActs = new NodeSet();
                System.out.println("Checking for substitutions");
                while(!reports.isEmpty()) {
                    Report report = reports.remove(0);
                    ActNode newAct = (ActNode) ((ActNode) act).applySubstitution(report.getSubstitutions());
                    newActs.add(newAct);
                }
                System.out.println("Substitutions are applied successfully");
                sendDoAllToActQueue(newActs);
                this.setAgenda(ActAgenda.EXECUTE);
                Scheduler.addToActQueue(this);
                break;
            case DONE:
                System.out.println("WithAll done");
                Scheduler.addToActQueue(this);
                break;
            default:
                System.out.print("UNIDENTIFIED AGENDA FOR WITHALL!!");
        }
    }
}
