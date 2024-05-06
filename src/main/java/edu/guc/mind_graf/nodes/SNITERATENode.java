package edu.guc.mind_graf.nodes;

import java.util.*;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Report;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.set.NodeSet;

public class SNITERATENode extends ActNode {

    private ActAgenda controlAgenda;

    public SNITERATENode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        switch(controlAgenda) {
			case START:
				controlAgenda = Agenda.TEST;
				Scheduler.addToActQueue(this);
				NodeSet guards = new NodeSet();
				for(Node n: getDownCableSet().get("obj").getNodeSet()) {
					guards.addAllTo(n.getDownCableSet().get("guard").getNodeSet());
				}
                this.sendRequestsToNodeSet(guards, new Substitutions(), new Substitutions(),
                ContextController.getCurrContextName(), 0, ChannelType.Act, this);
				break;
			case TEST:
				try{
					controlAgenda = Agenda.DONE;
					NodeSet allActs = getDownCableSet().get("obj").getNodeSet();
					NodeSet possibleActs = new NodeSet();
					ArrayList<PropositionNode> satisfiedGaurds = new ArrayList<>();
					for(Node act: allActs) {
						ArrayList<Report> reports = ((ActNode) act).getReports();
						for(Report report: reports){
							if(report.isSign()==true){
								satisfiedGaurds.add((PropositionNode) report.getReporterNode());
							}
						}
						boolean containsAll = true;
						for(Node n: act.getDownCableSet().get("guard").getNodeSet()) {
							if(!satisfiedGaurds.contains(n)) {
								containsAll = false;
								break;
							}
						}
						if(containsAll) {
							possibleActs.add((ActNode) act);
						}
					}
					sendDoOneToActQueue(possibleActs);
					this.restartAgenda();
					Scheduler.addToActQueue(this);
				} catch(Exception e) {
					System.out.println("SOMETHING WENT WRONG!! EXCEPTION THROWN FROM ACTNODE.JAVA 10");
				}
				break;
			default:
				System.out.print("UNIDENTIFIED AGENDA FOR ACHIEVE!!");
				return;
		}
    }

}
