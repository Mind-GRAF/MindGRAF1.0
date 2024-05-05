package nodes;

import java.util.*;

import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import components.Substitutions;
import context.ContextController;
import exceptions.NoSuchTypeException;
import mgip.Scheduler;
import mgip.requests.ChannelType;
import mgip.requests.MatchChannel;
import network.Network;
import relations.Relation;
import set.NodeSet;

public class SNIFNode extends ActNode {

    private Agenda controlAgenda;

    public SNIFNode(DownCableSet downCables) {
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
                ContextController.getCurrContextName(), 1, ChannelType.Act, this);
				break;
			case TEST:
				try{
					controlAgenda = Agenda.DONE;
					NodeSet allActs = getDownCableSet().get("obj").getNodeSet();
					NodeSet possibleActs = new NodeSet();
					ArrayList<PropositionNode> satisfiedGaurds = new ArrayList<>();
					for(Node act: allActs) {
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
