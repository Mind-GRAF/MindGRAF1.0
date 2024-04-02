package actuators;

import java.util.Random;

import mgip.Scheduler;
import nodes.ActNode;
import set.NodeSet;

public class DoAllActuator implements ControlActuator{

    private static DoAllActuator actuator;

    @Override
    public void actOnNode(ActNode node, int AttitudeID) {
        Random rand = new Random();
		NodeSet acts = node.getDownCableSet().get("obj").getNodeSet();
		NodeSet actsCopy = new NodeSet();
		actsCopy.addAllTo(acts);
		while(!actsCopy.isEmpty()) {
			int nextActIndex = rand.nextInt(actsCopy.size());
			ActNode nextAct = (ActNode) actsCopy.getNode(nextActIndex);
			nextAct.restartAgenda();
			Scheduler.addToActQueue(nextAct);
			actsCopy.remove(nextAct);
		}
    }

    public static DoAllActuator init() {
		if(actuator == null) {
			actuator = new DoAllActuator();
		}
		return actuator;
	}
    
}
