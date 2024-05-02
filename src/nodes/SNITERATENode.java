package nodes;

import java.util.*;

import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import caseFrames.CaseFrame;
import exceptions.NoSuchTypeException;
import mgip.Scheduler;
import mgip.requests.MatchChannel;
import network.Network;
import relations.Relation;
import set.NodeSet;

public class SNITERATENode extends ActNode {

    private Agenda controlAgenda;

    public SNITERATENode(DownCableSet downCables) {
        super(downCables);
    }

	//ezay ba3raf lw el guards satisfied

    public void runActuator(ActNode node) {
        switch(controlAgenda) {
			case START:
				controlAgenda = Agenda.TEST;
				Scheduler.addToActQueue(this);
				NodeSet guards = new NodeSet();
				for(Node n: getDownCableSet().get("obj").getNodeSet()) {
					guards.addAllTo(n.getDownCableSet().get("guard").getNodeSet());
				}
				for(Node guard: guards) {
					guard.receiveRequest(new MatchChannel(new LinearSubstitutions(), new LinearSubstitutions(),
							SNeBR.getCurrentContext().getId(), this, guard, true));
				}
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
