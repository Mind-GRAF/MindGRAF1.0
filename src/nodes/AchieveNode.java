package nodes;

import java.util.Stack;

import cables.DownCable;
import cables.DownCableSet;
import caseFrames.Adjustability;
import exceptions.NoSuchTypeException;
import mgip.Scheduler;
import network.Network;
import relations.Relation;
import set.NodeSet;

public class AchieveNode extends ActNode {

    private Agenda controlAgenda;

    public AchieveNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) throws NoSuchTypeException {
        switch(controlAgenda){
			case START:
				PropositionNode goal = (PropositionNode) node.getDownCableSet().get("obj").getNodeSet().getNode(0);
				//hwa ana hacheck beliefs bs wala kol el attitudes eza kanet asserted?
				//TODO Check if p is asserted
				boolean asserted = true;
				if(asserted) {
					return;
				} else {
					try {
						controlAgenda = Agenda.FIND_PLANS;
						Scheduler.addToActQueue(node);
						searchForPlansInAchieve(goal);
					} catch (NoSuchTypeException e) {
						e.printStackTrace();
					}
				}
				break;
			case FIND_PLANS:
				NodeSet plans = this.processReportsInAct();
				if(!plans.isEmpty()) {
					controlAgenda = Agenda.DONE;
					sendDoOneToActQueue(plans);
				}
			default:
				break;
		}
    }

}
