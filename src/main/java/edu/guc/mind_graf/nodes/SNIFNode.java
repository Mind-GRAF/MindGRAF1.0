package edu.guc.mind_graf.nodes;

import java.util.ArrayList;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.set.NodeSet;

public class SNIFNode extends ActNode {

    private ActAgenda controlAgenda;

    public SNIFNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
        controlAgenda = ActAgenda.START;
    }

    @Override
    public void runActuator() {
        switch(controlAgenda) {
			case START:
                controlAgenda = ActAgenda.TEST;
                Scheduler.addToActQueue(this);
                NodeSet guards = new NodeSet();
				System.out.println("Sending requests to guards");
                for(Node n: this.getDownCableSet().get("obj").getNodeSet()) {
                    guards.addAllTo(n.getDownCableSet().get("guard").getNodeSet());
					n.sendRequestsToNodeSet(guards, new Substitutions(), new Substitutions(),
                		ContextController.getCurrContextName(), 0, ChannelType.Act, n);
                }
				System.out.println("Sending requests to guards");
                break;
			case TEST:
                try{
                    controlAgenda = ActAgenda.DONE;
                    NodeSet allActs = getDownCableSet().get("obj").getNodeSet();
                    NodeSet possibleActs = new NodeSet();
                    ArrayList<PropositionNode> satisfiedGaurds = new ArrayList<>();
					System.out.println("Checking if guards are satisfied");
                    for(Node act: allActs) {
                        ArrayList<Report> reports = ((ActNode) act).getReports();
                        for(Report report: reports){
                            if(report.isSign()==true){
                                satisfiedGaurds.add((PropositionNode) report.getReporterNode());
                            }
                        }
                        boolean containsAll = true;
                        for(Node n: act.getDownCableSet().get("guard").getNodeSet()) {
							System.out.println("Checking if satisfied guards contain: " + n.getName());
                            if(!satisfiedGaurds.contains(n)) {
                                containsAll = false;
                                break;
                            }
                        }
                        if(containsAll) {
							System.out.println("Adding to possible acts");
                            possibleActs.add((ActNode) act.getDownCableSet().get("act").getNodeSet().getNode(0));
                        }
                    }
                    sendDoOneToActQueue(possibleActs);
                } catch(Exception e) {
                    System.out.println("SOMETHING WENT WRONG!! EXCEPTION THROWN FROM ACTNODE.JAVA 10");
                }
                break;
			default:
                System.out.print("UNIDENTIFIED AGENDA FOR ACHIEVE!!");
		}
    }

}
