package edu.guc.mind_graf.nodes;

import java.util.ArrayList;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.components.Substitutions;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.mgip.reports.Report;
import edu.guc.mind_graf.mgip.requests.ChannelType;
import edu.guc.mind_graf.set.NodeSet;

public class MGIfNode extends ActNode {

    private ActAgenda controlAgenda;

    public MGIfNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
        controlAgenda = ActAgenda.START;
    }

    public ActAgenda getControlAgenda() {
        return controlAgenda;
    }  

    @Override
    public void runActuator() throws NoSuchTypeException {
        NodeSet allActs = getDownCableSet().get("obj").getNodeSet();
        switch(controlAgenda) {
			case START:
                this.controlAgenda = ActAgenda.TEST;
                NodeSet guards = new NodeSet();
                for(Node n: allActs) {
                    guards.addAllTo(n.getDownCableSet().get("guard").getNodeSet());
					n.sendRequestsToNodeSet(guards, new Substitutions(), new Substitutions(),
                		ContextController.getCurrContextName(), 0, ChannelType.Act, n);
                }
				System.out.println("Sending requests to guards");
                this.setAgenda(ActAgenda.EXECUTE);
                Scheduler.addToActQueue(this);
                break;
			case TEST:
                try{
                    controlAgenda = ActAgenda.DONE;
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
                    System.out.println("Possible acts:" + possibleActs.size());
                    if(possibleActs.size() > 0)
                        sendDoOneToActQueue(possibleActs);
                    else System.out.println("No possible acts found");
                    this.setAgenda(ActAgenda.EXECUTE);
                    Scheduler.addToActQueue(this);
                } catch(NoSuchTypeException e) {
                    System.out.println("SOMETHING WENT WRONG!! EXCEPTION THROWN FROM ACTNODE.JAVA 10");
                }
                break;
            case DONE:
                System.out.println("MGIf DONE");
                Scheduler.addToActQueue(this);
                break;
			default:
                System.out.print("UNIDENTIFIED AGENDA FOR MGIf!!");
		}
    }

}
