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
		controlAgenda = ActAgenda.START;
    }

	public ActAgenda getControlAgenda() {
        return controlAgenda;
    }

    @Override
    public void runActuator() throws NoSuchTypeException{
        switch(controlAgenda){
			case START:
				PropositionNode goal = (PropositionNode) this.getDownCableSet().get("obj").getNodeSet().getNode(0);
				ContextSet cs = ContextController.getContextSet();
				for(Context c : cs.getSet().values()) {
					if(goal.supported(c.getName(), 0, 0)){
						System.out.println("Goal is already supported");
						controlAgenda = ActAgenda.DONE;
						return;
					}
				}
				try {
					controlAgenda = ActAgenda.FIND_PLANS;
					searchForPlansInAchieve(goal);
					Scheduler.addToActQueue(this);
				} catch (NoSuchTypeException e) {
					e.printStackTrace();
				}
				break;
			case FIND_PLANS:
				NodeSet plans = this.processReportsInAct();
				System.out.println("Plans found: " + plans);
				try{
					if(!plans.isEmpty()) {
						controlAgenda = ActAgenda.DONE;
						sendDoOneToActQueue(plans);
					}
				}
				catch(NullPointerException e){
					System.out.println("No plans found");
				}
				this.setAgenda(ActAgenda.EXECUTE);
				Scheduler.addToActQueue(this);
				break;
			case DONE:
				System.out.println("Achieve is done");
				Scheduler.addToActQueue(this);
				break;
			default:
				System.out.print("UNIDENTIFIED AGENDA FOR ACHIEVE!!");
		}
    }

}
