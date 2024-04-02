package actuators;

import java.util.Random;

import mgip.Scheduler;
import nodes.ActNode;
import set.NodeSet;

public class DoOneActuator implements ControlActuator{

    private static DoOneActuator actuator;

    @Override
    public void actOnNode(ActNode node, int AttitudeID) {
        Random rand = new Random();
		NodeSet possibleActs = node.getDownCableSet().get("obj").getNodeSet();
		int actIndex = rand.nextInt(possibleActs.size());
		ActNode act = (ActNode) possibleActs.getNode(actIndex);
		act.restartAgenda();
		Scheduler.addToActQueue(act);
    }

    public static DoOneActuator init() {
        if(actuator == null) {
            actuator = new DoOneActuator();
        }
        return actuator;
    }
    
}
