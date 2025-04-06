package edu.guc.mind_graf.nodes;

import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.context.Context;
import edu.guc.mind_graf.context.ContextController;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;
import edu.guc.mind_graf.set.ContextSet;
import edu.guc.mind_graf.set.NodeSet;

public class AchieveNode extends ActNode {

	private ActAgenda controlAgenda;
	static int achieveCount;

	public AchieveNode(DownCableSet downCables) {
		super(downCables);
		this.setPrimitive(true);
	}

	public void runActuator() throws NoSuchTypeException {
		switch (controlAgenda) {
			case START:
				PropositionNode goal = (PropositionNode) this.getDownCableSet().get("obj").getNodeSet().getNode(0);
				ContextSet cs = ContextController.getContextSet();
				for (Context c : cs.getSet().values()) {
					if (c.isHypothesis(1, goal))
						return;
				}
				try {
					controlAgenda = ActAgenda.FIND_PLANS;
					Scheduler.addToActQueue(this);
					searchForPlansInAchieve(goal);
				} catch (NoSuchTypeException e) {
					e.printStackTrace();
				}
				break;
			case FIND_PLANS:
				NodeSet plans = this.processReportsInAct();
				if (!plans.isEmpty()) {
					controlAgenda = ActAgenda.DONE;
					sendDoOneToActQueue(plans);
				}
			default:
				break;
		}
	}

}