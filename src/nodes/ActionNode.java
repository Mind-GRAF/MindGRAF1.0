package nodes;

import actuators.AttitudeActuator;
import actuators.ControlActuator;

public class ActionNode extends Node {
	
	private ControlActuator actuator;
	private AttitudeActuator attitudeActuator;
	
	public ActionNode(String name) {
		super(name, false);
	}
	
	public boolean isPremitive() {
		return actuator != null;
	}
	
	public void setActuator(ControlActuator actuator) {
		this.actuator = actuator;
	}

    public void setActuator(AttitudeActuator actuator) {
        this.attitudeActuator = actuator;
    }
	
	public void runActuator(ActNode node) {
		actuator.actOnNode(node);
	}

}