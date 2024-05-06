package edu.guc.mind_graf.nodes;

import java.util.Stack;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;

public class SNSequenceNode extends ActNode {

    public SNSequenceNode(DownCableSet downCables) {
        super(downCables);
    }

    public void runActuator(ActNode node) {
        Stack<ActNode> acts = new Stack<>();
        DownCable next = node.getDownCableSet().get("obj" + 1);
        ActNode act;
        for(int i=2; next != null; i++) {
			act = (ActNode) next.getNodeSet().getNode(0);
			act.restartAgenda();
			acts.push(act);
			next = node.getDownCableSet().get("obj" + i);
		}
        while(!acts.isEmpty()) {
            Scheduler.addToActQueue(acts.pop());
        }
    }

}
