package edu.guc.mind_graf.nodes;

import java.util.Stack;

import edu.guc.mind_graf.cables.DownCable;
import edu.guc.mind_graf.cables.DownCableSet;
import edu.guc.mind_graf.mgip.Scheduler;

public class SNSequenceNode extends ActNode {

    public SNSequenceNode(DownCableSet downCables) {
        super(downCables);
    }

    //howa eshm3na fl SNSequence byb2a el node 3obara 3n object1 w object2 wl ba2y object bas

    @Override
    public void runActuator() {
        Stack<ActNode> acts = new Stack<>();
        DownCable next = this.getDownCableSet().get("obj" + 1);
        ActNode act;
        for(int i=2; next!=null; i++) {
			act = (ActNode) next.getNodeSet().getNode(0);
			act.restartAgenda();
			acts.push(act);
			next = this.getDownCableSet().get("obj" + i);
            System.out.println(i);
		}
        while(!acts.isEmpty()) {
            System.out.println(acts.peek().getName());
            Scheduler.addToActQueue(acts.pop());
        }
    }

}
