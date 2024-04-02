package actuators;

import java.util.Stack;

import cables.DownCable;
import mgip.Scheduler;
import nodes.ActNode;

public class SNSequenceActuator implements ControlActuator{

    //hwa ana mehtag akhod attitude id ka parameter?

    private static SNSequenceActuator actuator;

    @Override
    public void actOnNode(ActNode node, int AttitudeID) {
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

    public static SNSequenceActuator init() {
        if(actuator == null) {
            actuator = new SNSequenceActuator();
        }
        return actuator;
    }
    
}
