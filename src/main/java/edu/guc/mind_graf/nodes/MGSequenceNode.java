package edu.guc.mind_graf.nodes;

import java.util.Stack;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.exceptions.NoSuchTypeException;
import edu.guc.mind_graf.mgip.Scheduler;

public class MGSequenceNode extends ActNode {

    public MGSequenceNode(DownCableSet downCables) {
        super(downCables);
        this.setPrimitive(true);
    }

    @Override
    public void runActuator() throws NoSuchTypeException {
        Stack<ActNode> acts = new Stack<>();
        DownCable next = this.getDownCableSet().get("obj" + 1);
        ActNode act;
        for(int i=2; next!=null; i++) {
			act = (ActNode) next.getNodeSet().getNode(0);
			act.restartAgenda();
			acts.push(act);
			next = this.getDownCableSet().get("obj" + i);
		}
        while(!acts.isEmpty()) {
            System.out.println(acts.peek().getName());
            Scheduler.addToActQueue(acts.pop());
        }
    }

}
